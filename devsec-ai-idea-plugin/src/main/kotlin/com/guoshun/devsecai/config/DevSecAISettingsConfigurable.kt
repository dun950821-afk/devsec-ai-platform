package com.guoshun.devsecai.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import javax.swing.*

class DevSecAISettingsConfigurable : Configurable {

    private var serverUrlField: JTextField = JTextField()
    private var accessTokenField: JPasswordField = JPasswordField()
    private var pluginIdField: JTextField = JTextField()
    private var enableSCACheckBox: JCheckBox = JCheckBox("SCA - 开源组件风险检测")
    private var enableSASTCheckBox: JCheckBox = JCheckBox("SAST - 代码安全检测")
    private var enableSecretsCheckBox: JCheckBox = JCheckBox("Secrets - 敏感信息检测")
    private var enableBaselineCheckBox: JCheckBox = JCheckBox("Baseline - 企业安全基线检测")
    private var enableAICheckBox: JCheckBox = JCheckBox("AI - 智能分析")
    private var autoScanOnSaveCheckBox: JCheckBox = JCheckBox("保存文件时自动扫描")
    private var autoUploadCheckBox: JCheckBox = JCheckBox("自动上传检测结果")
    private var gitCommitCheckCheckBox: JCheckBox = JCheckBox("启用 Git 提交前安全检查")
    private var blockCriticalCheckBox: JCheckBox = JCheckBox("发现严重风险时阻断提交")
    private var blockHighCheckBox: JCheckBox = JCheckBox("发现高危风险时阻断提交")
    private var blockMediumCheckBox: JCheckBox = JCheckBox("发现中危风险时阻断提交")
    private var blockLowCheckBox: JCheckBox = JCheckBox("发现低危风险时阻断提交")
    private var criticalHighlightCombo: JComboBox<String> = JComboBox(arrayOf("红色错误", "黄色告警"))
    private var highHighlightCombo: JComboBox<String> = JComboBox(arrayOf("红色错误", "黄色告警"))
    private var mediumHighlightCombo: JComboBox<String> = JComboBox(arrayOf("红色错误", "黄色告警"))
    private var lowHighlightCombo: JComboBox<String> = JComboBox(arrayOf("红色错误", "黄色告警"))
    private var infoHighlightCombo: JComboBox<String> = JComboBox(arrayOf("红色错误", "黄色告警"))
    private var testConnectionBtn: JButton = JButton("测试连接")

    override fun createComponent(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val serverPanel = JPanel()
        serverPanel.layout = BoxLayout(serverPanel, BoxLayout.Y_AXIS)
        serverPanel.border = BorderFactory.createTitledBorder("平台连接配置")
        serverPanel.add(JLabel("平台地址："))
        serverPanel.add(serverUrlField)
        serverPanel.add(JLabel("访问令牌："))
        serverPanel.add(accessTokenField)
        serverPanel.add(JLabel("插件实例 ID："))
        serverPanel.add(pluginIdField)
        serverPanel.add(testConnectionBtn)
        panel.add(serverPanel)

        val detectionPanel = JPanel()
        detectionPanel.layout = BoxLayout(detectionPanel, BoxLayout.Y_AXIS)
        detectionPanel.border = BorderFactory.createTitledBorder("检测能力")
        detectionPanel.add(enableSCACheckBox)
        detectionPanel.add(enableSASTCheckBox)
        detectionPanel.add(enableSecretsCheckBox)
        detectionPanel.add(enableBaselineCheckBox)
        detectionPanel.add(enableAICheckBox)
        panel.add(detectionPanel)

        val scanPanel = JPanel()
        scanPanel.layout = BoxLayout(scanPanel, BoxLayout.Y_AXIS)
        scanPanel.border = BorderFactory.createTitledBorder("扫描设置")
        scanPanel.add(autoScanOnSaveCheckBox)
        scanPanel.add(autoUploadCheckBox)
        scanPanel.add(gitCommitCheckCheckBox)
        panel.add(scanPanel)

        val blockPanel = JPanel()
        blockPanel.layout = BoxLayout(blockPanel, BoxLayout.Y_AXIS)
        blockPanel.border = BorderFactory.createTitledBorder("提交阻断策略")
        blockPanel.add(blockCriticalCheckBox)
        blockPanel.add(blockHighCheckBox)
        blockPanel.add(blockMediumCheckBox)
        blockPanel.add(blockLowCheckBox)
        panel.add(blockPanel)

        val highlightPanel = JPanel()
        highlightPanel.layout = BoxLayout(highlightPanel, BoxLayout.Y_AXIS)
        highlightPanel.border = BorderFactory.createTitledBorder("编辑器下划线级别")
        highlightPanel.add(row("严重风险：", criticalHighlightCombo))
        highlightPanel.add(row("高危风险：", highHighlightCombo))
        highlightPanel.add(row("中危风险：", mediumHighlightCombo))
        highlightPanel.add(row("低危风险：", lowHighlightCombo))
        highlightPanel.add(row("提示信息：", infoHighlightCombo))
        panel.add(highlightPanel)

        testConnectionBtn.addActionListener {
            testConnection()
        }

        reset()
        return panel
    }

    private fun testConnection() {
        val url = serverUrlField.text.trim()
        if (url.isEmpty()) {
            Messages.showWarningDialog("请输入平台地址", "测试连接")
            return
        }
        try {
            val connection = java.net.URL(url + "/api/plugin-instance/policy").openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val code = connection.responseCode
            if (code in 200..399) {
                Messages.showInfoMessage("连接成功（HTTP $code）", "测试连接")
            } else {
                Messages.showWarningDialog("平台返回 HTTP $code", "测试连接")
            }
            connection.disconnect()
        } catch (e: Exception) {
            Messages.showErrorDialog("连接失败：${e.message}", "测试连接")
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
                blockLowCheckBox.isSelected != settings.blockLow ||
                levelValue(criticalHighlightCombo) != settings.criticalHighlightLevel ||
                levelValue(highHighlightCombo) != settings.highHighlightLevel ||
                levelValue(mediumHighlightCombo) != settings.mediumHighlightLevel ||
                levelValue(lowHighlightCombo) != settings.lowHighlightLevel ||
                levelValue(infoHighlightCombo) != settings.infoHighlightLevel
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
        settings.criticalHighlightLevel = levelValue(criticalHighlightCombo)
        settings.highHighlightLevel = levelValue(highHighlightCombo)
        settings.mediumHighlightLevel = levelValue(mediumHighlightCombo)
        settings.lowHighlightLevel = levelValue(lowHighlightCombo)
        settings.infoHighlightLevel = levelValue(infoHighlightCombo)
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
        criticalHighlightCombo.selectedItem = displayValue(settings.criticalHighlightLevel)
        highHighlightCombo.selectedItem = displayValue(settings.highHighlightLevel)
        mediumHighlightCombo.selectedItem = displayValue(settings.mediumHighlightLevel)
        lowHighlightCombo.selectedItem = displayValue(settings.lowHighlightLevel)
        infoHighlightCombo.selectedItem = displayValue(settings.infoHighlightLevel)
    }

    override fun getDisplayName(): String = "DevSecAI 安全开发助手"

    override fun getPreferredFocusedComponent(): JComponent = serverUrlField

    private fun row(label: String, component: JComponent): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(JLabel(label))
        panel.add(component)
        return panel
    }

    private fun levelValue(comboBox: JComboBox<String>): String {
        return if (comboBox.selectedItem == "红色错误") "ERROR" else "WARNING"
    }

    private fun displayValue(value: String): String {
        return if (value == "ERROR") "红色错误" else "黄色告警"
    }
}
