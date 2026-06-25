package com.guoshun.devsecai.ui

import com.guoshun.devsecai.model.LocalFinding
import com.guoshun.devsecai.service.FindingCollector
import com.guoshun.devsecai.service.SecurityScanService
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.table.DefaultTableModel

class DevSecAIToolWindowPanel(private val project: Project, toolWindow: ToolWindow) {

    private val panel = JPanel(BorderLayout())
    private val tableModel = object : DefaultTableModel(arrayOf("等级", "类型", "风险", "位置", "问题原因", "修复建议"), 0) {
        override fun isCellEditable(row: Int, column: Int): Boolean = false
    }
    private val table = JBTable(tableModel)
    private var severityFilter: ComboBox<String> = ComboBox(arrayOf("全部", "严重", "高危", "中危", "低危", "提示"))
    private val allFindings = mutableListOf<LocalFinding>()
    private val displayedFindings = mutableListOf<LocalFinding>()
    private val statusLabel = JLabel("就绪")

    init {
        // Toolbar
        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT))
        toolbar.add(JLabel("风险等级："))
        toolbar.add(severityFilter)

        val scanButton = JButton("扫描项目")
        scanButton.addActionListener { triggerScan() }
        toolbar.add(scanButton)

        val uploadButton = JButton("上传结果")
        uploadButton.addActionListener { uploadFindings() }
        toolbar.add(uploadButton)

        val clearButton = JButton("清空")
        clearButton.addActionListener { clearResults() }
        toolbar.add(clearButton)

        val refreshButton = JButton("刷新")
        refreshButton.addActionListener { refreshTable() }
        toolbar.add(refreshButton)

        severityFilter.addActionListener { refreshTable() }

        // Table setup
        table.rowSorter = null
        table.autoCreateRowSorter = true
        table.setRowHeight(32)
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.emptyText.text = "暂无检测结果，请点击“扫描项目”"
        table.toolTipText = "双击结果或按 Enter 可跳转到对应代码行"
        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    navigateSelectedFinding()
                }
            }
        })
        table.inputMap.put(KeyStroke.getKeyStroke("ENTER"), "navigateFinding")
        table.actionMap.put("navigateFinding", object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                navigateSelectedFinding()
            }
        })

        // Severity color renderer
        val severityColumn = table.columnModel.getColumn(0)
        severityColumn.preferredWidth = 80
        severityColumn.cellRenderer = SeverityRenderer()
        table.columnModel.getColumn(1).preferredWidth = 80
        table.columnModel.getColumn(2).preferredWidth = 180
        table.columnModel.getColumn(3).preferredWidth = 140
        table.columnModel.getColumn(4).preferredWidth = 320
        table.columnModel.getColumn(5).preferredWidth = 360

        val scrollPane = JBScrollPane(table)

        panel.add(toolbar, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)

        // Status bar
        val statusBar = JPanel(FlowLayout(FlowLayout.LEFT))
        statusBar.add(statusLabel)
        panel.add(statusBar, BorderLayout.SOUTH)

        // Initial load
        refreshTable()
    }

    fun getContent(): JComponent = panel

    private fun refreshTable() {
        tableModel.rowCount = 0
        val filterSeverity = severityFilter.selectedItem as? String ?: "全部"

        try {
            val collector = project.getService(FindingCollector::class.java)
            allFindings.clear()
            allFindings.addAll(collector.getFindings())
        } catch (e: Exception) {
            // Service may not be available yet
        }

        val selectedSeverity = severityValue(filterSeverity)
        val filtered = if (selectedSeverity == null) allFindings
        else allFindings.filter { it.severity == selectedSeverity }
        displayedFindings.clear()
        displayedFindings.addAll(filtered)

        for (finding in filtered) {
            tableModel.addRow(arrayOf(
                    severityText(finding.severity),
                    finding.module,
                    finding.title,
                    locationText(finding),
                    finding.description,
                    finding.recommendation ?: finding.description
            ))
        }

        updateStatusBar()
    }

    private fun triggerScan() {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "DevSecAI 安全扫描", true) {
            private var summary = SecurityScanService.ScanSummary(0, 0)

            override fun run(indicator: ProgressIndicator) {
                summary = project.getService(SecurityScanService::class.java).scanProject(indicator, clearPrevious = true)
            }

            override fun onSuccess() {
                refreshTable()
                statusLabel.text = "扫描完成：${summary.filesScanned} 个文件，发现 ${summary.findingsFound} 个风险"
            }
        })
    }

    private fun uploadFindings() {
        try {
            val collector = project.getService(FindingCollector::class.java)
            val result = collector.flush()
            val summary = "成功：${result.uploaded}，失败：${result.failed}，待上传：${result.pending}"
            statusLabel.text = "上传结果：$summary"
            refreshTable()
            if (result.failed > 0) {
                val reason = result.failureReasons.joinToString("\n").ifBlank { "平台未返回明确失败原因" }
                Messages.showErrorDialog(project, "上传失败。\n$summary\n\n失败原因：\n$reason", "DevSecAI 上传结果")
            } else {
                Messages.showInfoMessage(project, "上传成功。\n$summary", "DevSecAI 上传结果")
            }
        } catch (e: Exception) {
            statusLabel.text = "上传失败：${e.message}"
            Messages.showErrorDialog(project, "上传失败：${e.message ?: "未知错误"}", "DevSecAI 上传结果")
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
        statusLabel.text = "总数：$total | 严重：$critical | 高危：$high | 中危：$medium | 低危：$low"
    }

    private fun severityText(severity: String): String = when (severity) {
        "CRITICAL" -> "严重"
        "HIGH" -> "高危"
        "MEDIUM" -> "中危"
        "LOW" -> "低危"
        "INFO" -> "提示"
        else -> severity
    }

    private fun severityValue(displayText: String): String? = when (displayText) {
        "严重" -> "CRITICAL"
        "高危" -> "HIGH"
        "中危" -> "MEDIUM"
        "低危" -> "LOW"
        "提示" -> "INFO"
        else -> null
    }

    private fun locationText(finding: LocalFinding): String {
        val name = finding.filePath.replace('\\', '/').substringAfterLast('/')
        val line = finding.startLine.coerceAtLeast(1)
        return "$name:$line"
    }

    private fun navigateSelectedFinding() {
        val selectedViewRow = table.selectedRow
        if (selectedViewRow < 0) {
            return
        }

        val modelRow = table.convertRowIndexToModel(selectedViewRow)
        val finding = displayedFindings.getOrNull(modelRow) ?: return
        val normalizedPath = finding.filePath.replace('\\', '/')
        val file = LocalFileSystem.getInstance().refreshAndFindFileByPath(normalizedPath)
        if (file == null) {
            Messages.showWarningDialog(
                project,
                "无法打开文件：\n${finding.filePath}\n\n请确认文件仍在当前项目或远程开发后端可访问。",
                "DevSecAI 跳转失败"
            )
            return
        }

        OpenFileDescriptor(project, file, finding.startLine.coerceAtLeast(1) - 1, 0).navigate(true)
    }

    private class SeverityRenderer : javax.swing.table.DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
                table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean,
                row: Int, column: Int
        ): Component {
            val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            when (value?.toString()) {
                "严重" -> c.foreground = Color(255, 0, 0)
                "高危" -> c.foreground = Color(255, 102, 0)
                "中危" -> c.foreground = Color(255, 170, 0)
                "低危" -> c.foreground = Color(102, 170, 255)
                "提示" -> c.foreground = Color(136, 136, 136)
            }
            c.font = c.font.deriveFont(Font.BOLD)
            return c
        }
    }
}
