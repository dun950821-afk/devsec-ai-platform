package com.guoshun.devsecai.inspection

import com.guoshun.devsecai.model.LocalFinding
import com.guoshun.devsecai.service.FindingCollector
import com.guoshun.devsecai.service.PolicyService
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.*

class SecurityInspectionTool : LocalInspectionTool() {

    private val logger = Logger.getInstance(SecurityInspectionTool::class.java)

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val project = holder.project

        // Only use cached policy - NEVER trigger network I/O in PSI path
        val policyService = project.getService(PolicyService::class.java)
        if (!policyService.isSastEnabledCached()) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        return object : JavaElementVisitor() {

            override fun visitMethod(method: PsiMethod) {
                super.visitMethod(method)
                checkSqlInjection(method, holder, project)
                checkCommandInjection(method, holder, project)
                checkPathTraversal(method, holder, project)
            }

            override fun visitLiteralExpression(expression: PsiLiteralExpression) {
                super.visitLiteralExpression(expression)
                checkHardcodedPassword(expression, holder, project)
                checkWeakCrypto(expression, holder, project)
            }

            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                checkXss(expression, holder, project)
                checkUnsafeDeserialization(expression, holder, project)
                checkInsecureSocket(expression, holder, project)
            }
        }
    }

    private fun checkSqlInjection(method: PsiMethod, holder: ProblemsHolder, project: Project) {
        val body = method.body ?: return
        val text = body.text

        if (text.contains("executeQuery") || text.contains("executeUpdate") || text.contains("prepareStatement")) {
            if (text.contains("+") && (text.contains("SELECT") || text.contains("INSERT") ||
                            text.contains("UPDATE") || text.contains("DELETE") || text.contains("WHERE"))) {
                val message = "SQL Injection: String concatenation in SQL query. Use PreparedStatement instead."
                holder.registerProblem(method, message)
                addFinding(project, "SAST-SQL-INJECTION", "HIGH", "SQL Injection",
                        "String concatenation detected in SQL query. Use parameterized queries.",
                        method.containingFile?.virtualFile?.path ?: "",
                        getLineNumber(method), getLineNumber(method))
            }
        }
    }

    private fun checkXss(expression: PsiMethodCallExpression, holder: ProblemsHolder, project: Project) {
        val methodText = expression.methodExpression.qualifierExpression?.text ?: ""
        val methodName = expression.methodExpression.referenceName ?: ""

        if (methodName == "println" || methodName == "print" || methodName == "write") {
            if (methodText.contains("Writer") || methodText.contains("writer") ||
                    methodText.contains("getWriter") || methodText.contains("out")) {
                val args = expression.argumentList.expressions
                if (args.isNotEmpty()) {
                    val argText = args[0].text
                    if (argText.contains("getParameter") || argText.contains("getParameterValues")) {
                        val message = "XSS: Unsanitized user input written to output. Apply output encoding."
                        holder.registerProblem(expression, message)
                        addFinding(project, "SAST-XSS", "HIGH", "Cross-Site Scripting (XSS)",
                                "Unsanitized user input written directly to output.",
                                expression.containingFile?.virtualFile?.path ?: "",
                                getLineNumber(expression), getLineNumber(expression))
                    }
                }
            }
        }
    }

    private fun checkCommandInjection(method: PsiMethod, holder: ProblemsHolder, project: Project) {
        val body = method.body ?: return
        val text = body.text

        if (text.contains("Runtime.getRuntime().exec(") || text.contains("ProcessBuilder(")) {
            if (text.contains("+") || text.contains("getParameter")) {
                val message = "Command Injection: User input in OS command. Use allowlist and parameterized execution."
                holder.registerProblem(method, message)
                addFinding(project, "SAST-CMD-INJECTION", "CRITICAL", "Command Injection",
                        "User input used in OS command execution.",
                        method.containingFile?.virtualFile?.path ?: "",
                        getLineNumber(method), getLineNumber(method))
            }
        }
    }

    private fun checkPathTraversal(method: PsiMethod, holder: ProblemsHolder, project: Project) {
        val body = method.body ?: return
        val text = body.text

        if ((text.contains("FileInputStream") || text.contains("FileOutputStream") ||
                        text.contains("File(")) && text.contains("getParameter")) {
            val message = "Path Traversal: User input used in file path. Validate and canonicalize paths."
            holder.registerProblem(method, message)
            addFinding(project, "SAST-PATH-TRAVERSAL", "HIGH", "Path Traversal",
                    "User input used in file path construction.",
                    method.containingFile?.virtualFile?.path ?: "",
                    getLineNumber(method), getLineNumber(method))
        }
    }

    private fun checkHardcodedPassword(expression: PsiLiteralExpression, holder: ProblemsHolder, project: Project) {
        val text = expression.text
        val parent = expression.parent

        if (parent is PsiNameValuePair) {
            val name = parent.name
            if (name == "password" || name == "secret" || name == "apiKey") {
                val message = "Hardcoded Password: Avoid hardcoding passwords in source code."
                holder.registerProblem(expression, message)
                addFinding(project, "SAST-HARDCODED-PASSWORD", "MEDIUM", "Hardcoded Password",
                        "Password/secret hardcoded in source code.",
                        expression.containingFile?.virtualFile?.path ?: "",
                        getLineNumber(expression), getLineNumber(expression))
            }
        }
    }

    private fun checkWeakCrypto(expression: PsiLiteralExpression, holder: ProblemsHolder, project: Project) {
        val text = expression.text
        val weakAlgorithms = listOf("DES", "MD5", "SHA-1", "RC4", "RC2", "AES/ECB")

        for (algo in weakAlgorithms) {
            if (text.contains(algo)) {
                val message = "Weak Cryptography: $algo is insecure. Use AES-GCM, SHA-256+ or Argon2."
                holder.registerProblem(expression, message)
                addFinding(project, "SAST-WEAK-CRYPTO", "MEDIUM", "Weak Cryptography",
                        "Insecure cryptographic algorithm: $algo",
                        expression.containingFile?.virtualFile?.path ?: "",
                        getLineNumber(expression), getLineNumber(expression))
                break
            }
        }
    }

    private fun checkUnsafeDeserialization(expression: PsiMethodCallExpression, holder: ProblemsHolder, project: Project) {
        val methodText = expression.methodExpression.referenceName ?: ""
        val qualifier = expression.methodExpression.qualifierExpression?.text ?: ""

        if (methodText == "readObject" && qualifier.contains("ObjectInputStream")) {
            val message = "Unsafe Deserialization: Untrusted data deserialization. Use allowlist or JSON."
            holder.registerProblem(expression, message)
            addFinding(project, "SAST-UNSAFE-DESERIALIZATION", "HIGH", "Unsafe Deserialization",
                    "ObjectInputStream.readObject() on untrusted data.",
                    expression.containingFile?.virtualFile?.path ?: "",
                    getLineNumber(expression), getLineNumber(expression))
        }
    }

    private fun checkInsecureSocket(expression: PsiMethodCallExpression, holder: ProblemsHolder, project: Project) {
        val methodText = expression.methodExpression.referenceName ?: ""
        val qualifier = expression.methodExpression.qualifierExpression?.text ?: ""

        if ((methodText == "Socket" || methodText == "ServerSocket") &&
                !qualifier.contains("SSLSocket") && !qualifier.contains("SSLServerSocket")) {
            val message = "Insecure Socket: Non-SSL socket detected. Use SSLSocket for encrypted communication."
            holder.registerProblem(expression, message)
            addFinding(project, "SAST-INSECURE-SOCKET", "MEDIUM", "Insecure Socket",
                    "Non-SSL socket usage detected.",
                    expression.containingFile?.virtualFile?.path ?: "",
                    getLineNumber(expression), getLineNumber(expression))
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
                    module = "SAST"
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
}
