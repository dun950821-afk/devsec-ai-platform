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
                val reason = "SQL 语句存在字符串拼接，攻击者可能通过输入改变查询语义。"
                val recommendation = "使用 PreparedStatement 占位符绑定参数，避免将用户输入拼接进 SQL。"
                val message = DevSecAIInspectionSupport.problemMessage("SQL 注入风险", reason, recommendation)
                holder.registerProblem(method, message, DevSecAIInspectionSupport.highlightType("HIGH"),
                        *DevSecAIInspectionSupport.fixes("SQL 注入", recommendation))
                addFinding(project, "SAST-SQL-INJECTION", "HIGH", "SQL 注入",
                        "检测到 SQL 字符串拼接，建议使用 PreparedStatement 参数化查询。",
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
                        val reason = "未净化的用户输入直接写入响应，可能在浏览器中执行恶意脚本。"
                        val recommendation = "对输出内容进行 HTML/JavaScript 上下文编码，避免直接输出 request 参数。"
                        val message = DevSecAIInspectionSupport.problemMessage("XSS 风险", reason, recommendation)
                        holder.registerProblem(expression, message, DevSecAIInspectionSupport.highlightType("HIGH"),
                                *DevSecAIInspectionSupport.fixes("跨站脚本（XSS）", recommendation))
                        addFinding(project, "SAST-XSS", "HIGH", "跨站脚本（XSS）",
                                "未净化的用户输入直接写入输出内容，可能造成跨站脚本风险。",
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
                val reason = "系统命令中包含用户输入，攻击者可能拼接额外命令。"
                val recommendation = "避免拼接命令字符串；对参数做白名单校验，并使用 ProcessBuilder 参数数组传递。"
                val message = DevSecAIInspectionSupport.problemMessage("命令注入风险", reason, recommendation)
                holder.registerProblem(method, message, DevSecAIInspectionSupport.highlightType("CRITICAL"),
                        *DevSecAIInspectionSupport.fixes("命令注入", recommendation))
                addFinding(project, "SAST-CMD-INJECTION", "CRITICAL", "命令注入",
                        "系统命令执行中使用了用户输入，可能造成命令注入。",
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
            val reason = "文件路径中包含用户输入，攻击者可能通过 ../ 访问非授权文件。"
            val recommendation = "使用 Path.normalize/canonical path 校验，限制访问目录，并拒绝包含 ../ 的输入。"
            val message = DevSecAIInspectionSupport.problemMessage("路径穿越风险", reason, recommendation)
            holder.registerProblem(method, message, DevSecAIInspectionSupport.highlightType("HIGH"),
                    *DevSecAIInspectionSupport.fixes("路径穿越", recommendation))
            addFinding(project, "SAST-PATH-TRAVERSAL", "HIGH", "路径穿越",
                    "文件路径拼接中使用了用户输入，可能造成路径穿越。",
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
                val reason = "源码中硬编码密码、密钥或令牌，代码泄露后会导致凭据失控。"
                val recommendation = "删除源码中的固定密钥，改为从环境变量、配置中心或密钥管理服务读取。"
                val message = DevSecAIInspectionSupport.problemMessage("硬编码密码风险", reason, recommendation)
                holder.registerProblem(expression, message, DevSecAIInspectionSupport.highlightType("MEDIUM"),
                        *DevSecAIInspectionSupport.fixes("硬编码密码", recommendation, "\"***REMOVED_SECRET***\""))
                addFinding(project, "SAST-HARDCODED-PASSWORD", "MEDIUM", "硬编码密码",
                        "源码中发现硬编码密码或密钥，建议改为安全配置或密钥管理服务。",
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
                val reason = "$algo 属于弱算法或不安全模式，可能被碰撞、破解或缺少认证保护。"
                val recommendation = "将 $algo 替换为符合场景的安全算法，例如哈希使用 SHA-256+，对称加密使用 AES/GCM/NoPadding。"
                val message = DevSecAIInspectionSupport.problemMessage("弱加密风险", reason, recommendation)
                val replacement = if (algo.contains("AES", ignoreCase = true) || algo.contains("DES", ignoreCase = true)) {
                    "\"AES/GCM/NoPadding\""
                } else {
                    "\"SHA-256\""
                }
                holder.registerProblem(expression, message, DevSecAIInspectionSupport.highlightType("MEDIUM"),
                        *DevSecAIInspectionSupport.fixes("弱加密算法", recommendation, replacement))
                addFinding(project, "SAST-WEAK-CRYPTO", "MEDIUM", "弱加密算法",
                        "检测到不安全的加密算法或模式：$algo。",
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
            val reason = "ObjectInputStream 反序列化不可信输入时可能触发任意对象构造或代码执行链。"
            val recommendation = "避免 ObjectInputStream 处理不可信输入；如必须使用，请增加类白名单和输入边界校验。"
            val message = DevSecAIInspectionSupport.problemMessage("不安全反序列化风险", reason, recommendation)
            holder.registerProblem(expression, message, DevSecAIInspectionSupport.highlightType("HIGH"),
                    *DevSecAIInspectionSupport.fixes("不安全反序列化", recommendation))
            addFinding(project, "SAST-UNSAFE-DESERIALIZATION", "HIGH", "不安全反序列化",
                    "检测到 ObjectInputStream.readObject()，不可信输入可能导致代码执行风险。",
                    expression.containingFile?.virtualFile?.path ?: "",
                    getLineNumber(expression), getLineNumber(expression))
        }
    }

    private fun checkInsecureSocket(expression: PsiMethodCallExpression, holder: ProblemsHolder, project: Project) {
        val methodText = expression.methodExpression.referenceName ?: ""
        val qualifier = expression.methodExpression.qualifierExpression?.text ?: ""

        if ((methodText == "Socket" || methodText == "ServerSocket") &&
                !qualifier.contains("SSLSocket") && !qualifier.contains("SSLServerSocket")) {
            val reason = "使用普通 Socket 传输数据，网络中间人可能窃听或篡改通信内容。"
            val recommendation = "改用 SSLSocket/SSLServerSocket 或上层 TLS 通道，避免明文传输敏感数据。"
            val message = DevSecAIInspectionSupport.problemMessage("不安全 Socket 风险", reason, recommendation)
            holder.registerProblem(expression, message, DevSecAIInspectionSupport.highlightType("MEDIUM"),
                    *DevSecAIInspectionSupport.fixes("不安全 Socket", recommendation))
            addFinding(project, "SAST-INSECURE-SOCKET", "MEDIUM", "不安全 Socket",
                    "检测到非 SSL Socket 通信，建议改用加密连接。",
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
}
