package com.guoshun.devsecai.ui

import com.guoshun.devsecai.config.DevSecAISettings
import com.guoshun.devsecai.model.FindingItem
import com.guoshun.devsecai.model.LocalFinding
import com.guoshun.devsecai.service.FindingCollector
import com.guoshun.devsecai.service.HeartbeatService
import com.guoshun.devsecai.service.PolicyService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.table.DefaultTableCellRenderer

class DevSecAIToolWindowPanel(private val project: Project) : JPanel(BorderLayout()) {

    companion object {
        private val SEVERITY_CRITICAL = JBColor(Color(0xE53935), Color(0xEF5350))
        private val SEVERITY_HIGH = JBColor(Color(0xFF8F00), Color(0xFFB74D))
        private val SEVERITY_MEDIUM = JBColor(Color(0xFDD835), Color(0xFFEE58))
        private val SEVERITY_LOW = JBColor(Color(0x43A047), Color(0x66BB6A))
        private val SEVERITY_INFO = JBColor(Color(0x1E88E5), Color(0x42A5F5))
        private val STATUS_ONLINE = JBColor(Color(0x43A047), Color(0x66BB6A))
        private val STATUS_OFFLINE = JBColor(Color(0x9E9E9E), Color(0xBDBDBD))
        private val STATUS_ERROR = JBColor(Color(0xE53935), Color(0xEF5350))
    }

    private val settings = DevSecAISettings.getInstance()

    // Status panel
    private val statusLabel = JLabel()
    private val serverLabel = JLabel()
    private val policyLabel = JLabel()
    private val lastHeartbeatLabel = JLabel()
    private val statsLabel = JLabel()

    // Filter
    private val severityFilter = ComboBox(arrayOf("All", "CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"))

    // Map display name back to enum name for filtering
    private val displayToSeverity = mapOf(
        "Critical" to "CRITICAL", "High" to "HIGH", "Medium" to "MEDIUM",
        "Low" to "LOW", "Info" to "INFO"
    )

    // Table
    private val tableModel = DefaultTableModel(
        arrayOf("Severity", "Rule ID", "Title", "File", "Line", "Module"), 0
    ) {
        override fun isCellEditable(row: Int, column: Int) = false
    }
    private val table = JBTable(tableModel)

    // Detail panel
    private val detailArea = JTextArea(6, 40).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        font = Font("Monospaced", Font.PLAIN, 12)
    }

    private var currentFindings: List<LocalFinding> = emptyList()

    init {
        buildUI()
        refreshStatus()
        refreshFindings()
    }

    private fun buildUI() {
        // === Top: Status Panel ===
        val statusPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(8, 10)
            background = JBColor.PanelBackground
        }

        // Connection status row
        val statusRow = JPanel(FlowLayout(FlowLayout.LEFT, 4, 0)).apply {
            background = JBColor.PanelBackground
        }
        statusRow.add(JLabel("Status:").apply { font = font.deriveFont(Font.BOLD) })
        statusRow.add(statusLabel)
        statusRow.add(Box.createHorizontalStrut(16))
        statusRow.add(JLabel("Server:").apply { font = font.deriveFont(Font.BOLD) })
        statusRow.add(serverLabel)
        statusPanel.add(statusRow)

        // Policy + heartbeat row
        val infoRow = JPanel(FlowLayout(FlowLayout.LEFT, 4, 0)).apply {
            background = JBColor.PanelBackground
        }
        infoRow.add(JLabel("Policy:").apply { font = font.deriveFont(Font.BOLD) })
        infoRow.add(policyLabel)
        infoRow.add(Box.createHorizontalStrut(16))
        infoRow.add(JLabel("Last HB:").apply { font = font.deriveFont(Font.BOLD) })
        infoRow.add(lastHeartbeatLabel)
        statusPanel.add(infoRow)

        // Stats row
        val statsRow = JPanel(FlowLayout(FlowLayout.LEFT, 4, 0)).apply {
            background = JBColor.PanelBackground
        }
        statsRow.add(JLabel("Findings:").apply { font = font.deriveFont(Font.BOLD) })
        statsRow.add(statsLabel)
        statusPanel.add(statsRow)

        add(statusPanel, BorderLayout.NORTH)

        // === Center: Filter + Table ===
        val centerPanel = JPanel(BorderLayout())

        // Toolbar
        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4)).apply {
            background = JBColor.PanelBackground

            add(JLabel("Severity:"))
            add(severityFilter)

            add(Box.createHorizontalStrut(8))

            add(JButton("Refresh").apply {
                toolTipText = "Refresh findings list"
                addActionListener { refreshFindings() }
            })
            add(JButton("Upload").apply {
                toolTipText = "Upload findings to platform"
                addActionListener { uploadFindings() }
            })
            add(JButton("Clear").apply {
                toolTipText = "Clear local findings"
                addActionListener { clearFindings() }
            })
        }
        centerPanel.add(toolbar, BorderLayout.NORTH)

        // Table
        table.setRowHeight(24)
        table.getColumnModel().getColumn(0).preferredWidth = 80
        table.getColumnModel().getColumn(1).preferredWidth = 120
        table.getColumnModel().getColumn(2).preferredWidth = 200
        table.getColumnModel().getColumn(3).preferredWidth = 250
        table.getColumnModel().getColumn(4).preferredWidth = 60
        table.getColumnModel().getColumn(5).preferredWidth = 70

        // Severity column renderer with color
        table.getColumnModel().getColumn(0).cellRenderer = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(
                table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean,
                row: Int, column: Int
            ): Component {
                val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                val severity = value?.toString() ?: ""
                c.foreground = when {
                    severity.contains("Critical") || severity == "CRITICAL" -> SEVERITY_CRITICAL
                    severity.contains("High") || severity == "HIGH" -> SEVERITY_HIGH
                    severity == "MEDIUM" -> JBColor.BLACK
                    severity.contains("Medium") -> JBColor.BLACK
                    severity.contains("Low") || severity == "LOW" -> SEVERITY_LOW
                    severity.contains("Info") || severity == "INFO" -> SEVERITY_INFO
                    else -> c.foreground
                }
                c.background = when {
                    severity == "MEDIUM" || severity.contains("Medium") -> SEVERITY_MEDIUM
                    else -> if (isSelected) table?.selectionBackground else null
                }
                font = font.deriveFont(Font.BOLD)
                horizontalAlignment = SwingConstants.CENTER
                return c
            }
        }

        // Click handler for detail view
        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val row = table.rowAtPoint(e.point)
                if (row >= 0 && row < currentFindings.size) {
                    showDetail(currentFindings[row])
                }
            }
        })

        centerPanel.add(JBScrollPane(table), BorderLayout.CENTER)

        // Detail panel at bottom
        val detailPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(4, 8)
            add(JLabel("Detail:").apply {
                font = font.deriveFont(Font.BOLD)
                border = JBUI.Borders.empty(0, 0, 4, 0)
            }, BorderLayout.NORTH)
            add(JBScrollPane(detailArea), BorderLayout.CENTER)
        }
        centerPanel.add(detailPanel, BorderLayout.SOUTH)

        add(centerPanel, BorderLayout.CENTER)

        // Filter listener
        severityFilter.addActionListener { refreshFindings() }
    }

    private fun refreshStatus() {
        val serverUrl = settings.serverUrl
        val accessToken = settings.accessToken

        // Connection status
        val heartbeatService = project.getService(HeartbeatService::class.java)
        val isConnected = heartbeatService.isRunning()

        statusLabel.text = if (isConnected) "Connected" else "Disconnected"
        statusLabel.foreground = if (isConnected) STATUS_ONLINE else STATUS_OFFLINE

        serverLabel.text = if (serverUrl.isNotEmpty()) serverUrl else "Not configured"
        serverLabel.foreground = JBColor.GRAY

        // Policy info
        val policyService = project.getService(PolicyService::class.java)
        val policy = policyService.getCachedPolicy()
        policyLabel.text = if (policy != null) {
            val modules = mutableListOf<String>()
            if (policyService.isSastEnabledCached()) modules.add("SAST")
            if (policyService.isSecretsEnabledCached()) modules.add("Secrets")
            if (policyService.isScaEnabledCached()) modules.add("SCA")
            if (policyService.isBaselineEnabledCached()) modules.add("Baseline")
            modules.joinToString(", ").ifEmpty { "None" }
        } else {
            "Not loaded"
        }
        policyLabel.foreground = JBColor.GRAY

        // Last heartbeat
        lastHeartbeatLabel.text = heartbeatService.getLastHeartbeatTime() ?: "Never"
        lastHeartbeatLabel.foreground = JBColor.GRAY

        // Stats
        val collector = project.getService(FindingCollector::class.java)
        val allFindings = collector.getLocalFindings()
        val stats = buildStats(allFindings)
        statsLabel.text = stats
        statsLabel.foreground = JBColor.GRAY
    }

    private fun buildStats(findings: List<LocalFinding>): String {
        if (findings.isEmpty()) return "0 total"
        val critical = findings.count { it.severity == "CRITICAL" }
        val high = findings.count { it.severity == "HIGH" }
        val medium = findings.count { it.severity == "MEDIUM" }
        val low = findings.count { it.severity == "LOW" }
        val info = findings.count { it.severity == "INFO" }
        val parts = mutableListOf<String>()
        if (critical > 0) parts.add("Critical:$critical")
        if (high > 0) parts.add("High:$high")
        if (medium > 0) parts.add("Medium:$medium")
        if (low > 0) parts.add("Low:$low")
        if (info > 0) parts.add("Info:$info")
        return "${findings.size} total [${parts.joinToString(" | ")}]"
    }

    private fun refreshFindings() {
        val collector = project.getService(FindingCollector::class.java)
        val allFindings = collector.getLocalFindings()

        val filter = severityFilter.selectedItem as String
        currentFindings = if (filter == "All") allFindings
        else allFindings.filter { it.severity == filter }

        // Update table
        tableModel.rowCount = 0
        for (f in currentFindings) {
            val sevDisplay = FindingSeverity.entries.find { it.name == f.severity }?.display ?: f.severity
            val line = if (f.startLine > 0) "${f.startLine}" else "-"
            tableModel.addRow(arrayOf(sevDisplay, f.ruleId, f.title, f.filePath, line, f.module))
        }

        // Update stats
        refreshStatus()

        // Clear detail if no selection
        if (currentFindings.isEmpty()) {
            detailArea.text = "No findings detected."
        }
    }

    private fun showDetail(finding: LocalFinding) {
        val severityDisplay = FindingSeverity.entries.find { it.name == finding.severity }?.display ?: finding.severity
        val sb = StringBuilder()
        sb.appendLine("[$severityDisplay] ${finding.title}")
        sb.appendLine("Rule: ${finding.ruleId}")
        sb.appendLine("Module: ${finding.module}")
        sb.appendLine("File: ${finding.filePath}:${finding.startLine}-${finding.endLine}")
        sb.appendLine()
        if (finding.description.isNotEmpty()) {
            sb.appendLine("Description:")
            sb.appendLine(finding.description)
            sb.appendLine()
        }
        if (!finding.recommendation.isNullOrEmpty()) {
            sb.appendLine("Recommendation:")
            sb.appendLine(finding.recommendation)
            sb.appendLine()
        }
        if (!finding.cwe.isNullOrEmpty()) {
            sb.appendLine("CWE: ${finding.cwe}")
        }
        if (!finding.owasp.isNullOrEmpty()) {
            sb.appendLine("OWASP: ${finding.owasp}")
        }
        if (finding.confidence.isNotEmpty()) {
            sb.appendLine("Confidence: ${finding.confidence}")
        }
        if (!finding.componentName.isNullOrEmpty()) {
            sb.appendLine("Component: ${finding.componentName}")
            if (!finding.currentVersion.isNullOrEmpty()) sb.appendLine("Version: ${finding.currentVersion}")
            if (!finding.fixedVersion.isNullOrEmpty()) sb.appendLine("Fixed: ${finding.fixedVersion}")
            if (!finding.vulnerabilityId.isNullOrEmpty()) sb.appendLine("CVE: ${finding.vulnerabilityId}")
        }
        detailArea.text = sb.toString()
        detailArea.caretPosition = 0
    }

    private fun uploadFindings() {
        val collector = project.getService(FindingCollector::class.java)
        val findings = collector.getFindings()
        if (findings.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No findings to upload.", "Info", JOptionPane.INFORMATION_MESSAGE)
            return
        }

        val confirmed = JOptionPane.showConfirmDialog(
            this,
            "Upload ${findings.size} findings to the platform?\nServer: ${settings.serverUrl}",
            "Confirm Upload",
            JOptionPane.YES_NO_OPTION
        )

        if (confirmed == JOptionPane.YES_OPTION) {
            val result = collector.flush()
            if (result.successful) {
                JOptionPane.showMessageDialog(
                    this,
                    "Successfully uploaded findings to the platform.",
                    "Upload Success",
                    JOptionPane.INFORMATION_MESSAGE
                )
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Upload failed: ${result.message}\n\nPlease check:\n1. Server URL is correct in Settings\n2. Access Token is valid\n3. Platform server is running",
                    "Upload Failed",
                    JOptionPane.ERROR_MESSAGE
                )
            }
            refreshFindings()
        }
    }

    private fun clearFindings() {
        val confirmed = JOptionPane.showConfirmDialog(
            this,
            "Clear all local findings? This does not affect already uploaded findings.",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION
        )
        if (confirmed == JOptionPane.YES_OPTION) {
            val collector = project.getService(FindingCollector::class.java)
            collector.clearFindings()
            refreshFindings()
        }
    }
}
