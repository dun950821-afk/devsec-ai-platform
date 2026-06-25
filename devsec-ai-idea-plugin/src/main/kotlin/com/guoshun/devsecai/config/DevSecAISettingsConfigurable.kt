package com.guoshun.devsecai.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import javax.swing.*

class DevSecAISettingsConfigurable : Configurable {

    private var serverUrlField: JTextField = JTextField()
    private var accessTokenField: JPasswordField = JPasswordField()
    private var pluginIdField: JTextField = JTextField()
    private var enableSCACheckBox: JCheckBox = JCheckBox("SCA - Software Composition Analysis")
    private var enableSASTCheckBox: JCheckBox = JCheckBox("SAST - Static Application Security Testing")
    private var enableSecretsCheckBox: JCheckBox = JCheckBox("Secrets - Hardcoded Secret Detection")
    private var enableBaselineCheckBox: JCheckBox = JCheckBox("Baseline - Security Baseline Check")
    private var enableAICheckBox: JCheckBox = JCheckBox("AI - Intelligent Analysis")
    private var autoScanOnSaveCheckBox: JCheckBox = JCheckBox("Auto scan on file save")
    private var autoUploadCheckBox: JCheckBox = JCheckBox("Auto upload findings")
    private var gitCommitCheckCheckBox: JCheckBox = JCheckBox("Git commit security check")
    private var blockCriticalCheckBox: JCheckBox = JCheckBox("Block commit on Critical findings")
    private var blockHighCheckBox: JCheckBox = JCheckBox("Block commit on High findings")
    private var blockMediumCheckBox: JCheckBox = JCheckBox("Block commit on Medium findings")
    private var blockLowCheckBox: JCheckBox = JCheckBox("Block commit on Low findings")
    private var testConnectionBtn: JButton = JButton("Test Connection")

    override fun createComponent(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        // Server settings
        val serverPanel = JPanel()
        serverPanel.layout = BoxLayout(serverPanel, BoxLayout.Y_AXIS)
        serverPanel.border = BorderFactory.createTitledBorder("Server Settings")
        serverPanel.add(JLabel("Server URL:"))
        serverPanel.add(serverUrlField)
        serverPanel.add(JLabel("Access Token:"))
        serverPanel.add(accessTokenField)
        serverPanel.add(JLabel("Plugin ID:"))
        serverPanel.add(pluginIdField)
        serverPanel.add(testConnectionBtn)
        panel.add(serverPanel)

        // Detection modules
        val detectionPanel = JPanel()
        detectionPanel.layout = BoxLayout(detectionPanel, BoxLayout.Y_AXIS)
        detectionPanel.border = BorderFactory.createTitledBorder("Detection Modules")
        detectionPanel.add(enableSCACheckBox)
        detectionPanel.add(enableSASTCheckBox)
        detectionPanel.add(enableSecretsCheckBox)
        detectionPanel.add(enableBaselineCheckBox)
        detectionPanel.add(enableAICheckBox)
        panel.add(detectionPanel)

        // Scan settings
        val scanPanel = JPanel()
        scanPanel.layout = BoxLayout(scanPanel, BoxLayout.Y_AXIS)
        scanPanel.border = BorderFactory.createTitledBorder("Scan Settings")
        scanPanel.add(autoScanOnSaveCheckBox)
        scanPanel.add(autoUploadCheckBox)
        scanPanel.add(gitCommitCheckCheckBox)
        panel.add(scanPanel)

        // Git commit blocking
        val blockPanel = JPanel()
        blockPanel.layout = BoxLayout(blockPanel, BoxLayout.Y_AXIS)
        blockPanel.border = BorderFactory.createTitledBorder("Git Commit Blocking Rules")
        blockPanel.add(blockCriticalCheckBox)
        blockPanel.add(blockHighCheckBox)
        blockPanel.add(blockMediumCheckBox)
        blockPanel.add(blockLowCheckBox)
        panel.add(blockPanel)

        testConnectionBtn.addActionListener {
            testConnection()
        }

        reset()
        return panel
    }

    private fun testConnection() {
        val url = serverUrlField.text.trim()
        if (url.isEmpty()) {
            Messages.showWarningDialog("Please enter server URL", "Test Connection")
            return
        }
        try {
            val connection = java.net.URL(url + "/api/plugin-instance/policy").openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val code = connection.responseCode
            if (code in 200..399) {
                Messages.showInfoMessage("Connection successful (HTTP $code)", "Test Connection")
            } else {
                Messages.showWarningDialog("Server responded with HTTP $code", "Test Connection")
            }
            connection.disconnect()
        } catch (e: Exception) {
            Messages.showErrorDialog("Connection failed: ${e.message}", "Test Connection")
        }
    }

    override fun isModified(): Boolean {
        val settings = DevSecAISettings.getInstance()
        return serverUrlField.text != settings.serverUrl ||
                String(accessTokenField.password) != settings.accessToken ||
                pluginIdField.text != settings.pluginId ||
                enableSCACheckBox.isSelected != settings.enableSCA ||
                enableSASTCheckBox.isSelected != settings.enableSAST ||
                enableSecretsCheckBox.isSelected != settings.enableSecrets ||
                enableBaselineCheckBox.isSelected != settings.enableBaseline ||
                enableAICheckBox.isSelected != settings.enableAI ||
                autoScanOnSaveCheckBox.isSelected != settings.autoScanOnSave ||
                autoUploadCheckBox.isSelected != settings.autoUploadFindings ||
                gitCommitCheckCheckBox.isSelected != settings.gitCommitCheck ||
                blockCriticalCheckBox.isSelected != settings.blockCritical ||
                blockHighCheckBox.isSelected != settings.blockHigh ||
                blockMediumCheckBox.isSelected != settings.blockMedium ||
                blockLowCheckBox.isSelected != settings.blockLow
    }

    override fun apply() {
        val settings = DevSecAISettings.getInstance()
        settings.serverUrl = serverUrlField.text.trim()
        settings.accessToken = String(accessTokenField.password)
        settings.pluginId = pluginIdField.text.trim()
        settings.enableSCA = enableSCACheckBox.isSelected
        settings.enableSAST = enableSASTCheckBox.isSelected
        settings.enableSecrets = enableSecretsCheckBox.isSelected
        settings.enableBaseline = enableBaselineCheckBox.isSelected
        settings.enableAI = enableAICheckBox.isSelected
        settings.autoScanOnSave = autoScanOnSaveCheckBox.isSelected
        settings.autoUploadFindings = autoUploadCheckBox.isSelected
        settings.gitCommitCheck = gitCommitCheckCheckBox.isSelected
        settings.blockCritical = blockCriticalCheckBox.isSelected
        settings.blockHigh = blockHighCheckBox.isSelected
        settings.blockMedium = blockMediumCheckBox.isSelected
        settings.blockLow = blockLowCheckBox.isSelected
    }

    override fun reset() {
        val settings = DevSecAISettings.getInstance()
        serverUrlField.text = settings.serverUrl
        accessTokenField.text = settings.accessToken
        pluginIdField.text = settings.pluginId
        enableSCACheckBox.isSelected = settings.enableSCA
        enableSASTCheckBox.isSelected = settings.enableSAST
        enableSecretsCheckBox.isSelected = settings.enableSecrets
        enableBaselineCheckBox.isSelected = settings.enableBaseline
        enableAICheckBox.isSelected = settings.enableAI
        autoScanOnSaveCheckBox.isSelected = settings.autoScanOnSave
        autoUploadCheckBox.isSelected = settings.autoUploadFindings
        gitCommitCheckCheckBox.isSelected = settings.gitCommitCheck
        blockCriticalCheckBox.isSelected = settings.blockCritical
        blockHighCheckBox.isSelected = settings.blockHigh
        blockMediumCheckBox.isSelected = settings.blockMedium
        blockLowCheckBox.isSelected = settings.blockLow
    }

    override fun getDisplayName(): String = "DevSecAI Security"

    override fun getPreferredFocusedComponent(): JComponent = serverUrlField
}
