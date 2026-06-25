package com.guoshun.devsecai.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * DevSecAI 插件持久化配置
 */
@State(
    name = "com.guoshun.devsecai.DevSecAISettings",
    storages = [Storage("devsecai.xml")]
)
class DevSecAISettings : PersistentStateComponent<DevSecAISettings> {

    var serverUrl: String = "http://localhost:8080"
    var accessToken: String = ""
    var pluginId: String = ""
    var developerName: String = ""
    var autoScanOnSave: Boolean = true
    var autoScanOnCommit: Boolean = true
    var blockCriticalOnCommit: Boolean = true
    var blockHighOnCommit: Boolean = true
    var uploadFindings: Boolean = true
    var heartbeatIntervalMinutes: Int = 5

    // 运行时状态（不持久化）
    @Transient
    var connected: Boolean = false

    @Transient
    var projectName: String? = null

    @Transient
    var policyId: String? = null

    override fun getState(): DevSecAISettings = this

    override fun loadState(state: DevSecAISettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): DevSecAISettings =
            ApplicationManager.getApplication().getService(DevSecAISettings::class.java)
    }

    fun isConfigured(): Boolean = serverUrl.isNotBlank() && accessToken.isNotBlank()

    fun getPluginIdOrGenerate(): String {
        if (pluginId.isBlank()) {
            pluginId = "IDEA-PLG-${System.currentTimeMillis().toString(16).uppercase().takeLast(6).padStart(6, '0')}"
        }
        return pluginId
    }
}
