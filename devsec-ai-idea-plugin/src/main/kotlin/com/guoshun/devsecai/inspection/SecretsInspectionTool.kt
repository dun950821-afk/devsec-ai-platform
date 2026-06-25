package com.guoshun.devsecai.inspection

import com.guoshun.devsecai.model.LocalFinding
import com.guoshun.devsecai.service.FindingCollector
import com.guoshun.devsecai.service.PolicyService
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile

/**
 * Secrets 敏感信息检测引擎
 * 检测代码和配置文件中的密钥、Token、密码等敏感信息
 */
class SecretsInspectionTool : LocalInspectionTool() {

    private val logger = Logger.getInstance(SecretsInspectionTool::class.java)

    // 敏感信息正则表达式规则
    private val secretPatterns = listOf(
        SecretPattern("SECRETS-AWS-KEY", "AWS Access Key", """AKIA[0-9A-Z]{16}""", "CRITICAL"),
        SecretPattern("SECRETS-AWS-SECRET", "AWS Secret Key", """(?i)aws(.{0,20})?(?-i)['\"][0-9a-zA-Z/+]{40}['\"]""", "CRITICAL"),
        SecretPattern("SECRETS-GITHUB-TOKEN", "GitHub Token", """gh[pousr]_[0-9a-zA-Z]{36}""", "CRITICAL"),
        SecretPattern("SECRETS-JWT-SECRET", "JWT Secret", """(?i)(jwt[_-]?secret|jwt[_-]?key)['\"]?\s*[:=]\s*['\"][^'\"]{8,}['\"]""", "HIGH"),
        SecretPattern("SECRETS-PRIVATE-KEY", "Private Key", """-----BEGIN (?:RSA |EC |DSA )?PRIVATE KEY-----""", "CRITICAL"),
        SecretPattern("SECRETS-DATABASE-PWD", "Database Password", """(?i)(db[_-]?password|database[_-]?pwd|mysql[_-]?password|postgres[_-]?password)['\"]?\s*[:=]\s*['\"][^'\"]{4,}['\"]""", "HIGH"),
        SecretPattern("SECRETS-API-KEY", "API Key", """(?i)(api[_-]?key|apikey|access[_-]?key)['\"]?\s*[:=]\s*['\"][^'\"]{8,}['\"]""", "MEDIUM"),
        SecretPattern("SECRETS-SECRET-KEY", "Secret Key", """(?i)(secret[_-]?key|secretkey|encryption[_-]?key)['\"]?\s*[:=]\s*['\"][^'\"]{8,}['\"]""", "HIGH"),
        SecretPattern("SECRETS-PASSWORD", "Hardcoded Password", """(?i)(password|passwd|pwd)['\"]?\s*[:=]\s*['\"][^'\"]{4,}['\"]""", "MEDIUM"),
        SecretPattern("SECRETS-TOKEN", "Access Token", """(?i)(token|access[_-]?token|auth[_-]?token)['\"]?\s*[:=]\s*['\"][^'\"]{8,}['\"]""", "MEDIUM"),
        SecretPattern("SECRETS-SLACK-TOKEN", "Slack Token", """xox[baprs]-[0-9]{10,13}-[0-9a-zA-Z]{24,34}""", "HIGH"),
        SecretPattern("SECRETS-STRIPE-KEY", "Stripe Key", """[sr]k_(live|test)_[0-9a-zA-Z]{24}""", "CRITICAL"),
    )

    // 排除的文件模式
    private val excludedFilePatterns = setOf(
        ".lock", ".min.js", ".min.css", ".map",
        "package-lock.json", "yarn.lock", "pnpm-lock.yaml"
    )

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val project = holder.project
        val policyService = PolicyService.getInstance(project)

        if (!policyService.isSecretsEnabled()) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        return SecretsVisitor(holder, project)
    }

    private inner class SecretsVisitor(
        private val holder: ProblemsHolder,
        private val project: Project
    ) : PsiElementVisitor() {

        override fun visitFile(file: PsiFile) {
            super.visitFile(file)

            val fileName = file.name.lowercase()
            if (excludedFilePatterns.any { fileName.endsWith(it) }) return
            if (fileName.contains("test") || fileName.contains("spec")) return

            val text = file.text ?: return
            val doc = file.viewProvider.document ?: return

            for (pattern in secretPatterns) {
                val regex = Regex(pattern.regex)
                val matches = regex.findAll(text)

                for (match in matches) {
                    val lineNumber = doc.getLineNumber(match.range.first) + 1
                    val matchedText = match.value

                    // 跳过明显的占位符
                    if (isPlaceholder(matchedText)) continue

                    // 上送 Finding
                    val finding = LocalFinding(
                        ruleId = pattern.ruleId,
                        module = "SECRETS",
                        severity = pattern.severity,
                        title = pattern.title,
                        description = "检测到${pattern.title}泄露，文件中包含敏感信息",
                        filePath = file.virtualFile.path,
                        startLine = lineNumber,
                        endLine = lineNumber
                    )
                    FindingCollector.getInstance(project).addFinding(finding)

                    // 高危以上的在IDE中标记
                    if (pattern.severity in setOf("CRITICAL", "HIGH")) {
                        val highlightType = when (pattern.severity) {
                            "CRITICAL" -> ProblemHighlightType.GENERIC_ERROR
                            else -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                        }
                        // 全文件级别警告
                        holder.registerProblem(
                            file,
                            "[DevSecAI] 检测到${pattern.title}泄露 (行$lineNumber)",
                            highlightType
                        )
                    }
                }
            }
        }

        private fun isPlaceholder(text: String): Boolean {
            val placeholders = setOf(
                "xxx", "your-", "change-me", "replace", "example",
                "placeholder", "todo", "fixme", "insert", "<", ">",
                "xxxx", "****", "0000", "1234", "abcd"
            )
            val lower = text.lowercase()
            return placeholders.any { lower.contains(it) }
        }
    }

    data class SecretPattern(
        val ruleId: String,
        val title: String,
        val regex: String,
        val severity: String
    )
}
