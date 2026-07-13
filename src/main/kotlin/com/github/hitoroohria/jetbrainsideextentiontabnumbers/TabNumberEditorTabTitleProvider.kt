package com.github.hitoroohria.jetbrainsideextentiontabnumbers

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class TabNumberEditorTabTitleProvider : EditorTabTitleProvider {
    private val log = thisLogger()

    override fun getEditorTabTitle(project: Project, file: VirtualFile): String? {
        // Approach 4: VirtualFile の詳細をログ出力して Console タブの実態を調査する
        log.warn("[TabNumber] getEditorTabTitle: url=${file.url} class=${file.javaClass.name} fs=${file.fileSystem.javaClass.name}")

        val tabNumber = findOpenTabNumber(project, file) ?: run {
            log.warn("[TabNumber] → not in fileList, skipped")
            return null
        }

        log.warn("[TabNumber] → tabNumber=$tabNumber")
        return "$tabNumber. ${file.presentableName}"
    }

    companion object {
        internal fun findOpenTabNumber(project: Project, file: VirtualFile): Int? {
            val manager = FileEditorManagerEx.getInstanceEx(project)

            // window.fileList に含まれる全ファイルをログ出力 (Console ファイルが含まれるか確認)
            manager.windows.forEachIndexed { wi, window ->
                window.fileList.forEachIndexed { fi, f ->
                    thisLogger().warn("[TabNumber] window[$wi].fileList[$fi]: url=${f.url} class=${f.javaClass.name}")
                }
            }

            val window = manager.windows.firstOrNull { it.fileList.contains(file) } ?: return null
            val index = window.fileList.indexOf(file)
            return if (index >= 0) index + 1 else null
        }
    }
}
