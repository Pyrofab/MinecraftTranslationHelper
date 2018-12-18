package ladysnake.translationhelper.controller

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.input.Clipboard
import javafx.util.Duration
import ladysnake.translationhelper.UserSettings
import ladysnake.translationhelper.model.TranslateAPI
import ladysnake.translationhelper.model.data.Language
import ladysnake.translationhelper.model.data.TranslationMap
import ladysnake.translationhelper.model.workspace.SourcesMap
import ladysnake.translationhelper.model.workspace.TranslationWorkspace
import ladysnake.translationhelper.view.TranslatorView
import ladysnake.translationhelper.view.language
import tornadofx.confirmation
import tornadofx.find
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

    fun chooseFolder(langFolder: File, lockedFiles: SourcesMap): ChooseFolderResult {
        return try {
            val workspace = TranslationWorkspace.load(langFolder, lockedFiles)
            println(workspace)
            this.workspace = workspace
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

    fun pasteInto(row: Int, column: TableColumn<*,*>) {
        workspace?.updateTranslation(
            row,
            column.language ?: return,
            Clipboard.getSystemClipboard().string
        )
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun removeTranslationKey() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun addTranslationKey() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun createFile() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun joker() {
        val table = view.root.center as? TableView<*> ?: return
        val workspace = this.workspace ?: return
        val outLang = table.selectionModel.selectedCells[0].tableColumn.language ?: return
        val translated = TranslateAPI.translate(
            workspace.translationData[table.selectionModel.selectedIndex][Language("en_us")] ?: "",
            outLang.name
        )
        Platform.runLater { workspace.updateTranslation(
            table.selectionModel.selectedIndex,
            outLang,
            translated
        )}
    }

    fun onEditCommit(event: TableColumn.CellEditEvent<TranslationMap.TranslationRow, Any>) {
        val workspace = this.workspace ?: return
        val language = event.tableColumn.language ?: return
        workspace.updateTranslation(event.rowValue.key, language, event.newValue as String)
    }

    fun onSort() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}