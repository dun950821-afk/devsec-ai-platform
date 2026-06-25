package com.guoshun.devsecai.action

import com.guoshun.devsecai.service.ApiResult
import com.guoshun.devsecai.service.FindingCollector
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class UploadFindingsAction : AnAction("Upload Findings", "Upload detection results to the platform", null) {

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

            val result = collector.flush()
            if (result.successful) {
                Messages.showInfoMessage(
                    project,
                    "Upload successful.\n${result.message}",
                    "Upload Findings"
                )
            } else {
                Messages.showErrorDialog(
                    project,
                    "Upload failed.\n${result.message}\n\nPlease check:\n1. Server URL in Settings\n2. Access Token is valid\n3. Platform server is running",
                    "Upload Findings"
                )
            }
        } catch (e: Exception) {
            logger.warn("Upload findings failed: ${e.message}")
            Messages.showErrorDialog(
                e.project ?: return,
                "Upload exception: ${e.message}",
                "Upload Findings"
            )
        }
    }
}
