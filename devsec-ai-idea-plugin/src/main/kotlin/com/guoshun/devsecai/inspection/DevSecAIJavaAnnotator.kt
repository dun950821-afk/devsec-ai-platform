package com.guoshun.devsecai.inspection

import com.guoshun.devsecai.config.DevSecAISettings
import com.guoshun.devsecai.service.PolicyService
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiNameValuePair

class DevSecAIJavaAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val settings = DevSecAISettings.getInstance()
        val policyService = element.project.getService(PolicyService::class.java)

        if (settings.enableSAST && policyService.isSastEnabledCached()) {
            when (element) {
                is PsiMethod -> {
                    checkSqlInjection(element, holder)
                    checkCommandInjection(element, holder)
                    checkPathTraversal(element, holder)
                }
                is PsiLiteralExpression -> {
                    checkHardcodedPassword(element, holder)
                    checkWeakCrypto(element, holder)
                }
                is PsiMethodCallExpression -> {
                    checkXss(element, holder)
                    checkUnsafeDeserialization(element, holder)
                    checkInsecureSocket(element, holder)
                }
            }
        }

        if (settings.enableSecrets && policyService.isSecretsEnabledCached()) {
            when (element) {
                is PsiLiteralExpression -> checkSecrets(element, element.text, holder)
                is PsiComment -> checkSecrets(element, element.text, holder)
            }
        }
    }

    private fun checkSqlInjection(method: PsiMethod, holder: AnnotationHolder) {
        val body = method.body ?: return
        val text = body.text
        if ((text.contains("executeQuery") || text.contains("executeUpdate") || text.contains("prepareStatement")) &&
            text.contains("+") &&
            listOf("SELECT", "INSERT", "UPDATE", "DELETE", "WHERE").any { text.contains(it) }) {
            val reason = "SQL 语句存在字符串拼接，攻击者可能通过输入改变查询语义。"
            val recommendation = "使用 PreparedStatement 占位符绑定参数，避免将用户输入拼接进 SQL。"
            annotate(method.nameIdentifier ?: method, "HIGH", "SQL 注入风险", reason, recommendation, holder)
        }
    }

    private fun checkXss(expression: PsiMethodCallExpression, holder: AnnotationHolder) {
        val methodText = expression.methodExpression.qualifierExpression?.text ?: ""
        val methodName = expression.methodExpression.referenceName ?: ""
        if (methodName !in setOf("println", "print", "write")) return
        if (!listOf("Writer", "writer", "getWriter", "out").any { methodText.contains(it) }) return
        val argText = expression.argumentList.expressions.firstOrNull()?.text ?: return
        if (argText.contains("getParameter") || argText.contains("getParameterValues")) {
            val reason = "未净化的用户输入直接写入响应，可能在浏览器中执行恶意脚本。"
            val recommendation = "对输出内容进行 HTML/JavaScript 上下文编码，避免直接输出 request 参数。"
            annotate(expression, "HIGH", "XSS 风险", reason, recommendation, holder)
        }
    }

    private fun checkCommandInjection(method: PsiMethod, holder: AnnotationHolder) {
        val body = method.body ?: return
        val text = body.text
        if ((text.contains("Runtime.getRuntime().exec(") || text.contains("ProcessBuilder(")) &&
            (text.contains("+") || text.contains("getParameter"))) {
            val reason = "系统命令中包含用户输入，攻击者可能拼接额外命令。"
            val recommendation = "避免拼接命令字符串；对参数做白名单校验，并使用 ProcessBuilder 参数数组传递。"
            annotate(method.nameIdentifier ?: method, "CRITICAL", "命令注入风险", reason, recommendation, holder)
        }
    }

    private fun checkPathTraversal(method: PsiMethod, holder: AnnotationHolder) {
        val body = method.body ?: return
        val text = body.text
        if ((text.contains("FileInputStream") || text.contains("FileOutputStream") || text.contains("File(")) &&
            text.contains("getParameter")) {
            val reason = "文件路径中包含用户输入，攻击者可能通过 ../ 访问非授权文件。"
            val recommendation = "使用 Path.normalize/canonical path 校验，限制访问目录，并拒绝包含 ../ 的输入。"
            annotate(method.nameIdentifier ?: method, "HIGH", "路径穿越风险", reason, recommendation, holder)
        }
    }

    private fun checkHardcodedPassword(expression: PsiLiteralExpression, holder: AnnotationHolder) {
        val parent = expression.parent
        if (parent is PsiNameValuePair && parent.name in setOf("password", "secret", "apiKey")) {
            val reason = "源码中硬编码密码、密钥或令牌，代码泄露后会导致凭据失控。"
            val recommendation = "删除源码中的固定密钥，改为从环境变量、配置中心或密钥管理服务读取。"
            annotate(expression, "MEDIUM", "硬编码密码风险", reason, recommendation, holder)
        }
    }

    private fun checkWeakCrypto(expression: PsiLiteralExpression, holder: AnnotationHolder) {
        val text = expression.text
        val algo = listOf("DES", "MD5", "SHA-1", "RC4", "RC2", "AES/ECB").firstOrNull { text.contains(it) } ?: return
        val reason = "$algo 属于弱算法或不安全模式，可能被碰撞、破解或缺少认证保护。"
        val recommendation = "将 $algo 替换为符合场景的安全算法，例如哈希使用 SHA-256+，对称加密使用 AES/GCM/NoPadding。"
        annotate(expression, "MEDIUM", "弱加密风险", reason, recommendation, holder)
    }

    private fun checkUnsafeDeserialization(expression: PsiMethodCallExpression, holder: AnnotationHolder) {
        val methodText = expression.methodExpression.referenceName ?: ""
        val qualifier = expression.methodExpression.qualifierExpression?.text ?: ""
        if (methodText == "readObject" && qualifier.contains("ObjectInputStream")) {
            val reason = "ObjectInputStream 反序列化不可信输入时可能触发任意对象构造或代码执行链。"
            val recommendation = "避免 ObjectInputStream 处理不可信输入；如必须使用，请增加类白名单和输入边界校验。"
            annotate(expression, "HIGH", "不安全反序列化风险", reason, recommendation, holder)
        }
    }

    private fun checkInsecureSocket(expression: PsiMethodCallExpression, holder: AnnotationHolder) {
        val methodText = expression.methodExpression.referenceName ?: ""
        val qualifier = expression.methodExpression.qualifierExpression?.text ?: ""
        if ((methodText == "Socket" || methodText == "ServerSocket") &&
            !qualifier.contains("SSLSocket") &&
            !qualifier.contains("SSLServerSocket")) {
            val reason = "使用普通 Socket 传输数据，网络中间人可能窃听或篡改通信内容。"
            val recommendation = "改用 SSLSocket/SSLServerSocket 或上层 TLS 通道，避免明文传输敏感数据。"
            annotate(expression, "MEDIUM", "不安全 Socket 风险", reason, recommendation, holder)
        }
    }

    private fun checkSecrets(element: PsiElement, text: String, holder: AnnotationHolder) {
        secretPatterns.forEach { pattern ->
            if (pattern.regex.containsMatchIn(text)) {
                val reason = "代码中出现了 ${pattern.name}，仓库、日志或构建产物泄露后会导致凭据暴露。"
                annotate(element, pattern.severity, "敏感信息泄露：${pattern.name}", reason, pattern.recommendation, holder)
            }
        }
    }

    private fun annotate(
        element: PsiElement,
        severity: String,
        title: String,
        reason: String,
        recommendation: String,
        holder: AnnotationHolder
    ) {
        val message = DevSecAIInspectionSupport.problemMessage(title, reason, recommendation)
        holder.newAnnotation(DevSecAIInspectionSupport.annotationSeverity(severity), message)
            .range(element)
            .tooltip(DevSecAIInspectionSupport.tooltip(message))
            .create()
    }

    private data class SecretPattern(
        val name: String,
        val regex: Regex,
        val severity: String,
        val recommendation: String
    )

    companion object {
        private val secretPatterns = listOf(
            SecretPattern("AWS Access Key", Regex("""AKIA[0-9A-Z]{16}"""), "CRITICAL", "检测到 AWS Access Key ID，请立即轮换密钥。"),
            SecretPattern("AWS Secret Key", Regex("""(?i)aws(.{0,20})?(?-i)['"][0-9a-zA-Z/+]{40}['"]"""), "CRITICAL", "检测到 AWS Secret Access Key，请立即轮换密钥。"),
            SecretPattern("GitHub Token", Regex("""gh[ps]_[0-9a-zA-Z]{36}"""), "CRITICAL", "检测到 GitHub Token，请吊销后重新生成。"),
            SecretPattern("JWT Token", Regex("""eyJ[A-Za-z0-9-_]+\.eyJ[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+"""), "HIGH", "源码中检测到 JWT Token，请改为环境变量或安全配置。"),
            SecretPattern("私钥", Regex("""-----BEGIN (?:RSA |EC |DSA )?PRIVATE KEY-----"""), "CRITICAL", "检测到私钥，请存放到安全密钥管理系统。"),
            SecretPattern("数据库密码", Regex("""(?i)(password|passwd|pwd)\s*[:=]\s*['"][^'"]{8,}['"]"""), "HIGH", "检测到数据库密码，请使用环境变量或密钥库。"),
            SecretPattern("通用 API Key", Regex("""(?i)(api[_-]?key|apikey)\s*[:=]\s*['"][^'"]{16,}['"]"""), "HIGH", "检测到 API Key，请使用环境变量或密钥库。"),
            SecretPattern("Slack Token", Regex("""xox[baprs]-[0-9]{10,}-[0-9a-zA-Z]{24,}"""), "HIGH", "检测到 Slack Token，请吊销后改用环境变量。")
        )
    }
}
