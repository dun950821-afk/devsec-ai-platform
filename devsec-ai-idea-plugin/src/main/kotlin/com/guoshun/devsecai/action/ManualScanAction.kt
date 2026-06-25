package com.guoshun.devsecai.action

import com.guoshun.devsecai.config.DevSecAISettings
import com.guoshun.devsecai.service.SecurityScanService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class ManualScanAction : AnAction("DevSecAI 安全扫描", "对当前项目执行安全扫描", null) {

    private val logger = Logger.getInstance(ManualScanAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        runSecurityScan(project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    private fun runSecurityScan(project: Project) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "DevSecAI 安全扫描", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                indicator.text = "正在准备安全扫描..."

                try {
                    val scanner = project.getService(SecurityScanService::class.java)
                    val summary = scanner.scanProject(indicator, clearPrevious = true)
                    indicator.text = "安全扫描完成：发现 ${summary.findingsFound} 个风险"
                    indicator.fraction = 1.0

                    if (DevSecAISettings.getInstance().autoUploadFindings && summary.findingsFound > 0) {
                        project.getService(com.guoshun.devsecai.service.FindingCollector::class.java).flush()
                    }
                } catch (e: Exception) {
                    logger.error("安全扫描失败：${e.message}", e)
                    indicator.text = "安全扫描失败"
                }
            }

            override fun onSuccess() {
                val count = project.getService(com.guoshun.devsecai.service.FindingCollector::class.java).getFindings().size
                Messages.showInfoMessage(
                    project,
                    "安全扫描完成。已在 DevSecAI 工具窗口生成 $count 个风险结果。",
                    "DevSecAI 安全扫描"
                )
            }
        })
    }
}
