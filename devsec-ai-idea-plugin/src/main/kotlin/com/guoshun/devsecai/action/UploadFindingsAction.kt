package com.guoshun.devsecai.action

import com.guoshun.devsecai.service.FindingCollector
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class UploadFindingsAction : AnAction("上传检测结果", "将检测结果上传到管理平台", null) {

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
                Messages.showInfoMessage(project, "当前没有待上传的检测结果。", "上传检测结果")
                return
            }

            val result = collector.flush()
            val summary = "成功：${result.uploaded}，失败：${result.failed}，待上传：${result.pending}"
            if (result.failed > 0) {
                val reason = result.failureReasons.joinToString("\n").ifBlank { "平台未返回明确失败原因" }
                Messages.showErrorDialog(
                    project,
                    "上传失败。\n$summary\n\n失败原因：\n$reason",
                    "上传检测结果"
                )
                return
            }
            Messages.showInfoMessage(
                    project,
                    "上传成功。\n$summary",
                    "上传检测结果"
            )
        } catch (e: Exception) {
            logger.warn("上传检测结果失败：${e.message}")
            Messages.showErrorDialog(
                    project,
                    "上传失败：${e.message}",
                    "上传检测结果"
            )
        }
    }
}
