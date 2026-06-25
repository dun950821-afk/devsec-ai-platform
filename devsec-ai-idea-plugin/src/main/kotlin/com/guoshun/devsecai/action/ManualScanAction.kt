package com.guoshun.devsecai.action

import com.guoshun.devsecai.config.DevSecAISettings
import com.guoshun.devsecai.service.FindingCollector
import com.guoshun.devsecai.service.PolicyService
import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.ex.InspectionManagerEx
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiManager

/**
 * 手动触发安全扫描
 * 通过 IntelliJ InspectionManager 触发全项目 Inspection，真正执行 SAST/Secrets 检测
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

        // 刷新策略（在用户主动触发时允许网络请求）
        policyService.refreshPolicy()

        val policy = policyService.getPolicy()
        if (policy?.data == null) {
            Messages.showWarningDialog(
                project,
                "尚未获取安全策略，请先完成握手认证。",
                "DevSecAI"
            )
            return
        }

        // 先清空旧结果
        val findingCollector = FindingCollector.getInstance(project)
        findingCollector.clearFindings()

        // 执行全项目 Inspection
        runFullProjectInspection(project)
    }

    /**
     * 通过 InspectionManagerEx 触发全项目代码分析
     * 这会真正调用所有已注册的 LocalInspectionTool（包括 SecurityInspectionTool 和 SecretsInspectionTool）
     */
    private fun runFullProjectInspection(project: Project) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "DevSecAI 安全扫描", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    indicator.isIndeterminate = false
                    indicator.text = "正在准备安全扫描..."

                    val inspectionManager = InspectionManagerEx.getInstance(project)
                    val scope = AnalysisScope(project)
                    scope.setIncludeTestSource(false)

                    val psiManager = PsiManager.getInstance(project)
                    val totalFiles = scope.fileCount
                    var processedFiles = 0

                    indicator.text = "正在扫描项目文件 ($totalFiles 个)..."

                    // 遍历项目中的每个文件，触发 Inspection
                    scope.accept { virtualFile ->
                        if (indicator.isCanceled) return@accept

                        processedFiles++
                        indicator.fraction = processedFiles.toDouble() / totalFiles.coerceAtLeast(1).toDouble()
                        indicator.text2 = virtualFile.name

                        try {
                            val psiFile = psiManager.findFile(virtualFile)
                            if (psiFile != null) {
                                // 对每个文件执行 Inspection 分析
                                // LocalInspectionTool.buildVisitor 会在内部被调用
                                inspectionManager.runInspectionOnFile(
                                    psiFile,
                                    inspectionManager.currentProfile,
                                    false
                                )
                            }
                        } catch (ex: Exception) {
                            // 单文件检测失败不中断整体扫描
                            logger.debug("Inspection skipped for ${virtualFile.name}: ${ex.message}")
                        }
                    }

                    indicator.fraction = 1.0
                    indicator.text = "扫描完成"

                } catch (e: Exception) {
                    logger.error("安全扫描失败", e)
                }
            }

            override fun onSuccess() {
                val findingCollector = FindingCollector.getInstance(project)
                val findings = findingCollector.getFindings()

                // 自动上送
                val settings = DevSecAISettings.getInstance()
                if (settings.uploadFindings && findings.isNotEmpty()) {
                    findingCollector.flush()
                }

                val critical = findings.count { it.severity == "CRITICAL" }
                val high = findings.count { it.severity == "HIGH" }
                val medium = findings.count { it.severity == "MEDIUM" }
                val low = findings.count { it.severity == "LOW" }

                Messages.showInfoMessage(
                    project,
                    "扫描完成！共发现 ${findings.size} 项安全问题。\n" +
                            "严重: $critical  高危: $high  中危: $medium  低危: $low\n\n" +
                            "详细结果请在 DevSecAI 工具窗口中查看。",
                    "DevSecAI 扫描结果"
                )
            }
        })
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
