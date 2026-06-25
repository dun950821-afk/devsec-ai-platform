package com.guoshun.devsecai.ui

import com.guoshun.devsecai.model.LocalFinding
import com.guoshun.devsecai.service.FindingCollector
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.*
import javax.swing.*
import javax.swing.table.DefaultTableModel

class DevSecAIToolWindowPanel(private val project: Project, toolWindow: ToolWindow) {

    private val panel = JPanel(BorderLayout())
    private val tableModel = DefaultTableModel(
            arrayOf("Severity", "Module", "Title", "File", "Line", "Rule ID"), 0
    )
    private val table = JBTable(tableModel)
    private var severityFilter: ComboBox<String> = ComboBox(arrayOf("All", "CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"))
    private val allFindings = mutableListOf<LocalFinding>()

    init {
        // Toolbar
        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT))
        toolbar.add(JLabel("Severity:"))
        toolbar.add(severityFilter)

        val scanButton = JButton("Scan Project")
        scanButton.addActionListener { triggerScan() }
        toolbar.add(scanButton)

        val uploadButton = JButton("Upload Results")
        uploadButton.addActionListener { uploadFindings() }
        toolbar.add(uploadButton)

        val clearButton = JButton("Clear")
        clearButton.addActionListener { clearResults() }
        toolbar.add(clearButton)

        val refreshButton = JButton("Refresh")
        refreshButton.addActionListener { refreshTable() }
        toolbar.add(refreshButton)

        severityFilter.addActionListener { refreshTable() }

        // Table setup
        table.rowSorter = null
        table.autoCreateRowSorter = true
        table.setRowHeight(24)

        // Severity color renderer
        val severityColumn = table.columnModel.getColumn(0)
        severityColumn.preferredWidth = 80
        severityColumn.cellRenderer = SeverityRenderer()

        val scrollPane = JBScrollPane(table)

        panel.add(toolbar, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)

        // Status bar
        val statusBar = JPanel(FlowLayout(FlowLayout.LEFT))
        val statusLabel = JLabel("Ready")
        statusBar.add(statusLabel)
        panel.add(statusBar, BorderLayout.SOUTH)

        // Initial load
        refreshTable()
    }

    fun getContent(): JComponent = panel

    private fun refreshTable() {
        tableModel.rowCount = 0
        val filterSeverity = severityFilter.selectedItem as? String ?: "All"

        try {
            val collector = project.getService(FindingCollector::class.java)
            allFindings.clear()
            allFindings.addAll(collector.getFindings())
        } catch (e: Exception) {
            // Service may not be available yet
        }

        val filtered = if (filterSeverity == "All") allFindings
        else allFindings.filter { it.severity == filterSeverity }

        for (finding in filtered) {
            tableModel.addRow(arrayOf(
                    finding.severity,
                    finding.module,
                    finding.title,
                    finding.filePath.substringAfterLast('/'),
                    finding.startLine.toString(),
                    finding.ruleId
            ))
        }

        updateStatusBar()
    }

    private fun triggerScan() {
        try {
            val actionManager = ActionManager.getInstance()
            val action = actionManager.getAction("DevSecAI.ManualScan")
            if (action != null) {
                val event = com.intellij.openapi.actionSystem.AnActionEvent.createFromDataContext(
                        "DevSecAIToolWindow", null,
                        com.intellij.openapi.actionSystem.DataContext { null }
                )
                action.actionPerformed(event)
            }
        } catch (e: Exception) {
            // Fallback
        }
    }

    private fun uploadFindings() {
        try {
            val collector = project.getService(FindingCollector::class.java)
            collector.flush()
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun clearResults() {
        tableModel.rowCount = 0
        try {
            val collector = project.getService(FindingCollector::class.java)
            collector.clearFindings()
            allFindings.clear()
        } catch (e: Exception) {
            // Ignore
        }
        updateStatusBar()
    }

    private fun updateStatusBar() {
        val total = allFindings.size
        val critical = allFindings.count { it.severity == "CRITICAL" }
        val high = allFindings.count { it.severity == "HIGH" }
        val medium = allFindings.count { it.severity == "MEDIUM" }
        val low = allFindings.count { it.severity == "LOW" }
        val statusLabel = (panel.getComponent(2) as JPanel).getComponent(0) as JLabel
        statusLabel.text = "Total: $total | Critical: $critical | High: $high | Medium: $medium | Low: $low"
    }

    private class SeverityRenderer : javax.swing.table.DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
                table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean,
                row: Int, column: Int
        ): Component {
            val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            when (value?.toString()) {
                "CRITICAL" -> c.foreground = Color(255, 0, 0)
                "HIGH" -> c.foreground = Color(255, 102, 0)
                "MEDIUM" -> c.foreground = Color(255, 170, 0)
                "LOW" -> c.foreground = Color(102, 170, 255)
                "INFO" -> c.foreground = Color(136, 136, 136)
            }
            c.font = c.font.deriveFont(Font.BOLD)
            return c
        }
    }
}
