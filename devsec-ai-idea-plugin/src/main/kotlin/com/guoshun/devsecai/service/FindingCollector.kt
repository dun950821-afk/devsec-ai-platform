package com.guoshun.devsecai.service

import com.guoshun.devsecai.model.LocalFinding
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.util.concurrent.*

/**
 * Finding 收集器
 * 收集所有检测引擎的发现，批量上送到管理平台
 */
class FindingCollector(private val project: Project) {

    private val logger = Logger.getInstance(FindingCollector::class.java)
    private val client = DevSecAIClient()
    private val buffer = ConcurrentLinkedQueue<LocalFinding>()
    private val localFindings = CopyOnWriteArrayList<LocalFinding>()
    private val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "DevSecAI-FindingUploader").apply { isDaemon = true }
    }

    fun start() {
        // 每30秒批量上送一次
        scheduler.scheduleWithFixedDelay({ flush() }, 30, 30, TimeUnit.SECONDS)
        logger.info("FindingCollector started")
    }

    fun stop() {
        scheduler.shutdown()
        flush() // 最终上送
        logger.info("FindingCollector stopped")
    }

    /**
     * 添加一个 Finding 到缓冲区
     */
    fun addFinding(finding: LocalFinding) {
        buffer.add(finding)
        localFindings.add(finding)
        // 如果缓冲区超过100条，立即上送
        if (buffer.size >= 100) {
            CompletableFuture.runAsync { flush() }
        }
    }

    /**
     * 批量添加 Finding
     */
    fun addFindings(findings: List<LocalFinding>) {
        buffer.addAll(findings)
        localFindings.addAll(findings)
        if (buffer.size >= 100) {
            CompletableFuture.runAsync { flush() }
        }
    }

    /**
     * 将缓冲区的 Finding 上送到管理平台
     */
    @Synchronized
    fun flush() {
        if (buffer.isEmpty()) return

        val batch = mutableListOf<LocalFinding>()
        while (buffer.isNotEmpty() && batch.size < 200) {
            buffer.poll()?.let { batch.add(it) }
        }

        if (batch.isEmpty()) return

        // 按 module 分组上送
        batch.groupBy { it.module }.forEach { (module, findings) ->
            try {
                val response = client.uploadFindings(findings, module)
                if (response.code == 200) {
                    logger.info("Uploaded ${findings.size} findings for module: $module")
                    findings.forEach { it.uploaded = true }
                } else {
                    logger.warn("Upload findings failed: ${response.message}")
                    // 上送失败，重新加入缓冲区
                    buffer.addAll(findings)
                }
            } catch (e: Exception) {
                logger.warn("Upload findings error: ${e.message}")
                buffer.addAll(findings)
            }
        }
    }

    /**
     * 获取所有本地 Finding（供 ToolWindow 和 Actions 使用）
     */
    fun getFindings(): List<LocalFinding> = localFindings.toList()

    /**
     * 获取未上传的 Finding
     */
    fun getPendingFindings(): List<LocalFinding> = localFindings.filter { !it.uploaded }

    /**
     * 获取缓冲区大小
     */
    fun getBufferSize(): Int = buffer.size

    /**
     * 清空本地 Finding（用于重新扫描前清空旧结果）
     */
    fun clearFindings() {
        localFindings.clear()
        buffer.clear()
    }

    companion object {
        fun getInstance(project: Project): FindingCollector =
            project.getService(FindingCollector::class.java)
    }
}
