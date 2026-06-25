package com.guoshun.devsecai.service

import com.guoshun.devsecai.model.FindingItem
import com.guoshun.devsecai.model.LocalFinding
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@Service(Service.Level.PROJECT)
class FindingCollector(private val project: Project) {

    private val logger = Logger.getInstance(FindingCollector::class.java)
    private val findings = ConcurrentLinkedQueue<LocalFinding>()
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
        findings.add(finding)
    }

    fun getFindings(): List<LocalFinding> {
        return findings.toList()
    }

    fun getPendingCount(): Int {
        return findings.size
    }

    fun clearFindings() {
        findings.clear()
    }

    fun flush() {
        if (findings.isEmpty()) return

        val batch = mutableListOf<LocalFinding>()
        while (batch.size < 10 && findings.isNotEmpty()) {
            findings.poll()?.let { batch.add(it) }
        }

        if (batch.isEmpty()) return

        // Group by module and upload
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
            val success = client.uploadFindings(module, findingItems)
            if (success) {
                logger.info("Uploaded ${findingItems.size} findings for module $module")
            } else {
                logger.warn("Failed to upload findings for module $module, re-queuing")
                findings.addAll(items)
            }
        }
    }
}
