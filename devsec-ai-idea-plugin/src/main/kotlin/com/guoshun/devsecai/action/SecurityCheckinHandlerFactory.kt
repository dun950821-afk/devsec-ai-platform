package com.guoshun.devsecai.action

import com.guoshun.devsecai.service.FindingCollector
import com.guoshun.devsecai.service.PolicyService
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages

/**
 * Git 提交检查处理器工厂
 * 在代码提交前执行安全检查，如果存在高危漏洞则阻断提交
 */
class SecurityCheckinHandlerFactory : CheckinHandlerFactory() {

    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return SecurityCheckinHandler(panel)
    }
}

/**
 * Git 提交安全检查处理器
 */
class SecurityCheckinHandler(private val panel: CheckinProjectPanel) : CheckinHandler() {

    companion object {
        private val LOG = Logger.getInstance(SecurityCheckinHandler::class.java)
    }

    override fun beforeCheckin(): ReturnResult {
        val project = panel.project
        val policyService = PolicyService.getInstance(project)

        // 如果未启用 Git 提交检查，跳过
        if (!policyService.isOnCommitScanEnabled()) {
            return ReturnResult.COMMIT
        }

        val collector = FindingCollector.getInstance(project)
        val findings = collector.getFindings()

        // 统计高危及以上漏洞
        val criticalCount = findings.count { it.severity == "CRITICAL" }
        val highCount = findings.count { it.severity == "HIGH" }

        if (criticalCount > 0 && policyService.shouldBlockCritical()) {
            LOG.warn("Commit blocked: $criticalCount critical findings")
            val result = Messages.showYesNoDialog(
                project,
                "DevSecAI 安全检查发现 $criticalCount 个严重漏洞，是否仍要提交？\n" +
                        "可在 DevSecAI 工具窗口查看详情。",
                "DevSecAI 安全检查",
                Messages.getWarningIcon()
            )
            if (result == Messages.NO) {
                return ReturnResult.CANCEL
            }
        }

        if (highCount > 0 && policyService.shouldBlockHigh()) {
            LOG.warn("Commit warning: $highCount high findings")
            val result = Messages.showYesNoDialog(
                project,
                "DevSecAI 安全检查发现 $highCount 个高危漏洞，是否仍要提交？\n" +
                        "可在 DevSecAI 工具窗口查看详情。",
                "DevSecAI 安全检查",
                Messages.getWarningIcon()
            )
            if (result == Messages.NO) {
                return ReturnResult.CANCEL
            }
        }

        return ReturnResult.COMMIT
    }
}
