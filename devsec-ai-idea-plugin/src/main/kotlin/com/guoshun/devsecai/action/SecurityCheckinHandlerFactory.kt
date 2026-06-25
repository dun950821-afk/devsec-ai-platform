package com.guoshun.devsecai.action

import com.guoshun.devsecai.model.LocalFinding
import com.guoshun.devsecai.service.FindingCollector
import com.guoshun.devsecai.service.PolicyService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.intellij.openapi.wm.WindowManager

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

                // Group by severity
                val critical = blockingFindings.count { it.severity == "CRITICAL" }
                val high = blockingFindings.count { it.severity == "HIGH" }
                val medium = blockingFindings.count { it.severity == "MEDIUM" }
                val low = blockingFindings.count { it.severity == "LOW" }

                val message = StringBuilder("DevSecAI Security Check - ${blockingFindings.size} blocking finding(s):\n")
                if (critical > 0) message.append("  Critical: $critical\n")
                if (high > 0) message.append("  High: $high\n")
                if (medium > 0) message.append("  Medium: $medium\n")
                if (low > 0) message.append("  Low: $low\n")

                if (blockingRules.allowOverride) {
                    message.append("\nDo you want to commit anyway?")
                    val frame = WindowManager.getInstance().getFrame(project)
                    val result = Messages.showYesNoDialog(
                            frame, message.toString(), "DevSecAI Security Check",
                            Messages.getWarningIcon()
                    )
                    return if (result == Messages.YES) ReturnResult.COMMIT else ReturnResult.CANCEL
                } else {
                    Messages.showErrorDialog(
                            project, message.toString(), "DevSecAI: Commit Blocked"
                    )
                    return ReturnResult.CANCEL
                }
            } catch (e: Exception) {
                logger.warn("Security checkin handler error: ${e.message}")
                return ReturnResult.COMMIT
            }
        }
    }
}
