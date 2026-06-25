package com.guoshun.devsecai.action

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile

class ManualScanAction : AnAction("DevSecAI Security Scan", "Run security scan on current project", null) {

    private val logger = Logger.getInstance(ManualScanAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        runSecurityScan(project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    private fun runSecurityScan(project: Project) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "DevSecAI Security Scan", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                indicator.text = "Collecting source files..."

                try {
                    val sourceRoots = ProjectRootManager.getInstance(project).contentSourceRoots
                    val filesToScan = mutableListOf<VirtualFile>()

                    for (root in sourceRoots) {
                        collectSourceFiles(root, filesToScan)
                    }

                    indicator.fraction = 0.0
                    val total = filesToScan.size
                    if (total == 0) {
                        indicator.text = "No source files found to scan"
                        return
                    }

                    // Run daemon analyzer on each file to trigger local inspections
                    // (SecurityInspectionTool / SecretsInspectionTool will be invoked by the platform)
                    val daemonAnalyzer = DaemonCodeAnalyzer.getInstance(project)
                    val psiManager = com.intellij.psi.PsiManager.getInstance(project)

                    for ((index, file) in filesToScan.withIndex()) {
                        if (indicator.isCanceled) break
                        if (project.isDisposed) break

                        indicator.fraction = (index + 1).toDouble() / total
                        indicator.text = "Scanning: ${file.name}"

                        try {
                            val psiFile = psiManager.findFile(file)
                            if (psiFile != null) {
                                // Restart daemon on the file triggers all registered
                                // LocalInspectionTools including our SecurityInspectionTool
                                // and SecretsInspectionTool. The platform will invoke
                                // buildVisitor() and collect problems for us.
                                daemonAnalyzer.restart(psiFile)
                            }
                        } catch (ex: Exception) {
                            logger.warn("Error scanning ${file.name}: ${ex.message}")
                        }
                    }

                    indicator.text = "Security scan completed"
                    indicator.fraction = 1.0
                } catch (e: Exception) {
                    logger.error("Security scan failed: ${e.message}", e)
                    indicator.text = "Security scan failed"
                }
            }
        })
    }

    private fun collectSourceFiles(root: VirtualFile, files: MutableList<VirtualFile>) {
        if (!root.isDirectory) {
            val ext = root.extension?.lowercase() ?: ""
            if (ext in setOf("java", "kt", "kts", "xml", "properties", "yml", "yaml")) {
                files.add(root)
            }
            return
        }
        val children = root.children
        for (child in children) {
            collectSourceFiles(child, files)
        }
    }
}
