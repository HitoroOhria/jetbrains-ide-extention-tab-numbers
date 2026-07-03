# DataGrip Console Tab Investigation

This note records failed approaches and useful findings for showing tab numbers on DataGrip console tabs.

## Problem

The current plugin uses `EditorTabTitleProvider` to prefix editor tab names with their open-tab position. This works for regular editor tabs, DataGrip table tabs, and DDL tabs, but it does not affect DataGrip console tabs.

## Attempts That Did Not Work

### Use `EditorWindow.allComposites`

The first attempt changed tab number lookup from:

```kotlin
window.fileList
```

to:

```kotlin
window.allComposites.map { it.file }.ifEmpty { window.fileList }
```

and compared `VirtualFile.url` as well as `VirtualFile` equality. This did not make numbers appear on DataGrip console tabs.

### Update `TabInfo` From `FileEditorManagerListener`

The second attempt added a `FileEditorManagerListener` and directly rewrote editor tab `TabInfo.text` after `fileOpened`, `fileClosed`, and `selectionChanged`.

This also did not make numbers appear on DataGrip console tabs. It suggests the visible console tab label is either not represented by the same `EditorWindow`/`JBTabs` path used by normal editor tabs, or DataGrip rewrites the label after these updates.

## Findings

The JetBrains Platform SDK docs confirm that plugin behavior is typically registered through `plugin.xml` extension points and listeners:

- https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html
- https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html

Inspection of the bundled DatabaseTools plugin in the local IntelliJ IDEA Ultimate distribution showed:

- DataGrip database support lives under `plugins/DatabaseTools`.
- Database console-related classes are in `database-plugin.jar`, including:
  - `com.intellij.database.console.JdbcConsole`
  - `com.intellij.database.console.JdbcConsoleProvider`
  - `com.intellij.database.console.DbConsoleRootType`
- `JdbcConsole` has methods such as `getTitle()`, `getDisplayName()`, and `getVirtualFile()`.
- `JdbcConsole$FilePresentationUpdater` calls:

```java
FileEditorManagerEx.getInstanceEx(project).updateFilePresentation(myVirtualFile)
```

- `DbConsoleRootType.substituteName(project, file)` builds the console display name from the console scratch file and data source information.
- DatabaseTools registers its own `editorTabTitleProvider`:

```xml
<editorTabTitleProvider implementation="com.intellij.database.vfs.DatabaseElementTabTitleProvider"/>
```

but `DatabaseElementTabTitleProvider` only handles `DatabaseElementVirtualFileImpl`, so it does not appear to be the direct path for console scratch files.

## Current Conclusion

DataGrip console tabs appear to use a DatabaseTools-specific presentation path based around `JdbcConsole`, `DbConsoleRootType`, and `updateFilePresentation()`. The plugin's current `EditorTabTitleProvider` hook is not enough, and directly updating normal editor `TabInfo` from `FileEditorManagerListener` was also ineffective.

Future investigation should focus on finding the exact UI component or presentation updater used for `JdbcConsole` tabs, rather than only the generic editor tab title provider path.
