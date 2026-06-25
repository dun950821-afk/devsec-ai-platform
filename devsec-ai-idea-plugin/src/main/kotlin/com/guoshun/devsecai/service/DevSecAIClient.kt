package com.guoshun.devsecai.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guoshun.devsecai.config.DevSecAISettings
import com.guoshun.devsecai.model.*
import com.intellij.openapi.diagnostic.Logger
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * DevSecAI HTTP 客户端
 * 负责与后端管理平台的所有HTTP通信
 * 注意：这是一个普通类，不注册为 IntelliJ Service
 */
class DevSecAIClient {

    private val logger = Logger.getInstance(DevSecAIClient::class.java)
    private val gson = Gson()
    private val connectTimeout = 10000
    private val readTimeout = 30000

    private fun getSettings(): DevSecAISettings = DevSecAISettings.getInstance()

    private fun getBaseUrl(): String = getSettings().serverUrl.removeSuffix("/")

    /**
     * 握手认证
     */
    fun handshake(): HandshakeResponse {
        val settings = getSettings()
        val ideInfo = getIdeInfo()
        val request = HandshakeRequest(
            pluginId = settings.getPluginIdOrGenerate(),
            pluginVersion = PLUGIN_VERSION,
            ideName = ideInfo.first,
            ideVersion = ideInfo.second,
            machineId = getMachineId(),
            developer = settings.developerName,
            accessToken = settings.accessToken
        )
        return postJson("${getBaseUrl()}/api/plugin-instance/handshake", request, HandshakeResponse::class.java)
    }

    /**
     * 心跳上报
     */
    fun heartbeat(): ApiResponse {
        val settings = getSettings()
        val request = HeartbeatRequest(pluginId = settings.getPluginIdOrGenerate())
        return postJson("${getBaseUrl()}/api/plugin-instance/heartbeat", request, ApiResponse::class.java)
    }

    /**
     * 获取策略配置
     */
    fun getPolicy(): PolicyResponse {
        val settings = getSettings()
        val url = "${getBaseUrl()}/api/plugin-instance/policy?pluginId=${settings.getPluginIdOrGenerate()}"
        return getJson(url, PolicyResponse::class.java)
    }

    /**
     * 上送Finding结果
     */
    fun uploadFindings(findings: List<LocalFinding>, module: String): ApiResponse {
        val settings = getSettings()
        val items = findings.map { f ->
            FindingItem(
                ruleId = f.ruleId,
                severity = f.severity,
                title = f.title,
                description = f.description,
                filePath = f.filePath,
                startLine = f.startLine,
                endLine = f.endLine,
                componentName = f.componentName,
                currentVersion = f.currentVersion,
                fixedVersion = f.fixedVersion,
                vulnerabilityId = f.vulnerabilityId,
                recommendation = f.recommendation
            )
        }
        val request = FindingUploadRequest(
            pluginId = settings.getPluginIdOrGenerate(),
            module = module,
            findings = items
        )
        return postJson("${getBaseUrl()}/api/finding/upload", request, ApiResponse::class.java)
    }

    // ==================== HTTP 方法 ====================

    private fun <T> getJson(url: String, clazz: Class<T>): T {
        val conn = URL(url).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "GET"
            conn.connectTimeout = connectTimeout
            conn.readTimeout = readTimeout
            conn.setRequestProperty("Accept", "application/json")
            val body = conn.readBody()
            gson.fromJson(body, clazz)
        } catch (e: Exception) {
            logger.error("GET request failed: $url", e)
            throw e
        } finally {
            conn.disconnect()
        }
    }

    private fun <T> postJson(url: String, payload: Any, clazz: Class<T>): T {
        val conn = URL(url).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "POST"
            conn.connectTimeout = connectTimeout
            conn.readTimeout = readTimeout
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")
            val json = gson.toJson(payload)
            conn.outputStream.use { os ->
                os.write(json.toByteArray(StandardCharsets.UTF_8))
            }
            val body = conn.readBody()
            gson.fromJson(body, clazz)
        } catch (e: Exception) {
            logger.error("POST request failed: $url", e)
            throw e
        } finally {
            conn.disconnect()
        }
    }

    private fun HttpURLConnection.readBody(): String {
        val stream = if (responseCode in 200..299) inputStream else errorStream
        return stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() } ?: ""
    }

    companion object {
        const val PLUGIN_VERSION = "0.9.7"

        private fun getIdeInfo(): Pair<String, String> {
            val appInfo = com.intellij.openapi.application.ApplicationInfo.getInstance()
            val appName = appInfo.fullApplicationName
            val buildNumber = appInfo.build.toString()
            return when {
                appName.contains("IDEA", ignoreCase = true) -> "IntelliJ IDEA" to buildNumber
                appName.contains("WebStorm", ignoreCase = true) -> "WebStorm" to buildNumber
                appName.contains("PyCharm", ignoreCase = true) -> "PyCharm" to buildNumber
                appName.contains("GoLand", ignoreCase = true) -> "GoLand" to buildNumber
                else -> appName to buildNumber
            }
        }

        private fun getMachineId(): String {
            return try {
                val optionsDir = com.intellij.openapi.application.PathManager.getOptionsDir()
                val dirName = optionsDir.fileName.toString()
                "hash-${dirName.hashCode().toString(16).uppercase().take(8)}"
            } catch (e: Exception) {
                "hash-${System.getProperty("user.name", "unknown").hashCode().toString(16).uppercase().take(8)}"
            }
        }
    }
}
