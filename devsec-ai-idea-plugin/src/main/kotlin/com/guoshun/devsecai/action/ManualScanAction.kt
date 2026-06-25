package com.guoshun.devsecai.action

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ex.InspectionManagerEx
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
                indicator.text = "Scanning project for security issues..."

                try {
                    val inspectionManagerEx = InspectionManagerEx.getInstance(project)

                    // Get all source files in project
                    val sourceRoots = ProjectRootManager.getInstance(project).contentSourceRoots
                    val filesToScan = mutableListOf<VirtualFile>()

                    for (root in sourceRoots) {
                        collectJavaFiles(root, filesToScan)
                    }

                    indicator.fraction = 0.0
                    val total = filesToScan.size
                    if (total == 0) {
                        indicator.text = "No Java source files found to scan"
                        return
                    }

                    // Run inspections on each file
                    for ((index, file) in filesToScan.withIndex()) {
                        if (indicator.isCanceled) break

                        indicator.fraction = (index + 1).toDouble() / total
                        indicator.text = "Scanning: ${file.name}"

                        try {
                            val psiFile = com.intellij.psi.PsiManager.getInstance(project).findFile(file)
                            if (psiFile != null) {
                                val globalContext = inspectionManagerEx.createNewGlobalContext(false)
                                val tool = com.intellij.codeInspection.InspectionEngine
                                DaemonCodeAnalyzer.getInstance(project).restart(psiFile)
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

    private fun collectJavaFiles(root: VirtualFile, files: MutableList<VirtualFile>) {
        if (!root.isDirectory) {
            if (root.extension.equals("java", ignoreCase = true)) {
                files.add(root)
            }
            return
        }
        val children = root.children
        for (child in children) {
            collectJavaFiles(child, files)
        }
    }
}
