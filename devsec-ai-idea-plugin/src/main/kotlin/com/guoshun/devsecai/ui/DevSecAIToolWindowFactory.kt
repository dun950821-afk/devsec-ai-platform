package com.guoshun.devsecai.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class DevSecAIToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = DevSecAIToolWindowPanel(project, toolWindow)
        val content = ContentFactory.getInstance().createContent(panel.getContent(), "", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun init(toolWindow: ToolWindow) {
        toolWindow.stripeTitle = "DevSecAI"
    }
}
