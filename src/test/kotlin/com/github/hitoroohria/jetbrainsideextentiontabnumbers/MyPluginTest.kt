package com.github.hitoroohria.jetbrainsideextentiontabnumbers

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MyPluginTest : BasePlatformTestCase() {

    fun testEditorTabTitleIncludesOpenTabNumber() {
        val firstFile = myFixture.configureByText("first.txt", "first").virtualFile
        val secondFile = myFixture.configureByText("second.txt", "second").virtualFile

        val provider = TabNumberEditorTabTitleProvider()

        assertEquals("1. first.txt", provider.getEditorTabTitle(project, firstFile))
        assertEquals("2. second.txt", provider.getEditorTabTitle(project, secondFile))
    }
}
