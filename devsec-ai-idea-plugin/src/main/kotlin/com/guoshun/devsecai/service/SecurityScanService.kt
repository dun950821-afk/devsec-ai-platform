package com.guoshun.devsecai.service

import com.guoshun.devsecai.model.LocalFinding
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import java.util.Locale

@Service(Service.Level.PROJECT)
class SecurityScanService(private val project: Project) {

    fun scanProject(indicator: ProgressIndicator? = null, clearPrevious: Boolean = true): ScanSummary {
        val collector = project.getService(FindingCollector::class.java)
        if (clearPrevious) {
            collector.clearFindings()
        }

        val files = collectProjectFiles()
        val findings = mutableListOf<LocalFinding>()
        val total = files.size.coerceAtLeast(1)

        files.forEachIndexed { index, file ->
            if (indicator?.isCanceled == true || project.isDisposed) {
                return@forEachIndexed
            }
            indicator?.fraction = (index + 1).toDouble() / total
            indicator?.text = "正在扫描 ${file.name}"
            findings.addAll(scanFile(file))
        }

        collector.addFindings(findings)
        return ScanSummary(filesScanned = files.size, findingsFound = findings.size)
    }

    fun scanFiles(files: Collection<VirtualFile>, clearPrevious: Boolean = false): ScanSummary {
        val collector = project.getService(FindingCollector::class.java)
        if (clearPrevious) {
            collector.clearFindings()
        }
        val findings = files.flatMap { scanFile(it) }
        collector.addFindings(findings)
        return ScanSummary(filesScanned = files.size, findingsFound = findings.size)
    }

    private fun collectProjectFiles(): List<VirtualFile> {
        val roots = ProjectRootManager.getInstance(project).contentRoots
        val result = mutableListOf<VirtualFile>()
        roots.forEach { collectSupportedFiles(it, result) }
        return result
    }

    private fun collectSupportedFiles(file: VirtualFile, result: MutableList<VirtualFile>) {
        if (file.name in ignoredDirectories) return
        if (file.isDirectory) {
            file.children.forEach { collectSupportedFiles(it, result) }
            return
        }
        val name = file.name.lowercase(Locale.ROOT)
        val ext = file.extension?.lowercase(Locale.ROOT)
        if (ext in sourceExtensions || name in dependencyFiles) {
            result.add(file)
        }
    }

    private fun scanFile(file: VirtualFile): List<LocalFinding> {
        val text = runCatching { String(file.contentsToByteArray(), file.charset) }.getOrNull() ?: return emptyList()
        val ext = file.extension?.lowercase(Locale.ROOT)
        val name = file.name.lowercase(Locale.ROOT)
        val findings = mutableListOf<LocalFinding>()

        if (ext in setOf("java", "kt", "kts")) {
            findings.addAll(scanSast(file, text))
        }
        if (ext in sourceExtensions) {
            findings.addAll(scanSecrets(file, text))
        }
        if (name in dependencyFiles) {
            findings.addAll(scanDependencies(file, text))
        }

        return findings
    }

    private fun scanSast(file: VirtualFile, text: String): List<LocalFinding> {
        val findings = mutableListOf<LocalFinding>()
        val lines = text.lines()
        lines.forEachIndexed { idx, line ->
            val lineNo = idx + 1
            val normalized = line.lowercase(Locale.ROOT)
            if (line.contains("executeQuery(") && line.contains("+")) {
                findings.add(file.finding("SAST-SQL-INJECTION", "HIGH", "SQL 注入",
                    "SQL 语句使用字符串拼接构造，建议改为参数化查询。", lineNo, "SAST"))
            }
            if ((line.contains("Runtime.getRuntime().exec(") || line.contains("ProcessBuilder(")) &&
                (line.contains("+") || normalized.contains("request.") || normalized.contains("getparameter"))) {
                findings.add(file.finding("SAST-CMD-INJECTION", "CRITICAL", "命令注入",
                    "命令执行中疑似包含动态输入，请使用白名单并避免 Shell 拼接。", lineNo, "SAST"))
            }
            if (weakCrypto.any { normalized.contains(it) }) {
                findings.add(file.finding("SAST-WEAK-CRYPTO", "MEDIUM", "弱加密算法",
                    "检测到弱加密算法或模式，建议使用现代认证加密和 SHA-256+。", lineNo, "SAST"))
            }
            if (normalized.contains("objectinputstream") || normalized.contains(".readobject(")) {
                findings.add(file.finding("SAST-UNSAFE-DESERIALIZATION", "HIGH", "不安全反序列化",
                    "Java 对象反序列化处理不可信输入时可能导致代码执行风险。", lineNo, "SAST"))
            }
        }
        return findings
    }

    private fun scanSecrets(file: VirtualFile, text: String): List<LocalFinding> {
        val findings = mutableListOf<LocalFinding>()
        val lines = text.lines()
        lines.forEachIndexed { idx, line ->
            val lineNo = idx + 1
            secretPatterns.forEach { pattern ->
                if (pattern.regex.containsMatchIn(line) && !isPlaceholder(line)) {
                    findings.add(file.finding(pattern.ruleId, pattern.severity, pattern.title,
                        "检测到${pattern.title}，请立即轮换并迁移到密钥管理系统。", lineNo, "SECRETS"))
                }
            }
        }
        return findings
    }

    private fun scanDependencies(file: VirtualFile, text: String): List<LocalFinding> {
        val findings = mutableListOf<LocalFinding>()
        vulnerableDependencies.forEach { dep ->
            if (text.contains(dep.name, ignoreCase = true) && dep.versionRegex.containsMatchIn(text)) {
                findings.add(LocalFinding(
                    ruleId = dep.ruleId,
                    severity = dep.severity,
                    title = dep.title,
                    description = dep.recommendation,
                    filePath = file.path,
                    startLine = findLine(text, dep.name),
                    endLine = findLine(text, dep.name),
                    module = "SCA",
                    componentName = dep.name,
                    currentVersion = dep.detectedVersion,
                    fixedVersion = dep.fixedVersion,
                    vulnerabilityId = dep.vulnerabilityId,
                    recommendation = dep.recommendation
                ))
            }
        }
        return findings
    }

    private fun VirtualFile.finding(
        ruleId: String,
        severity: String,
        title: String,
        description: String,
        line: Int,
        module: String
    ): LocalFinding = LocalFinding(
        ruleId = ruleId,
        severity = severity,
        title = title,
        description = description,
        filePath = path,
        startLine = line,
        endLine = line,
        module = module,
        recommendation = description
    )

    private fun findLine(text: String, token: String): Int {
        val index = text.lines().indexOfFirst { it.contains(token, ignoreCase = true) }
        return if (index >= 0) index + 1 else 1
    }

    private fun isPlaceholder(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT)
        return placeholderTokens.any { lower.contains(it) }
    }

    data class ScanSummary(val filesScanned: Int, val findingsFound: Int)

    private data class SecretPattern(
        val title: String,
        val regex: Regex,
        val severity: String,
        val ruleId: String
    )

    private data class VulnerableDependency(
        val name: String,
        val versionRegex: Regex,
        val detectedVersion: String,
        val fixedVersion: String,
        val vulnerabilityId: String,
        val severity: String,
        val ruleId: String,
        val title: String,
        val recommendation: String
    )

    companion object {
        private val ignoredDirectories = setOf(".git", ".gradle", ".idea", "build", "out", "target", "node_modules")
        private val sourceExtensions = setOf("java", "kt", "kts", "xml", "properties", "yml", "yaml", "json")
        private val dependencyFiles = setOf("pom.xml", "build.gradle", "build.gradle.kts", "gradle.lockfile")
        private val weakCrypto = setOf("md5", "sha-1", "sha1", "des/", "\"des\"", "rc4", "aes/ecb")
        private val placeholderTokens = setOf("example", "placeholder", "change-me", "changeme", "your-", "xxxx", "****", "dummy")

        private val secretPatterns = listOf(
            SecretPattern("AWS Access Key", Regex("""AKIA[0-9A-Z]{16}"""), "CRITICAL", "SECRETS-AWS-KEY"),
            SecretPattern("GitHub Token", Regex("""gh[ps]_[0-9A-Za-z]{36}"""), "CRITICAL", "SECRETS-GITHUB-TOKEN"),
            SecretPattern("私钥", Regex("""-----BEGIN (?:RSA |EC |DSA )?PRIVATE KEY-----"""), "CRITICAL", "SECRETS-PRIVATE-KEY"),
            SecretPattern("JWT Token", Regex("""eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+"""), "HIGH", "SECRETS-JWT"),
            SecretPattern("密码", Regex("""(?i)(password|passwd|pwd)\s*[:=]\s*['"][^'"]{8,}['"]"""), "HIGH", "SECRETS-PASSWORD"),
            SecretPattern("API Key", Regex("""(?i)(api[_-]?key|apikey|access[_-]?token|secret[_-]?key)\s*[:=]\s*['"][^'"]{12,}['"]"""), "HIGH", "SECRETS-API-KEY")
        )

        private val vulnerableDependencies = listOf(
            VulnerableDependency(
                name = "log4j-core",
                versionRegex = Regex("""log4j-core[^0-9]*(2\.(0|1[0-6])(\.[0-9]+)?)""", RegexOption.IGNORE_CASE),
                detectedVersion = "<= 2.16.x",
                fixedVersion = "2.17.1+",
                vulnerabilityId = "CVE-2021-44228",
                severity = "CRITICAL",
                ruleId = "SCA-LOG4J-001",
                title = "存在风险的 Log4j Core 版本",
                recommendation = "请升级 log4j-core 至 2.17.1 或更高版本。"
            ),
            VulnerableDependency(
                name = "fastjson",
                versionRegex = Regex("""fastjson[^0-9]*(1\.2\.[0-7][0-9])""", RegexOption.IGNORE_CASE),
                detectedVersion = "1.2.x",
                fixedVersion = "1.2.83+ or fastjson2",
                vulnerabilityId = "FASTJSON-AUTO-TYPE",
                severity = "HIGH",
                ruleId = "SCA-FASTJSON-001",
                title = "Fastjson 风险版本",
                recommendation = "请升级 Fastjson，并在非必要情况下关闭 autoType。"
            )
        )
    }
}
