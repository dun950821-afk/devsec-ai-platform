package com.guoshun.devsecai.inspection

import com.guoshun.devsecai.model.LocalFinding
import com.guoshun.devsecai.service.FindingCollector
import com.guoshun.devsecai.service.PolicyService
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

/**
 * SAST 安全检测引擎
 * 基于 PSI 分析 Java/Kotlin 代码中的安全漏洞
 *
 * 重要：buildVisitor 中仅读取 PolicyService 的本地缓存，
 * 绝不触发网络请求，避免在 PSI 分析路径中造成 IDE 卡顿。
 */
class SecurityInspectionTool : LocalInspectionTool() {

    private val logger = Logger.getInstance(SecurityInspectionTool::class.java)

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val project = holder.project
        val policyService = PolicyService.getInstance(project)

        // 仅使用缓存策略判断，不触发网络请求
        // 如果策略未加载（缓存为空），默认启用检测
        if (!policyService.isSastEnabledCached()) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        return SecurityVisitor(holder, project)
    }

    /**
     * 安全检查访问器
     */
    private inner class SecurityVisitor(
        private val holder: ProblemsHolder,
        private val project: Project
    ) : JavaElementVisitor() {

        override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
            super.visitMethodCallExpression(expression)
            checkSqlInjection(expression)
            checkXssVulnerability(expression)
            checkCommandInjection(expression)
            checkPathTraversal(expression)
            checkUnsafeDeserialization(expression)
            checkWeakCrypto(expression)
        }

        override fun visitNewExpression(expression: PsiNewExpression) {
            super.visitNewExpression(expression)
            checkInsecureSocket(expression)
        }

        // ==================== SQL 注入检测 ====================
        private fun checkSqlInjection(call: PsiMethodCallExpression) {
            val method = call.resolveMethod() ?: return
            val className = method.containingClass?.qualifiedName ?: return
            val methodName = method.name

            val sqlClasses = setOf(
                "java.sql.Statement", "java.sql.PreparedStatement",
                "java.sql.Connection", "javax.sql.DataSource"
            )

            if (className in sqlClasses && methodName in setOf("executeQuery", "execute", "executeUpdate")) {
                val args = call.argumentList.expressions
                if (args.isNotEmpty()) {
                    val arg = args.first()
                    if (containsStringConcat(arg) || containsVariableReference(arg)) {
                        registerFinding(
                            call, "SAST-SQL-001",
                            "SQL注入风险：检测到动态SQL拼接，建议使用参数化查询",
                            "HIGH", "SQL注入"
                        )
                    }
                }
            }
        }

        // ==================== XSS 检测 ====================
        private fun checkXssVulnerability(call: PsiMethodCallExpression) {
            val method = call.resolveMethod() ?: return
            val className = method.containingClass?.qualifiedName ?: return
            val methodName = method.name

            val xssPatterns = mapOf(
                "javax.servlet.jsp.JspWriter" to setOf("print", "write", "println"),
                "javax.servlet.http.HttpServletResponse" to setOf("getWriter"),
                "java.io.PrintWriter" to setOf("print", "write", "println")
            )

            xssPatterns[className]?.let { methods ->
                if (methodName in methods) {
                    val args = call.argumentList.expressions
                    if (args.any { containsUserInput(it) }) {
                        registerFinding(
                            call, "SAST-XSS-001",
                            "XSS风险：用户输入未经过滤直接输出到页面",
                            "HIGH", "XSS跨站脚本"
                        )
                    }
                }
            }
        }

        // ==================== 命令注入检测 ====================
        private fun checkCommandInjection(call: PsiMethodCallExpression) {
            val method = call.resolveMethod() ?: return
            val className = method.containingClass?.qualifiedName ?: return
            val methodName = method.name

            if (className == "java.lang.Runtime" && methodName == "exec") {
                val args = call.argumentList.expressions
                if (args.isNotEmpty() && containsVariableReference(args.first())) {
                    registerFinding(
                        call, "SAST-CMD-001",
                        "命令注入风险：检测到动态命令执行，建议使用白名单校验",
                        "CRITICAL", "命令注入"
                    )
                }
            }
        }

        // ==================== 路径遍历检测 ====================
        private fun checkPathTraversal(call: PsiMethodCallExpression) {
            val method = call.resolveMethod() ?: return
            val className = method.containingClass?.qualifiedName ?: return
            val methodName = method.name

            if (className in setOf("java.io.File", "java.io.FileInputStream", "java.io.FileOutputStream")
                && methodName in setOf("<init>", "File")) {
                val args = call.argumentList.expressions
                if (args.isNotEmpty() && containsUserInput(args.first())) {
                    registerFinding(
                        call, "SAST-PATH-001",
                        "路径遍历风险：文件路径包含用户输入，建议校验路径合法性",
                        "HIGH", "路径遍历"
                    )
                }
            }
        }

        // ==================== 不安全反序列化检测 ====================
        private fun checkUnsafeDeserialization(call: PsiMethodCallExpression) {
            val method = call.resolveMethod() ?: return
            val className = method.containingClass?.qualifiedName ?: return

            if (className == "java.io.ObjectInputStream") {
                registerFinding(
                    call, "SAST-DESER-001",
                    "不安全反序列化：使用ObjectInputStream可能导致RCE漏洞",
                    "CRITICAL", "不安全反序列化"
                )
            }
        }

        // ==================== 弱加密检测 ====================
        private fun checkWeakCrypto(call: PsiMethodCallExpression) {
            val method = call.resolveMethod() ?: return
            val className = method.containingClass?.qualifiedName ?: return
            val methodName = method.name

            val weakCryptoClasses = mapOf(
                "javax.crypto.Cipher" to setOf("getInstance"),
                "java.security.MessageDigest" to setOf("getInstance"),
                "javax.net.ssl.SSLContext" to setOf("getInstance")
            )

            if (className in weakCryptoClasses && methodName in (weakCryptoClasses[className] ?: emptySet())) {
                val args = call.argumentList.expressions
                if (args.isNotEmpty()) {
                    val algo = args.first().text.lowercase()
                    val weakAlgos = setOf("des", "rc4", "md5", "sha1", "tlsv1", "sslv3")
                    if (weakAlgos.any { algo.contains(it) }) {
                        registerFinding(
                            call, "SAST-CRYPTO-001",
                            "弱加密算法：检测到不安全的加密算法 $algo，建议使用AES-256/SHA-256等",
                            "MEDIUM", "弱加密"
                        )
                    }
                }
            }
        }

        // ==================== 不安全Socket检测 ====================
        private fun checkInsecureSocket(expr: PsiNewExpression) {
            val className = expr.classReference?.qualifiedName ?: return
            if (className in setOf("java.net.Socket", "java.net.ServerSocket")) {
                registerFinding(
                    expr, "SAST-NET-001",
                    "不安全网络连接：使用明文Socket通信，建议使用SSLSocket",
                    "MEDIUM", "不安全通信"
                )
            }
        }

        // ==================== 辅助方法 ====================

        private fun containsStringConcat(expr: PsiExpression): Boolean {
            return expr.text.contains("+") && expr.text.contains("\"")
        }

        private fun containsVariableReference(expr: PsiExpression): Boolean {
            return expr is PsiReferenceExpression || expr.text.contains("+")
        }

        private fun containsUserInput(expr: PsiExpression): Boolean {
            val text = expr.text.lowercase()
            val userInputPatterns = setOf(
                "getparameter", "getheader", "getinputstream",
                "request.get", "getreader", "getline"
            )
            return userInputPatterns.any { text.contains(it) } || containsVariableReference(expr)
        }

        private fun registerFinding(
            element: PsiElement,
            ruleId: String,
            description: String,
            severity: String,
            category: String
        ) {
            // 在IDE中显示警告
            val highlightType = when (severity) {
                "CRITICAL" -> ProblemHighlightType.GENERIC_ERROR
                "HIGH" -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                "MEDIUM" -> ProblemHighlightType.WEAK_WARNING
                else -> ProblemHighlightType.INFORMATION
            }

            holder.registerProblem(element, "[DevSecAI] $description", highlightType)

            // 收集 Finding 上送
            val file = element.containingFile
            val doc = file.viewProvider.document
            val lineNumber = doc?.getLineNumber(element.textOffset)?.plus(1) ?: 0

            val finding = LocalFinding(
                ruleId = ruleId,
                module = "SAST",
                severity = severity,
                title = description.substringBefore(":").substringBefore("\uff1a"),
                description = description,
                filePath = file.virtualFile.path,
                startLine = lineNumber,
                endLine = lineNumber
            )

            FindingCollector.getInstance(element.project).addFinding(finding)
        }
    }
}
