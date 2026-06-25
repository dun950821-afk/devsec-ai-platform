package com.guoshun.devsecai.ui

import com.guoshun.devsecai.model.LocalFinding
import com.guoshun.devsecai.service.FindingCollector
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.*
import javax.swing.*
import javax.swing.table.DefaultTableModel

/**
 * DevSecAI 检测结果 ToolWindow 面板
 */
class DevSecAIToolWindowPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val tableModel = DefaultTableModel(
        arrayOf("严重等级", "规则", "描述", "文件", "行号", "模块"),
        0
    )
    private val table = JBTable(tableModel)
    private val severityFilter = ComboBox(arrayOf("全部", "严重", "高危", "中危", "低危"))
    private val statusLabel = JLabel("就绪")

    // 本地 Finding 缓存（用于表格展示）
    private val localFindings = mutableListOf<LocalFinding>()

    init {
        // 顶部工具栏
        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT))
        toolbar.add(JLabel("严重等级:"))
        toolbar.add(severityFilter)
        toolbar.add(Box.createHorizontalStrut(10))

        val refreshBtn = JButton("刷新", AllIcons.Actions.Refresh)
        refreshBtn.addActionListener { refreshTable() }
        toolbar.add(refreshBtn)

        val uploadBtn = JButton("上送结果", AllIcons.Actions.Upload)
        uploadBtn.addActionListener { uploadFindings() }
        toolbar.add(uploadBtn)

        val clearBtn = JButton("清空", AllIcons.Actions.GC)
        clearBtn.addActionListener { clearFindings() }
        toolbar.add(clearBtn)

        toolbar.add(Box.createHorizontalStrut(20))
        toolbar.add(statusLabel)

        // 表格设置
        table.rowHeight = 28
        table.setAutoCreateRowSorter(true)
        table.columnModel.getColumn(0).preferredWidth = 70
        table.columnModel.getColumn(1).preferredWidth = 140
        table.columnModel.getColumn(2).preferredWidth = 300
        table.columnModel.getColumn(3).preferredWidth = 250
        table.columnModel.getColumn(4).preferredWidth = 50
        table.columnModel.getColumn(5).preferredWidth = 70

        // 严重等级列渲染器
        table.columnModel.getColumn(0).cellRenderer = SeverityCellRenderer()

        // 布局
        add(toolbar, BorderLayout.NORTH)
        add(JBScrollPane(table), BorderLayout.CENTER)

        // 统计面板
        add(createStatsPanel(), BorderLayout.SOUTH)

        // 监听筛选
        severityFilter.addActionListener { refreshTable() }
    }

    /**
     * 添加 Finding 到本地缓存和表格
     */
    fun addFinding(finding: LocalFinding) {
        localFindings.add(finding)
        refreshTable()
    }

    /**
     * 批量添加 Finding
     */
    fun addFindings(findings: List<LocalFinding>) {
        localFindings.addAll(findings)
        refreshTable()
    }

    private fun refreshTable() {
        tableModel.rowCount = 0
        val selectedSeverity = severityFilter.selectedItem as String

        val severityMap = mapOf(
            "严重" to "CRITICAL", "高危" to "HIGH",
            "中危" to "MEDIUM", "低危" to "LOW"
        )

        val filteredFindings = if (selectedSeverity == "全部") {
            localFindings
        } else {
            localFindings.filter { it.severity == (severityMap[selectedSeverity] ?: it.severity) }
        }

        val severityChinese = mapOf(
            "CRITICAL" to "严重", "HIGH" to "高危",
            "MEDIUM" to "中危", "LOW" to "低危"
        )

        for (f in filteredFindings) {
            tableModel.addRow(arrayOf(
                severityChinese[f.severity] ?: f.severity,
                f.ruleId,
                f.title,
                f.filePath.substringAfterLast("/"),
                f.startLine.toString(),
                f.module
            ))
        }

        updateStats(localFindings)
    }

    private fun updateStats(findings: List<LocalFinding>) {
        val critical = findings.count { it.severity == "CRITICAL" }
        val high = findings.count { it.severity == "HIGH" }
        val medium = findings.count { it.severity == "MEDIUM" }
        val low = findings.count { it.severity == "LOW" }
        statusLabel.text = "共 ${findings.size} 项 | 严重:$critical 高危:$high 中危:$medium 低危:$low"
    }

    private fun uploadFindings() {
        statusLabel.text = "正在上送..."
        val collector = FindingCollector.getInstance(project)
        // 将本地未上传的 Finding 交给 collector 上送
        val notUploaded = localFindings.filter { !it.uploaded }
        if (notUploaded.isEmpty()) {
            statusLabel.text = "没有待上送的结果"
            return
        }
        notUploaded.groupBy { it.module }.forEach { (module, findings) ->
            collector.addFindings(findings)
        }
        collector.flush()
        notUploaded.forEach { it.uploaded = true }
        statusLabel.text = "上送完成"
    }

    private fun clearFindings() {
        localFindings.clear()
        refreshTable()
    }

    private fun createStatsPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
        panel.add(JLabel("DevSecAI 安全检测结果"))
        return panel
    }

    /**
     * 严重等级单元格渲染器（带颜色）
     */
    private class SeverityCellRenderer : JLabel(), javax.swing.table.TableCellRenderer {
        init {
            isOpaque = true
            horizontalAlignment = SwingConstants.CENTER
        }

        override fun getTableCellRendererComponent(
            table: JTable?, value: Any?, isSelected: Boolean,
            hasFocus: Boolean, row: Int, column: Int
        ): Component {
            text = value?.toString() ?: ""
            when (text) {
                "严重" -> { background = Color(220, 53, 69); foreground = Color.WHITE }
                "高危" -> { background = Color(255, 120, 50); foreground = Color.WHITE }
                "中危" -> { background = Color(255, 193, 7); foreground = Color.BLACK }
                "低危" -> { background = Color(40, 167, 69); foreground = Color.WHITE }
                else -> { background = Color.WHITE; foreground = Color.BLACK }
            }
            if (isSelected) {
                background = background.darker()
            }
            return this
        }
    }
}
