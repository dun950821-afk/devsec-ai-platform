package com.guoshun.devsecai.inspection

import com.guoshun.devsecai.config.DevSecAISettings
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager

object DevSecAIInspectionSupport {

    fun problemMessage(title: String, reason: String, recommendation: String): String {
        return "DevSecAI $title\n问题原因：$reason\n修复建议：$recommendation"
    }

    fun highlightType(severity: String): ProblemHighlightType {
        return if (configuredLevel(severity) == "ERROR") ProblemHighlightType.ERROR else ProblemHighlightType.WARNING
    }

    fun annotationSeverity(severity: String): HighlightSeverity {
        return if (configuredLevel(severity) == "ERROR") HighlightSeverity.ERROR else HighlightSeverity.WARNING
    }

    fun tooltip(message: String): String {
        return message
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\n", "<br/>")
    }

    private fun configuredLevel(severity: String): String {
        val settings = DevSecAISettings.getInstance()
        return when (severity) {
            "CRITICAL" -> settings.criticalHighlightLevel
            "HIGH" -> settings.highHighlightLevel
            "MEDIUM" -> settings.mediumHighlightLevel
            "LOW" -> settings.lowHighlightLevel
            else -> settings.infoHighlightLevel
        }
    }

    fun fixes(ruleTitle: String, recommendation: String, replacement: String? = null): Array<LocalQuickFix> {
        return arrayOf(
            LocalSuggestionFix(ruleTitle, recommendation, replacement),
            LlmSuggestionFix(ruleTitle, recommendation)
        )
    }

    private class LocalSuggestionFix(
        private val ruleTitle: String,
        private val recommendation: String,
        private val replacement: String?
    ) : LocalQuickFix {

        override fun getFamilyName(): String = "DevSecAI 本地修复"

        override fun getName(): String {
            return if (replacement == null) "查看 DevSecAI 本地修复建议" else "应用 DevSecAI 本地修复"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement ?: return
            if (replacement == null) {
                Messages.showInfoMessage(
                    project,
                    "$ruleTitle\n\n建议：$recommendation",
                    "DevSecAI 本地修复建议"
                )
                return
            }

            WriteCommandAction.runWriteCommandAction(project, "DevSecAI 本地修复", null, Runnable {
                val document = PsiDocumentManager.getInstance(project).getDocument(element.containingFile) ?: return@Runnable
                document.replaceString(element.textRange.startOffset, element.textRange.endOffset, replacement)
                PsiDocumentManager.getInstance(project).commitDocument(document)
            }, element.containingFile)
        }
    }

    private class LlmSuggestionFix(
        private val ruleTitle: String,
        private val recommendation: String
    ) : LocalQuickFix {

        override fun getFamilyName(): String = "DevSecAI LLM 修复"

        override fun getName(): String = "使用 DevSecAI LLM 生成修复方案"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            val code = element?.text?.take(1200).orEmpty()
            val prompt = buildString {
                appendLine("请修复以下安全问题：$ruleTitle")
                appendLine("修复建议：$recommendation")
                if (code.isNotBlank()) {
                    appendLine()
                    appendLine("相关代码：")
                    appendLine(code)
                }
            }
            Messages.showInfoMessage(
                project,
                "已生成 LLM 修复上下文，可发送到 DevSecAI 平台的 AI 修复能力。\n\n$prompt",
                "DevSecAI LLM 修复"
            )
        }
    }
}
