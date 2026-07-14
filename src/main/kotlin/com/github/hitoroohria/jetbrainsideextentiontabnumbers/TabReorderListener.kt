package com.github.hitoroohria.jetbrainsideextentiontabnumbers

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.tabs.JBTabs
import com.intellij.ui.tabs.TabsListener
import java.util.Collections
import java.util.WeakHashMap

// タブがドラッグ等で並び替えられたとき、そのウィンドウ内の全タブの番号を更新する
// (JBTabs.tabsMoved はファイルの開閉・選択とは独立したイベントのため専用の購読が必要。
//  並び替え自体は移動したタブ自身の選択状態変化で再描画されるが、
//  入れ替わった側のタブは何もトリガーされないため個別に updateFilePresentation する)
class TabReorderListener : StartupActivity.DumbAware {
    private val instrumentedTabs = Collections.newSetFromMap(WeakHashMap<JBTabs, Boolean>())

    override fun runActivity(project: Project) {
        ApplicationManager.getApplication().invokeLater { instrumentAllWindows(project) }

        project.messageBus.connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                    ApplicationManager.getApplication().invokeLater { instrumentAllWindows(project) }
                }

                override fun selectionChanged(event: FileEditorManagerEvent) {
                    ApplicationManager.getApplication().invokeLater { instrumentAllWindows(project) }
                }
            }
        )
    }

    private fun instrumentAllWindows(project: Project) {
        FileEditorManagerEx.getInstanceEx(project).windows.forEach { window -> instrumentWindow(window, project) }
    }

    private fun instrumentWindow(window: EditorWindow, project: Project) {
        val tabs = window.tabbedPane.tabs
        if (!instrumentedTabs.add(tabs)) return

        tabs.addListener(object : TabsListener {
            override fun tabsMoved() {
                ApplicationManager.getApplication().invokeLater { refreshAllTabTitles(project) }
            }
        })
    }

    private fun refreshAllTabTitles(project: Project) {
        val manager = FileEditorManager.getInstance(project)
        FileEditorManagerEx.getInstanceEx(project).windows.forEach { window ->
            window.fileList.forEach { file -> manager.updateFilePresentation(file) }
        }
    }
}
