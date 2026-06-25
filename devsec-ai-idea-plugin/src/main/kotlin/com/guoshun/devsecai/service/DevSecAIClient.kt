package com.guoshun.devsecai.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guoshun.devsecai.config.DevSecAISettings
import com.guoshun.devsecai.model.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class ApiResult(val successful: Boolean, val message: String, val data: String = "")

class DevSecAIClient(private val project: Project) {

    companion object {
        private val LOG = Logger.getInstance(DevSecAIClient::class.java)
        private val gson = Gson()
    }

    private fun getSettings(): DevSecAISettings {
        return DevSecAISettings.getInstance()
    }

    private fun getBaseUrl(): String {
        return getSettings().serverUrl.trimEnd('/')
    }

    private fun getAccessToken(): String {
        return getSettings().accessToken
    }

    private fun getPluginId(): String {
        return getSettings().pluginId
    }

    /**
     * Create HttpURLConnection with common headers including accessToken
     */
    private fun createConnection(url: URL, method: String): HttpURLConnection {
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = 10000
        conn.readTimeout = 15000
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.setRequestProperty("Accept", "application/json")

        // Send accessToken header for authentication
        val token = getAccessToken()
        if (token.isNotEmpty()) {
            conn.setRequestProperty("X-Access-Token", token)
        }

        return conn
    }

    /**
     * Handshake with the platform server
     */
    fun handshake(): HandshakeResult {
        val settings = getSettings()
        val url = URL("${getBaseUrl()}/api/plugin-instance/handshake")

        val ideVersion = ApplicationManager.getApplication().let { app ->
            com.intellij.openapi.util.BuildNumber.fromString(
                app.getVersion() ?: "2026.1"
            )?.asStringWithoutProductCode() ?: "2026.1"
        }

        val body = mapOf(
            "pluginId" to getPluginId(),
            "pluginVersion" to "1.0.0",
            "ideName" to "IDEA",
            "ideVersion" to ideVersion,
            "machineId" to getMachineId(),
            "developer" to System.getProperty("user.name", "unknown"),
            "accessToken" to getAccessToken()
        )

        return try {
            val conn = createConnection(url, "POST")
            conn.doOutput = true
            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(gson.toJson(body))
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                LOG.info("Handshake successful: $response")
                val resultType = object : TypeToken<Map<String, Any>>() {}.type
                val result = gson.fromJson<Map<String, Any>>(response, resultType)
                val data = result["data"] as? Map<String, Any>
                HandshakeResult(
                    successful = true,
                    message = "Handshake successful",
                    projectId = data?.get("projectId") as? String ?: "",
                    projectName = data?.get("projectName") as? String ?: "",
                    policyId = data?.get("policyId") as? String ?: "",
                    enabledModules = (data?.get("enabledModules") as? List<String>) ?: emptyList()
                )
            } else {
                val errorBody = try { conn.errorStream?.bufferedReader()?.use { it.readText() } } catch (_: Exception) { "" }
                LOG.warn("Handshake failed: HTTP $responseCode - $errorBody")
                HandshakeResult(
                    successful = false,
                    message = "HTTP $responseCode: ${errorBody.take(200)}"
                )
            }
        } catch (e: Exception) {
            LOG.warn("Handshake exception", e)
            HandshakeResult(successful = false, message = "Connection failed: ${e.message}")
        }
    }

    /**
     * Send heartbeat to the platform server
     */
    fun heartbeat(): ApiResult {
        val url = URL("${getBaseUrl()}/api/plugin-instance/heartbeat")
        val body = mapOf(
            "pluginId" to getPluginId(),
            "status" to "ONLINE"
        )
        return post(url, body)
    }

    /**
     * Fetch policy configuration from the platform server
     */
    fun fetchPolicy(): PolicyResult {
        val url = URL("${getBaseUrl()}/api/plugin-instance/policy?pluginId=${getPluginId()}")

        return try {
            val conn = createConnection(url, "GET")
            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val resultType = object : TypeToken<Map<String, Any>>() {}.type
                val result = gson.fromJson<Map<String, Any>>(response, resultType)
                val data = result["data"] as? Map<String, Any> ?: emptyMap()
                LOG.info("Policy fetched successfully")
                PolicyResult(successful = true, message = "OK", policyData = data)
            } else {
                val errorBody = try { conn.errorStream?.bufferedReader()?.use { it.readText() } } catch (_: Exception) { "" }
                LOG.warn("Policy fetch failed: HTTP $responseCode")
                PolicyResult(successful = false, message = "HTTP $responseCode: $errorBody")
            }
        } catch (e: Exception) {
            LOG.warn("Policy fetch exception", e)
            PolicyResult(successful = false, message = "Connection failed: ${e.message}")
        }
    }

    /**
     * Upload findings to the platform server
     */
    fun uploadFindings(module: String, findings: List<FindingItem>): ApiResult {
        val url = URL("${getBaseUrl()}/api/finding/upload")
        val body = mapOf(
            "pluginId" to getPluginId(),
            "module" to module,
            "findings" to findings
        )
        LOG.info("Uploading ${findings.size} findings for module $module to $url")
        val result = post(url, body)
        LOG.info("Upload result: successful=${result.successful}, message=${result.message}")
        return result
    }

    private fun post(url: URL, body: Any): ApiResult {
        return try {
            val conn = createConnection(url, "POST")
            conn.doOutput = true
            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(gson.toJson(body))
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                ApiResult(successful = true, message = "OK", data = response)
            } else {
                val errorBody = try { conn.errorStream?.bufferedReader()?.use { it.readText() } } catch (_: Exception) { "" }
                LOG.warn("POST $url failed: HTTP $responseCode - $errorBody")
                ApiResult(successful = false, message = "HTTP $responseCode: ${errorBody.take(200)}")
            }
        } catch (e: Exception) {
            LOG.warn("POST $url exception", e)
            ApiResult(successful = false, message = "Connection failed: ${e.message}")
        }
    }

    private fun getMachineId(): String {
        return try {
            val prefs = com.intellij.openapi.util.JDOMExternalizer.getInstance()
            val id = prefs?.getValue("DevSecAI.MachineId")
            if (id.isNullOrEmpty()) {
                val newId = java.util.UUID.randomUUID().toString().replace("-", "").take(16)
                prefs?.setValue("DevSecAI.MachineId", newId)
                newId
            } else {
                id
            }
        } catch (e: Exception) {
            val fallback = java.util.UUID.randomUUID().toString().replace("-", "").take(16)
            LOG.warn("Failed to get machine ID, using fallback", e)
            fallback
        }
    }
}

data class HandshakeResult(
    val successful: Boolean,
    val message: String,
    val projectId: String = "",
    val projectName: String = "",
    val policyId: String = "",
    val enabledModules: List<String> = emptyList()
)

data class PolicyResult(
    val successful: Boolean,
    val message: String,
    val policyData: Map<String, Any> = emptyMap()
)
