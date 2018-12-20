package ladysnake.translationhelper.controller

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TableColumn
import javafx.scene.control.TextInputDialog
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.stage.WindowEvent
import javafx.util.Duration
import ladysnake.translationhelper.UserSettings
import ladysnake.translationhelper.model.TranslateAPI
import ladysnake.translationhelper.model.data.Language
import ladysnake.translationhelper.model.data.TranslationMap
import ladysnake.translationhelper.model.workspace.SourcesMap
import ladysnake.translationhelper.model.workspace.TranslationWorkspace
import ladysnake.translationhelper.view.TranslatorView
import ladysnake.translationhelper.view.language
import tornadofx.*
import java.io.File
import java.io.IOException


object TranslationController {
    private var workspace: TranslationWorkspace? = null
        set(value) {
            field = value
            value?.transactionManager?.addListener { Platform.runLater { view.translationTable?.refresh() } }
        }
    private var view: TranslatorView = find(TranslatorView::class)
    private val autosaveTimer = Timeline(KeyFrame(Duration.seconds(10.0), null, EventHandler { save() }))

    init {
        autosaveTimer.cycleCount = Animation.INDEFINITE
        if (UserSettings.autosaveProperty.get()) {
            autosaveTimer.play()
        }
        UserSettings.autosaveProperty.addListener { _, _, enable ->
            if (enable) {
                autosaveTimer.play()
            } else {
                autosaveTimer.pause()
            }
        }
    }

    /**
     * Handles the program exiting
     */
    fun onExit(event: WindowEvent) {
        val workspace = workspace ?: return
        if (UserSettings.autosaveProperty.get()) {
            workspace.save()
        } else if (workspace.sourceFiles.values.any { it.hasChanged }) {
            val confirm = Alert(Alert.AlertType.CONFIRMATION)
            confirm.headerText = "You have some unsaved changes !"
            confirm.contentText = "Press OK to ignore"
            confirm.showAndWait().filter { b -> b == ButtonType.CANCEL }.ifPresent { event.consume() }
        }
    }

    fun chooseFolder(langFolder: File, lockedFiles: SourcesMap): ChooseFolderResult {
        return try {
            val workspace = TranslationWorkspace.load(langFolder, lockedFiles)
            println(workspace)
            this.workspace = workspace
            view.canRedo.bind(workspace.transactionManager.canRedo)
            view.canUndo.bind(workspace.transactionManager.canUndo)
            ChooseFolderResult(workspace.translationData, workspace.sourceFiles)
        } catch (e: IOException) {
            System.err.println("The file selected isn't a valid folder ($e)")
            ChooseFolderResult.NONE
        }
    }

    data class ChooseFolderResult(val translationData: TranslationMap?, val sourceFiles: SourcesMap?) {
        companion object {
            val NONE = ChooseFolderResult(null, null)
        }
    }

    fun copySelectedCell() {
        val table = view.translationTable ?: return
        val tablePosition = table.focusModel.focusedCell
        val row = table.items[tablePosition.row] as? TranslationMap.TranslationRow ?: return
        val content = ClipboardContent()
        val contentString = row[tablePosition.tableColumn.language]
        println("copying $contentString")
        content.putString(contentString)
        content.putHtml("<td>$contentString</td>")
        Clipboard.getSystemClipboard().setContent(content)
    }

    private fun setSelectedCellContent(content: String) {
        val tablePositions = view.translationTable?.selectionModel?.selectedCells ?: return
        for (tablePosition in tablePositions) {
            workspace?.updateTranslation(
                tablePosition.row,
                tablePosition.tableColumn.language ?: return,
                content
            )
        }
    }

    fun clearSelectedCell() {
        setSelectedCellContent("")
    }

    fun pasteInSelectedCell() {
        setSelectedCellContent(Clipboard.getSystemClipboard().string)
    }

    fun undo() = workspace?.transactionManager?.undo()

    fun redo() = workspace?.transactionManager?.redo()

    fun save() {
        workspace?.save()
    }

    fun export(langFolder: File, extension: String) {
        val workspace = this.workspace ?: return
        val overwrittenFiles = mutableSetOf<File>()
        for (lang in workspace.translationData.languages) {
            val file = File(langFolder, "$lang.$extension")
            if (file.exists()) {
                overwrittenFiles += file
            }
        }
        // Ask confirmation before overwriting
        if (overwrittenFiles.isEmpty() || confirmation(
                "The following files will be overwritten:",
                overwrittenFiles.joinToString(limit = 5) { it.name }
            ).showAndWait()
                .filter { it.buttonData.isDefaultButton }
                .isPresent
        ) { workspace.export(langFolder, extension) }
    }

    fun editTranslationKey() {
        val table = view.translationTable ?: return
        val workspace = workspace ?: return
        val row = workspace.translationData[table.selectionModel.selectedIndex]
        val d = TextInputDialog()
        d.graphic = null
        d.headerText = "Enter the new translation key:"
        d.contentText = "Note: this will update every opened file."
        d.title = "New translation key"
        d.editor.text = row.key
        d.showAndWait()
            .ifPresent { s -> workspace.updateTranslationKey(row.key, s) }
        table.refresh()
        table.sort()
    }

    fun removeTranslationKey() {
        val table = view.translationTable ?: return
        val workspace = workspace ?: return
        val row = workspace.translationData[table.selectionModel.selectedIndex]
        workspace.deleteTranslation(row.key)
    }

    fun addTranslationKey(key: String) {
        workspace?.addTranslationRow(key)
    }

    fun createFile() {
        TODO("Language file creation is not supported yet")
    }

    fun joker() {
        view.status = "fetching translation"
        val outLang = view.translationTable?.selectionModel?.selectedCells?.get(0)?.tableColumn?.language ?: return
        runAsync {
            val table = view.translationTable
            val workspace = workspace
            if (table != null && workspace != null) {
                TranslateAPI.translate(
                    workspace.translationData[table.selectionModel.selectedIndex][Language("en_us")] ?: "",
                    outLang.name
                )
            } else {
                null
            }
        } success { translated ->
            view.status = "idle"
            if (translated != null) {
                workspace?.updateTranslation(
                    view.translationTable?.selectionModel?.selectedIndex ?: return@success,
                    outLang,
                    translated
                )
            }
        } fail {
            val d = Alert(Alert.AlertType.ERROR)
            d.headerText = "Failed to retrieve answer. Maybe you are offline ?"
            d.contentText = it.toString()
            it.printStackTrace()
            d.showAndWait()
            view.status = ("failed to retrieve translation")

        }
    }

    fun onEditCommit(event: TableColumn.CellEditEvent<TranslationMap.TranslationRow, Any>) {
        val workspace = this.workspace ?: return
        val language = event.tableColumn.language ?: return
        workspace.updateTranslation(event.rowValue.key, language, event.newValue as String)
    }

    fun findReplace() {
        TODO("Find replace not implemented yet")
/*
        val findReplaceDialog: FindReplaceDialog
        val table = view.translationTable ?: return
        val availableLanguages = workspace?.translationData?.languages ?: return
        val focusModel = table.focusModel
        val focusedItem = focusModel.focusedItem as? TranslationMap.TranslationRow ?: return
        findReplaceDialog = FindReplaceDialog(availableLanguages)
        findReplaceDialog.setRegex(focusedItem.get(Language("en_us")) ?: "")
        if (focusModel.focusedCell.tableColumn != null) {
            val selectedLang = focusModel.focusedCell.tableColumn.language
            findReplaceDialog.setToLang(selectedLang)
            findReplaceDialog.setReplace(focusedItem[selectedLang] as String)
        }
        findReplaceDialog.showAndWait().ifPresent({ params ->
            workspace.searchReplace(
                params.fromLang,
                params.toLang,
                params.regex,
                params.replace,
                params.replaceExistingTranslations
            )
        })
        table.refresh()
*/
    }
}