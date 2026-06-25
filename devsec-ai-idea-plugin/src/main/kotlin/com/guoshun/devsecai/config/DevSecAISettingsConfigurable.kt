package com.guoshun.devsecai.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

/**
 * DevSecAI 插件设置界面
 */
class DevSecAISettingsConfigurable : Configurable {

    private var serverUrlField: JBTextField = JBTextField()
    private var accessTokenField: JBPasswordField = JBPasswordField()
    private var developerField: JBTextField = JBTextField()
    private var autoScanOnSaveCheckBox: JCheckBox = JCheckBox("保存文件时自动扫描")
    private var autoScanOnCommitCheckBox: JCheckBox = JCheckBox("提交代码时自动扫描")
    private var blockCriticalCheckBox: JCheckBox = JCheckBox("阻断严重级别漏洞提交")
    private var blockHighCheckBox: JCheckBox = JCheckBox("阻断高危级别漏洞提交")
    private var uploadFindingsCheckBox: JCheckBox = JCheckBox("自动上送检测结果")

    private val settings: DevSecAISettings
        get() = DevSecAISettings.getInstance()

    override fun getDisplayName(): String = "DevSecAI 安全助手"

    override fun createComponent(): JComponent {
        val formBuilder = FormBuilder.createFormBuilder()
            .addSeparator()
            .addComponent(JLabel("服务器连接配置").apply { font = font.deriveFont(java.awt.Font.BOLD) })
            .addLabeledComponent("服务器地址:", serverUrlField)
            .addLabeledComponent("访问令牌 (Token):", accessTokenField)
            .addLabeledComponent("开发者名称:", developerField)
            .addSeparator()
            .addComponent(JLabel("检测策略").apply { font = font.deriveFont(java.awt.Font.BOLD) })
            .addComponent(autoScanOnSaveCheckBox)
            .addComponent(autoScanOnCommitCheckBox)
            .addComponent(blockCriticalCheckBox)
            .addComponent(blockHighCheckBox)
            .addComponent(uploadFindingsCheckBox)
            .addSeparator()
            .addComponent(JLabel("<html><body style='color:gray;font-size:11px'>访问令牌请在管理平台 Token 管理页面生成。<br>插件ID: ${settings.getPluginIdOrGenerate()}</body></html>"))

        serverUrlField.emptyText.text = "http://localhost:8080"
        accessTokenField.preferredSize = Dimension(400, accessTokenField.preferredSize.height)

        reset()
        return formBuilder.panel
    }

    override fun isModified(): Boolean {
        return serverUrlField.text != settings.serverUrl ||
                String(accessTokenField.password) != settings.accessToken ||
                developerField.text != settings.developerName ||
                autoScanOnSaveCheckBox.isSelected != settings.autoScanOnSave ||
                autoScanOnCommitCheckBox.isSelected != settings.autoScanOnCommit ||
                blockCriticalCheckBox.isSelected != settings.blockCriticalOnCommit ||
                blockHighCheckBox.isSelected != settings.blockHighOnCommit ||
                uploadFindingsCheckBox.isSelected != settings.uploadFindings
    }

    override fun apply() {
        settings.serverUrl = serverUrlField.text.trim().removeSuffix("/")
        settings.accessToken = String(accessTokenField.password)
        settings.developerName = developerField.text.trim()
        settings.autoScanOnSave = autoScanOnSaveCheckBox.isSelected
        settings.autoScanOnCommit = autoScanOnCommitCheckBox.isSelected
        settings.blockCriticalOnCommit = blockCriticalCheckBox.isSelected
        settings.blockHighOnCommit = blockHighCheckBox.isSelected
        settings.uploadFindings = uploadFindingsCheckBox.isSelected
    }

    override fun reset() {
        serverUrlField.text = settings.serverUrl
        accessTokenField.text = settings.accessToken
        developerField.text = settings.developerName
        autoScanOnSaveCheckBox.isSelected = settings.autoScanOnSave
        autoScanOnCommitCheckBox.isSelected = settings.autoScanOnCommit
        blockCriticalCheckBox.isSelected = settings.blockCriticalOnCommit
        blockHighCheckBox.isSelected = settings.blockHighOnCommit
        uploadFindingsCheckBox.isSelected = settings.uploadFindings
    }
}
