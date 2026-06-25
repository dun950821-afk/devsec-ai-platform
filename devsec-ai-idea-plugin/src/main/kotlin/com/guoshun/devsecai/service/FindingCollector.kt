package com.guoshun.devsecai.service

import com.guoshun.devsecai.model.FindingItem
import com.guoshun.devsecai.model.LocalFinding
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class FindingCollector(private val project: Project) {

    private val logger = Logger.getInstance(FindingCollector::class.java)
    private val findings = ConcurrentLinkedQueue<LocalFinding>()
    private val findingKeys = ConcurrentHashMap.newKeySet<String>()
    private val client = DevSecAIClient()
    private var uploadTimer: Timer? = null

    fun start() {
        stop()
        uploadTimer = Timer("DevSecAI-FindingUpload", true)
        uploadTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                flush()
            }
        }, 30 * 1000L, 30 * 1000L) // every 30 seconds
        logger.info("Finding collector started")
    }

    fun stop() {
        flush()
        uploadTimer?.cancel()
        uploadTimer = null
        logger.info("Finding collector stopped")
    }

    fun addFinding(finding: LocalFinding) {
        val key = finding.key()
        if (findingKeys.add(key)) {
            findings.add(finding)
        }
    }

    fun addFindings(newFindings: Collection<LocalFinding>) {
        newFindings.forEach { addFinding(it) }
    }

    fun getFindings(): List<LocalFinding> {
        return findings.toList()
    }

    fun getPendingCount(): Int {
        return findings.size
    }

    fun clearFindings() {
        findings.clear()
        findingKeys.clear()
    }

    fun flush(): UploadResult {
        if (findings.isEmpty()) return UploadResult(uploaded = 0, failed = 0, pending = 0)

        val batch = mutableListOf<LocalFinding>()
        while (batch.size < 10 && findings.isNotEmpty()) {
            findings.poll()?.let {
                findingKeys.remove(it.key())
                batch.add(it)
            }
        }

        if (batch.isEmpty()) return UploadResult(uploaded = 0, failed = 0, pending = findings.size)

        // Group by module and upload
        var uploaded = 0
        var failed = 0
        val failureReasons = mutableListOf<String>()
        val grouped = batch.groupBy { it.module }
        for ((module, items) in grouped) {
            val findingItems = items.map { local ->
                FindingItem(
                    ruleId = local.ruleId,
                    severity = local.severity,
                    title = local.title,
                    description = local.description,
                    filePath = local.filePath,
                    startLine = local.startLine,
                    endLine = local.endLine,
                    componentName = local.componentName,
                    currentVersion = local.currentVersion,
                    fixedVersion = local.fixedVersion,
                    vulnerabilityId = local.vulnerabilityId,
                    confidence = local.confidence,
                    recommendation = local.recommendation,
                    cwe = local.cwe,
                    owasp = local.owasp
                )
            }
            val upload = client.uploadFindings(module, findingItems)
            if (upload.successful) {
                logger.info("Uploaded ${findingItems.size} findings for module $module")
                uploaded += findingItems.size
            } else {
                val reason = upload.message.ifBlank { "未知错误" }
                logger.warn("Failed to upload findings for module $module, re-queuing: $reason")
                addFindings(items)
                failed += findingItems.size
                failureReasons.add("$module：$reason")
            }
        }
        return UploadResult(uploaded = uploaded, failed = failed, pending = findings.size, failureReasons = failureReasons)
    }

    data class UploadResult(
        val uploaded: Int,
        val failed: Int,
        val pending: Int,
        val failureReasons: List<String> = emptyList()
    )

    private fun LocalFinding.key(): String = listOf(ruleId, module, severity, filePath, startLine, endLine, componentName ?: "")
        .joinToString("|")
}
