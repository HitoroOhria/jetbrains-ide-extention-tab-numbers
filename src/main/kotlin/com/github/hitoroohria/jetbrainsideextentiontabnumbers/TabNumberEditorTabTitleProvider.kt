package com.github.hitoroohria.jetbrainsideextentiontabnumbers

import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class TabNumberEditorTabTitleProvider : EditorTabTitleProvider {

    override fun getEditorTabTitle(project: Project, file: VirtualFile): String? {
        val tabNumber = findOpenTabNumber(project, file) ?: return null

        return "$tabNumber. ${file.presentableName}"
    }

    companion object {
        internal fun findOpenTabNumber(project: Project, file: VirtualFile): Int? {
            val openFiles = FileEditorManagerEx.getInstanceEx(project)
                .windows
                .flatMap { it.fileList.asIterable() }

            val index = openFiles.indexOf(file)
            return if (index >= 0) index + 1 else null
        }
    }
}
