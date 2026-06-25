package com.guoshun.devsecai.action

import com.guoshun.devsecai.service.FindingCollector
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class UploadFindingsAction : AnAction("Upload Findings", "Upload detection results to management platform", null) {

    private val logger = Logger.getInstance(UploadFindingsAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        uploadFindings(project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    private fun uploadFindings(project: Project) {
        try {
            val collector = project.getService(FindingCollector::class.java)
            val pendingCount = collector.getPendingCount()

            if (pendingCount == 0) {
                Messages.showInfoMessage(project, "No pending findings to upload.", "Upload Findings")
                return
            }

            collector.flush()
            Messages.showInfoMessage(
                    project,
                    "Upload completed. $pendingCount finding(s) uploaded.",
                    "Upload Findings"
            )
        } catch (e: Exception) {
            logger.warn("Upload findings failed: ${e.message}")
            Messages.showErrorDialog(
                    project,
                    "Upload failed: ${e.message}",
                    "Upload Findings"
            )
        }
    }
}
