package ladysnake.translationhelper.controller

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.input.Clipboard
import javafx.stage.DirectoryChooser
import javafx.util.Duration
import ladysnake.translationhelper.UserSettings
import ladysnake.translationhelper.model.TranslateAPI
import ladysnake.translationhelper.model.data.Language
import ladysnake.translationhelper.model.data.TranslationMap
import ladysnake.translationhelper.model.workspace.SourcesMap
import ladysnake.translationhelper.model.workspace.TranslationWorkspace
import ladysnake.translationhelper.view.TranslatorView
import tornadofx.find
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.util.*

object TranslationController {
    internal val fileChooser: DirectoryChooser = DirectoryChooser().apply {
        title = "Open resource file"
        initialDirectory = File(".")
    }
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

    fun chooseFolder(langFolder: File): ChooseFolderResult {
        try {
            val workspace = TranslationWorkspace.load(langFolder)
            println(workspace)
            fileChooser.initialDirectory = langFolder.parentFile
            this.workspace = workspace
            return ChooseFolderResult(workspace.translationData, workspace.sourceFiles)
        } catch (e: NoSuchElementException) {
            System.err.println("Operation cancelled : " + e.localizedMessage)
        } catch (e: IOException) {
            System.err.println("The file selected isn't a valid folder")
            throw UncheckedIOException(e)
        }
        return ChooseFolderResult.NONE
    }

    data class ChooseFolderResult(val translationData: TranslationMap?, val sourceFiles: SourcesMap?) {
        companion object {
            val NONE = ChooseFolderResult(null, null)
        }
    }

    fun pasteInto(row: Int, column: TableColumn<*,*>) {
        workspace?.updateTranslation(
            row,
            Language(column.text),
            Clipboard.getSystemClipboard().string
        )
    }

    fun undo() = workspace?.transactionManager?.undo()

    fun redo() = workspace?.transactionManager?.redo()

    fun save() {
        workspace?.save()
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
        val translated = TranslateAPI.translate(
            workspace.translationData[table.selectionModel.selectedIndex][Language("en_us")] ?: "",
            table.selectionModel.selectedCells[0].tableColumn.text
        )
        Platform.runLater { workspace.updateTranslation(
            table.selectionModel.selectedIndex,
            Language((view.root.center as TableView<*>).selectionModel.selectedCells[0].tableColumn.text),
            translated
        )}
    }

    fun onEditCommit(event: TableColumn.CellEditEvent<TranslationMap.TranslationRow, Any>) {
        val workspace = this.workspace ?: return
        workspace.updateTranslation(event.rowValue.key, Language(event.tableColumn.text), event.newValue as String)
    }

    fun onSort() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}