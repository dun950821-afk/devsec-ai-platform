package com.guoshun.devsecai.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guoshun.devsecai.config.DevSecAISettings
import com.guoshun.devsecai.model.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class DevSecAIClient {

    private val gson = Gson()

    private fun getBaseUrl(): String {
        val url = DevSecAISettings.getInstance().serverUrl
        return if (url.endsWith("/")) url.dropLast(1) else url
    }

    private fun getPluginId(): String {
        return DevSecAISettings.getInstance().pluginId
    }

    private fun getAccessToken(): String {
        return DevSecAISettings.getInstance().accessToken
    }

    fun handshake(
        ideName: String,
        ideVersion: String,
        machineId: String,
        developer: String
    ): HandshakeResponse? {
        val url = URL("${getBaseUrl()}/api/plugin-instance/handshake")
        val request = HandshakeRequest(
            pluginId = getPluginId(),
            pluginVersion = "1.0.0",
            ideName = ideName,
            ideVersion = ideVersion,
            machineId = machineId,
            developer = developer,
            accessToken = getAccessToken()
        )
        return post(url, request, HandshakeResponse::class.java)
    }

    fun heartbeat(status: String, activeModules: List<String>?): Boolean {
        val url = URL("${getBaseUrl()}/api/plugin-instance/heartbeat")
        val request = HeartbeatRequest(
            pluginId = getPluginId(),
            status = status,
            activeModules = activeModules
        )
        return postWithoutResponse(url, request).successful
    }

    fun fetchPolicy(): PolicyData? {
        val url = URL("${getBaseUrl()}/api/plugin-instance/policy?pluginId=${getPluginId()}")
        val response = get(url, PolicyResponse::class.java)
        return response?.data
    }

    fun uploadFindings(module: String, findings: List<FindingItem>): ApiResult {
        val url = URL("${getBaseUrl()}/api/finding/upload")
        val request = FindingUploadRequest(
            pluginId = getPluginId(),
            module = module,
            findings = findings
        )
        return postWithoutResponse(url, request)
    }

    private fun <T> post(url: URL, body: Any, responseClass: Class<T>): T? {
        var connection: HttpURLConnection? = null
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 15000

            val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
            writer.write(gson.toJson(body))
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val response = reader.readText()
                reader.close()
                return gson.fromJson(response, responseClass)
            }
            return null
        } catch (e: Exception) {
            return null
        } finally {
            connection?.disconnect()
        }
    }

    private fun postWithoutResponse(url: URL, body: Any): ApiResult {
        var connection: HttpURLConnection? = null
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 15000

            val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
            writer.write(gson.toJson(body))
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            val responseBody = readBody(if (responseCode in 200..299) connection.inputStream else connection.errorStream)
            return if (responseCode in 200..299) {
                ApiResult(successful = true, httpCode = responseCode, message = responseBody.ifBlank { "请求成功" })
            } else {
                ApiResult(successful = false, httpCode = responseCode, message = responseBody.ifBlank { "平台返回 HTTP $responseCode" })
            }
        } catch (e: Exception) {
            return ApiResult(successful = false, message = e.message ?: e.javaClass.simpleName)
        } finally {
            connection?.disconnect()
        }
    }

    private fun <T> get(url: URL, responseClass: Class<T>): T? {
        var connection: HttpURLConnection? = null
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val response = reader.readText()
                reader.close()
                return gson.fromJson(response, responseClass)
            }
            return null
        } catch (e: Exception) {
            return null
        } finally {
            connection?.disconnect()
        }
    }

    private fun readBody(stream: InputStream?): String {
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }
    }

    data class ApiResult(
        val successful: Boolean,
        val httpCode: Int? = null,
        val message: String = ""
    )
}
