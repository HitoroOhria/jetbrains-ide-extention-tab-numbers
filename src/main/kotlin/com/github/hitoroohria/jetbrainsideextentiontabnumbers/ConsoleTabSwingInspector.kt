package com.github.hitoroohria.jetbrainsideextentiontabnumbers

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.tabs.JBTabs
import java.awt.Component
import java.awt.Container

// Approach 2: Swing コンポーネントツリーを探索して Console タブを持つ JBTabs を直接操作する
// ProjectActivity ではなく StartupActivity.DumbAware を使う
// (DataGrip で <projectActivity> 要素が認識されなかったため <postStartupActivity> に変更)
class ConsoleTabSwingInspector : StartupActivity.DumbAware {
    private val log = thisLogger()

    override fun runActivity(project: Project) {
        // Activity が起動したか確認するためのログ
        log.warn("[ConsoleTabInspector] runActivity() started for project: ${project.name}")

        // 起動時に初回スキャン (UI 初期化を待つため invokeLater)
        ApplicationManager.getApplication().invokeLater {
            log.warn("[ConsoleTabInspector] Initial scan running")
            inspectAndUpdate(project)
        }

        project.messageBus.connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                    log.warn("[ConsoleTabInspector] fileOpened: ${file.url}")
                    scheduleUpdate(project)
                }
                override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                    log.warn("[ConsoleTabInspector] fileClosed: ${file.url}")
                    scheduleUpdate(project)
                }
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    log.warn("[ConsoleTabInspector] selectionChanged: ${event.newFile?.url}")
                    scheduleUpdate(project)
                }
            }
        )
    }

    private fun scheduleUpdate(project: Project) {
        ApplicationManager.getApplication().invokeLater { inspectAndUpdate(project) }
    }

    private fun inspectAndUpdate(project: Project) {
        val frame = WindowManager.getInstance().getFrame(project) ?: run {
            log.warn("[ConsoleTabInspector] Frame is null, skipping")
            return
        }
        val allTabs = collectJBTabs(frame)

        // フィルタなしで全 JBTabs を出力して Console タブの所在を特定する
        log.warn("[ConsoleTabInspector] Found ${allTabs.size} JBTabs instances")
        for ((i, tabs) in allTabs.withIndex()) {
            val tabList = tabs.tabs
            log.warn("[ConsoleTabInspector] JBTabs[$i] class=${tabs.javaClass.name} count=${tabList.size}")
            tabList.forEachIndexed { j, tab ->
                val comp = tab.component?.javaClass?.name ?: "null"
                log.warn("[ConsoleTabInspector]   [$j] text='${tab.text}' component=$comp")
            }
        }

        // Console らしい JBTabs を対象に番号付与を試みる
        for (tabs in allTabs) {
            val tabList = tabs.tabs
            val isConsoleContainer = tabList.any { tab ->
                val text = tab.text.lowercase()
                val comp = tab.component?.javaClass?.name?.lowercase() ?: ""
                text.contains("console") || comp.contains("console") || comp.contains("database")
            }
            if (!isConsoleContainer) continue

            log.warn("[ConsoleTabInspector] Attempting to number console tabs in ${tabs.javaClass.name}")
            tabList.forEachIndexed { index, tab ->
                val stripped = tab.text.replace(Regex("""^\d+\. """), "")
                val numbered = "${index + 1}. $stripped"
                if (tab.text != numbered) {
                    val before = tab.text
                    tab.setText(numbered)
                    log.warn("[ConsoleTabInspector]   Renamed '$before' -> '$numbered'")
                }
            }
        }
    }

    private fun collectJBTabs(root: Component): List<JBTabs> {
        val result = mutableListOf<JBTabs>()
        if (root is JBTabs) result.add(root)
        if (root is Container) {
            for (child in root.components) result.addAll(collectJBTabs(child))
        }
        return result
    }
}
