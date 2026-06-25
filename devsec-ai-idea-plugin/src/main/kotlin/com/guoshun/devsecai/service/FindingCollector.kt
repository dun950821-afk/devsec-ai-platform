package com.guoshun.devsecai.service

import com.guoshun.devsecai.model.FindingItem
import com.guoshun.devsecai.model.LocalFinding
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.*

@Service(Service.Level.PROJECT)
class FindingCollector(private val project: Project) {

    companion object {
        private val LOG = Logger.getInstance(FindingCollector::class.java)
        private const val BATCH_SIZE = 10
        private const val FLUSH_INTERVAL_MS = 30_000L
    }

    private val findings = ConcurrentLinkedQueue<FindingItem>()
    private var flushTimer: Timer? = null
    private var autoUploadEnabled = true

    fun startAutoUpload() {
        stopAutoUpload()
        autoUploadEnabled = true
        flushTimer = Timer("DevSecAI-FindingFlush", true)
        flushTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    if (project.isDisposed) {
                        stopAutoUpload()
                        return
                    }
                    flushIfNeeded()
                } catch (e: Exception) {
                    LOG.warn("Auto-flush error: ${e.message}")
                }
            }
        }, FLUSH_INTERVAL_MS, FLUSH_INTERVAL_MS)
        LOG.info("Finding auto-upload started")
    }

    fun stopAutoUpload() {
        autoUploadEnabled = false
        flushTimer?.cancel()
        flushTimer = null
        LOG.info("Finding auto-upload stopped")
    }

    fun addFinding(finding: FindingItem) {
        findings.add(finding)
        LOG.info("Finding added: [${finding.severity}] ${finding.title} at ${finding.filePath}:${finding.startLine}")
        if (autoUploadEnabled && findings.size >= BATCH_SIZE) {
            flushIfNeeded()
        }
    }

    fun addFinding(finding: LocalFinding) {
        addFinding(finding.toFindingItem())
    }

    fun addFindings(newFindings: List<FindingItem>) {
        findings.addAll(newFindings)
        LOG.info("Added ${newFindings.size} findings, total: ${findings.size}")
        if (autoUploadEnabled && findings.size >= BATCH_SIZE) {
            flushIfNeeded()
        }
    }

    fun addLocalFindings(newFindings: List<LocalFinding>) {
        val items = newFindings.map { it.toFindingItem() }
        addFindings(items)
    }

    fun getLocalFindings(): List<LocalFinding> {
        return findings.map { it.toLocalFinding() }
    }

    fun getFindings(): List<FindingItem> {
        return findings.toList()
    }

    fun getPendingCount(): Int = findings.size

    fun clearFindings() {
        findings.clear()
        LOG.info("All local findings cleared")
    }

    fun flush(): ApiResult {
        val pending = mutableListOf<FindingItem>()
        while (findings.isNotEmpty()) {
            findings.poll()?.let { pending.add(it) }
        }

        if (pending.isEmpty()) {
            return ApiResult(successful = true, message = "No findings to upload")
        }

        val client = DevSecAIClient(project)
        val byModule = pending.groupBy { it.module }

        var allSuccess = true
        val errors = mutableListOf<String>()

        for ((module, moduleFindings) in byModule) {
            val result = client.uploadFindings(module, moduleFindings)
            if (!result.successful) {
                allSuccess = false
                errors.add("$module: ${result.message}")
                // Re-add failed findings
                findings.addAll(moduleFindings)
                LOG.warn("Upload failed for module $module: ${result.message}")
            } else {
                LOG.info("Uploaded ${moduleFindings.size} findings for module $module")
            }
        }

        return if (allSuccess) {
            ApiResult(successful = true, message = "Uploaded ${pending.size} findings")
        } else {
            ApiResult(successful = false, message = "Some uploads failed: ${errors.joinToString("; ")}")
        }
    }

    private fun flushIfNeeded() {
        if (findings.isNotEmpty()) {
            flush()
        }
    }
}
