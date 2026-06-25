package com.guoshun.devsecai.action

import com.guoshun.devsecai.service.FindingCollector
import com.guoshun.devsecai.service.PolicyService
import com.guoshun.devsecai.service.SecurityScanService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory

class SecurityCheckinHandlerFactory : CheckinHandlerFactory() {

    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return SecurityCheckinHandler(panel.project)
    }

    private class SecurityCheckinHandler(private val project: Project) : CheckinHandler() {

        private val logger = Logger.getInstance(SecurityCheckinHandler::class.java)

        override fun beforeCheckin(): ReturnResult {
            val settings = com.guoshun.devsecai.config.DevSecAISettings.getInstance()
            if (!settings.gitCommitCheck) {
                return ReturnResult.COMMIT
            }

            try {
                val policyService = project.getService(PolicyService::class.java)
                val blockingRules = policyService.getBlockingRulesCached()
                val collector = project.getService(FindingCollector::class.java)
                if (collector.getFindings().isEmpty()) {
                    project.getService(SecurityScanService::class.java).scanProject(clearPrevious = false)
                }
                val findings = collector.getFindings()

                val blockedSeverities = mutableSetOf<String>()
                if (blockingRules.blockCritical) blockedSeverities.add("CRITICAL")
                if (blockingRules.blockHigh) blockedSeverities.add("HIGH")
                if (blockingRules.blockMedium) blockedSeverities.add("MEDIUM")
                if (blockingRules.blockLow) blockedSeverities.add("LOW")

                val blockingFindings = findings.filter { it.severity in blockedSeverities }

                if (blockingFindings.isEmpty()) {
                    return ReturnResult.COMMIT
                }

                val critical = blockingFindings.count { it.severity == "CRITICAL" }
                val high = blockingFindings.count { it.severity == "HIGH" }
                val medium = blockingFindings.count { it.severity == "MEDIUM" }
                val low = blockingFindings.count { it.severity == "LOW" }

                val message = StringBuilder("DevSecAI 提交前安全检查发现 ${blockingFindings.size} 个阻断风险：\n")
                if (critical > 0) message.append("  严重：$critical\n")
                if (high > 0) message.append("  高危：$high\n")
                if (medium > 0) message.append("  中危：$medium\n")
                if (low > 0) message.append("  低危：$low\n")

                if (blockingRules.allowOverride) {
                    message.append("\n是否仍要继续提交？")
                    val result = Messages.showYesNoDialog(
                            project, message.toString(), "DevSecAI 提交前安全检查",
                            Messages.getWarningIcon()
                    )
                    return if (result == Messages.YES) ReturnResult.COMMIT else ReturnResult.CANCEL
                } else {
                    Messages.showErrorDialog(
                            project, message.toString(), "DevSecAI：提交已阻断"
                    )
                    return ReturnResult.CANCEL
                }
            } catch (e: Exception) {
                logger.warn("提交前安全检查异常：${e.message}")
                return ReturnResult.COMMIT
            }
        }
    }
}
