package com.guoshun.devsecai.inspection

import com.guoshun.devsecai.model.LocalFinding
import com.guoshun.devsecai.service.FindingCollector
import com.guoshun.devsecai.service.PolicyService
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.*

class SecretsInspectionTool : LocalInspectionTool() {

    private val logger = Logger.getInstance(SecretsInspectionTool::class.java)

    private val patterns = listOf(
            SecretPattern("AWS Access Key", "AKIA[0-9A-Z]{16}", "CRITICAL", "SECRETS-AWS-KEY",
                    "AWS Access Key ID detected. Rotate the key immediately."),
            SecretPattern("AWS Secret Key", "(?i)aws(.{0,20})?(?-i)['\\\"][0-9a-zA-Z/+]{40}['\\\"]", "CRITICAL", "SECRETS-AWS-SECRET",
                    "AWS Secret Access Key detected. Rotate the key immediately."),
            SecretPattern("GitHub Token", "gh[ps]_[0-9a-zA-Z]{36}", "CRITICAL", "SECRETS-GITHUB-TOKEN",
                    "GitHub Token detected. Revoke and regenerate the token."),
            SecretPattern("JWT Token", "eyJ[A-Za-z0-9-_]+\\.eyJ[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+", "HIGH", "SECRETS-JWT",
                    "JWT Token detected in source code. Store in environment variables."),
            SecretPattern("Private Key", "-----BEGIN (?:RSA |EC |DSA )?PRIVATE KEY-----", "CRITICAL", "SECRETS-PRIVATE-KEY",
                    "Private key detected. Store in secure key management system."),
            SecretPattern("Database Password", "(?i)(password|passwd|pwd)\\s*[:=]\\s*['\\\"][^'\\\"]{8,}['\\\"]", "HIGH", "SECRETS-DB-PASSWORD",
                    "Database password detected. Use environment variables or vault."),
            SecretPattern("API Key Generic", "(?i)(api[_-]?key|apikey)\\s*[:=]\\s*['\\\"][^'\\\"]{16,}['\\\"]", "HIGH", "SECRETS-API-KEY",
                    "API key detected. Store in environment variables or vault."),
            SecretPattern("Slack Token", "xox[baprs]-[0-9]{10,}-[0-9a-zA-Z]{24,}", "HIGH", "SECRETS-SLACK-TOKEN",
                    "Slack token detected. Revoke and use environment variables.")
    )

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val project = holder.project

        // Only use cached policy - NEVER trigger network I/O in PSI path
        val policyService = project.getService(PolicyService::class.java)
        if (!policyService.isSecretsEnabledCached()) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        return object : JavaElementVisitor() {
            override fun visitLiteralExpression(expression: PsiLiteralExpression) {
                super.visitLiteralExpression(expression)
                val text = expression.text ?: return
                checkSecrets(expression, text, holder, project)
            }

            override fun visitComment(comment: PsiComment) {
                super.visitComment(comment)
                val text = comment.text
                checkSecrets(comment, text, holder, project)
            }
        }
    }

    private fun checkSecrets(element: PsiElement, text: String, holder: ProblemsHolder, project: Project) {
        for (pattern in patterns) {
            try {
                val regex = Regex(pattern.regex)
                if (regex.containsMatchIn(text)) {
                    val message = "Secret Detected: ${pattern.name}. ${pattern.recommendation}"
                    holder.registerProblem(element, message,
                            com.intellij.codeInspection.ProblemHighlightType.WARNING)
                    addFinding(project, pattern.ruleId, pattern.severity, pattern.name,
                            pattern.recommendation,
                            element.containingFile?.virtualFile?.path ?: "",
                            getLineNumber(element), getLineNumber(element))
                }
            } catch (e: Exception) {
                logger.warn("Regex error for pattern ${pattern.name}: ${e.message}")
            }
        }
    }

    private fun addFinding(project: Project, ruleId: String, severity: String, title: String,
                           description: String, filePath: String, startLine: Int, endLine: Int) {
        try {
            val collector = project.getService(FindingCollector::class.java)
            collector.addFinding(LocalFinding(
                    ruleId = ruleId,
                    severity = severity,
                    title = title,
                    description = description,
                    filePath = filePath,
                    startLine = startLine,
                    endLine = endLine,
                    module = "SECRETS"
            ))
        } catch (e: Exception) {
            logger.warn("Failed to add finding: ${e.message}")
        }
    }

    private fun getLineNumber(element: PsiElement): Int {
        return try {
            val doc = com.intellij.openapi.editor.Document.getInstance(element.containingFile)
            val offset = element.textOffset
            doc.getLineNumber(offset) + 1
        } catch (e: Exception) {
            0
        }
    }

    private data class SecretPattern(
            val name: String,
            val regex: String,
            val severity: String,
            val ruleId: String,
            val recommendation: String
    )
}
