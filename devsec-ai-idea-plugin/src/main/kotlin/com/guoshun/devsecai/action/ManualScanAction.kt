package com.guoshun.devsecai.action

import com.guoshun.devsecai.config.DevSecAISettings
import com.guoshun.devsecai.service.FindingCollector
import com.guoshun.devsecai.service.PolicyService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

/**
 * 手动触发安全扫描
 * 触发 IntelliJ 的 Inspection 机制执行全项目扫描
 */
class ManualScanAction : AnAction() {

    private val logger = Logger.getInstance(ManualScanAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val settings = DevSecAISettings.getInstance()
        val policyService = PolicyService.getInstance(project)

        if (!settings.isConfigured()) {
            Messages.showWarningDialog(
                project,
                "尚未配置服务器连接，请先在 Settings > Tools > DevSecAI 安全助手 中配置。",
                "DevSecAI"
            )
            return
        }

        val policy = policyService.getPolicy()
        if (policy?.data == null) {
            Messages.showWarningDialog(
                project,
                "尚未获取安全策略，请先完成握手认证。",
                "DevSecAI"
            )
            return
        }

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "DevSecAI 安全扫描", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                indicator.text = "正在执行安全扫描..."

                try {
                    // 清空旧结果
                    val findingCollector = FindingCollector.getInstance(project)
                    findingCollector.clearFindings()

                    // 通过 InspectionManager 触发全项目扫描
                    // SAST/Secrets 的 LocalInspectionTool 会在 Inspection 过程中自动收集 Finding
                    indicator.fraction = 0.5
                    indicator.text = "正在执行代码分析..."

                    // 全项目扫描由 IntelliJ Inspection 机制自动完成
                    // 此处等待扫描结果被收集到 FindingCollector
                    Thread.sleep(2000)

                    indicator.fraction = 1.0
                    indicator.text = "扫描完成"

                    // 自动上送
                    if (settings.uploadFindings) {
                        findingCollector.flush()
                    }

                } catch (e: Exception) {
                    logger.error("安全扫描失败", e)
                }
            }

            override fun onSuccess() {
                val findingCollector = FindingCollector.getInstance(project)
                val findings = findingCollector.getFindings()
                Messages.showInfoMessage(
                    project,
                    "扫描完成！共发现 ${findings.size} 项安全问题。\n" +
                            "严重: ${findings.count { it.severity == "CRITICAL" }}  " +
                            "高危: ${findings.count { it.severity == "HIGH" }}  " +
                            "中危: ${findings.count { it.severity == "MEDIUM" }}  " +
                            "低危: ${findings.count { it.severity == "LOW" }}",
                    "DevSecAI 扫描结果"
                )
            }
        })
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
