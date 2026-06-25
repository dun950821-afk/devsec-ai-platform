package com.guoshun.devsecai.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "com.guoshun.devsecai.DevSecAISettings",
    storages = [Storage("devsecai.xml")]
)
class DevSecAISettings : PersistentStateComponent<DevSecAISettings.State> {

    private var state = State()

    data class State(
        var serverUrl: String = "http://localhost:8080",
        var accessToken: String = "",
        var pluginId: String = "devsecai-idea-plugin",
        var enableSCA: Boolean = false,
        var enableSAST: Boolean = true,
        var enableSecrets: Boolean = true,
        var enableBaseline: Boolean = false,
        var enableAI: Boolean = false,
        var autoScanOnSave: Boolean = true,
        var autoUploadFindings: Boolean = true,
        var gitCommitCheck: Boolean = true,
        var blockCritical: Boolean = true,
        var blockHigh: Boolean = true,
        var blockMedium: Boolean = false,
        var blockLow: Boolean = false
    )

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var serverUrl: String
        get() = state.serverUrl
        set(value) { state.serverUrl = value }

    var accessToken: String
        get() = state.accessToken
        set(value) { state.accessToken = value }

    var pluginId: String
        get() = state.pluginId
        set(value) { state.pluginId = value }

    var enableSCA: Boolean
        get() = state.enableSCA
        set(value) { state.enableSCA = value }

    var enableSAST: Boolean
        get() = state.enableSAST
        set(value) { state.enableSAST = value }

    var enableSecrets: Boolean
        get() = state.enableSecrets
        set(value) { state.enableSecrets = value }

    var enableBaseline: Boolean
        get() = state.enableBaseline
        set(value) { state.enableBaseline = value }

    var enableAI: Boolean
        get() = state.enableAI
        set(value) { state.enableAI = value }

    var autoScanOnSave: Boolean
        get() = state.autoScanOnSave
        set(value) { state.autoScanOnSave = value }

    var autoUploadFindings: Boolean
        get() = state.autoUploadFindings
        set(value) { state.autoUploadFindings = value }

    var gitCommitCheck: Boolean
        get() = state.gitCommitCheck
        set(value) { state.gitCommitCheck = value }

    var blockCritical: Boolean
        get() = state.blockCritical
        set(value) { state.blockCritical = value }

    var blockHigh: Boolean
        get() = state.blockHigh
        set(value) { state.blockHigh = value }

    var blockMedium: Boolean
        get() = state.blockMedium
        set(value) { state.blockMedium = value }

    var blockLow: Boolean
        get() = state.blockLow
        set(value) { state.blockLow = value }

    companion object {
        fun getInstance(): DevSecAISettings {
            return ApplicationManager.getApplication().getService(DevSecAISettings::class.java)
        }
    }
}
