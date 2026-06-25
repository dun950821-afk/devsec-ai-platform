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
                    "检测到 AWS Access Key ID，请立即轮换密钥。"),
            SecretPattern("AWS Secret Key", "(?i)aws(.{0,20})?(?-i)['\\\"][0-9a-zA-Z/+]{40}['\\\"]", "CRITICAL", "SECRETS-AWS-SECRET",
                    "检测到 AWS Secret Access Key，请立即轮换密钥。"),
            SecretPattern("GitHub Token", "gh[ps]_[0-9a-zA-Z]{36}", "CRITICAL", "SECRETS-GITHUB-TOKEN",
                    "检测到 GitHub Token，请吊销后重新生成。"),
            SecretPattern("JWT Token", "eyJ[A-Za-z0-9-_]+\\.eyJ[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+", "HIGH", "SECRETS-JWT",
                    "源码中检测到 JWT Token，请改为环境变量或安全配置。"),
            SecretPattern("私钥", "-----BEGIN (?:RSA |EC |DSA )?PRIVATE KEY-----", "CRITICAL", "SECRETS-PRIVATE-KEY",
                    "检测到私钥，请存放到安全密钥管理系统。"),
            SecretPattern("数据库密码", "(?i)(password|passwd|pwd)\\s*[:=]\\s*['\\\"][^'\\\"]{8,}['\\\"]", "HIGH", "SECRETS-DB-PASSWORD",
                    "检测到数据库密码，请使用环境变量或密钥库。"),
            SecretPattern("通用 API Key", "(?i)(api[_-]?key|apikey)\\s*[:=]\\s*['\\\"][^'\\\"]{16,}['\\\"]", "HIGH", "SECRETS-API-KEY",
                    "检测到 API Key，请使用环境变量或密钥库。"),
            SecretPattern("Slack Token", "xox[baprs]-[0-9]{10,}-[0-9a-zA-Z]{24,}", "HIGH", "SECRETS-SLACK-TOKEN",
                    "检测到 Slack Token，请吊销后改用环境变量。")
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
                    val reason = "代码中出现了 ${pattern.name}，仓库、日志或构建产物泄露后会导致凭据暴露。"
                    val message = DevSecAIInspectionSupport.problemMessage("敏感信息泄露：${pattern.name}", reason, pattern.recommendation)
                    val replacement = if (element is PsiLiteralExpression) "\"***REMOVED_SECRET***\"" else "/* DevSecAI: sensitive information removed */"
                    holder.registerProblem(element, message, DevSecAIInspectionSupport.highlightType(pattern.severity),
                            *DevSecAIInspectionSupport.fixes(pattern.name, pattern.recommendation, replacement))
                    addFinding(project, pattern.ruleId, pattern.severity, pattern.name,
                            pattern.recommendation,
                            element.containingFile?.virtualFile?.path ?: "",
                            getLineNumber(element), getLineNumber(element))
                }
            } catch (e: Exception) {
                logger.warn("敏感信息规则 ${pattern.name} 正则执行异常：${e.message}")
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
            logger.warn("添加检测结果失败：${e.message}")
        }
    }

    private fun getLineNumber(element: PsiElement): Int {
        return try {
            val file = element.containingFile ?: return 0
            val doc = com.intellij.psi.PsiDocumentManager.getInstance(element.project).getDocument(file)
                ?: return 0
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
