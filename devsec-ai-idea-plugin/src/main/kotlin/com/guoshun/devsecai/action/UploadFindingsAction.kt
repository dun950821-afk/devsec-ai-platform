package com.guoshun.devsecai.action

import com.guoshun.devsecai.service.FindingCollector
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

/**
 * 手动上送检测结果到管理平台
 */
class UploadFindingsAction : AnAction("上送检测结果", "将检测结果上送到 DevSecAI 管理平台", null) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val collector = FindingCollector.getInstance(project)
        val pendingFindings = collector.getPendingFindings()

        if (pendingFindings.isEmpty()) {
            Messages.showInfoMessage(project, "当前没有需要上送的检测结果。", "DevSecAI")
            return
        }

        object : Task.Backgroundable(project, "上送检测结果", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "正在上送 ${pendingFindings.size} 条检测结果..."
                collector.flush()
            }

            override fun onSuccess() {
                Messages.showInfoMessage(project, "成功上送检测结果。", "DevSecAI")
            }

            override fun onThrowable(error: Throwable) {
                Messages.showErrorMessage(project, "上送失败: ${error.message}", "DevSecAI")
            }
        }.queue()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }
}
