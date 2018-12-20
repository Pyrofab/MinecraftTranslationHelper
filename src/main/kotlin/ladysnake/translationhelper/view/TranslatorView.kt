package ladysnake.translationhelper.view

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.image.Image
import javafx.stage.DirectoryChooser
import javafx.util.converter.DefaultStringConverter
import ladysnake.translationhelper.UserSettings
import ladysnake.translationhelper.controller.TranslationController
import ladysnake.translationhelper.controller.TranslationController.clearSelectedCell
import ladysnake.translationhelper.controller.TranslationController.copySelectedCell
import ladysnake.translationhelper.controller.TranslationController.pasteInSelectedCell
import ladysnake.translationhelper.model.TranslationLoader
import ladysnake.translationhelper.model.data.TranslationMap
import ladysnake.translationhelper.model.workspace.SourcesMap
import tornadofx.*
import java.io.File
import java.util.*

class TranslatorView : View() {
    val translationTable: TableView<*>? get() = root.center as? TableView<*>
    val canUndo: BooleanProperty = SimpleBooleanProperty(false)
    val canRedo: BooleanProperty = SimpleBooleanProperty(false)

    private val statusProperty: StringProperty = SimpleStringProperty("status: no lang folder selected")
    var status: String by statusProperty
    private val notEditingProperty: BooleanProperty = SimpleBooleanProperty(true)
    var isNotEditing: Boolean by notEditingProperty
    private val fileChooser: DirectoryChooser = DirectoryChooser().apply {
        title = "Choose lang folder"
        initialDirectory = File(".")
    }

    override val root = borderpane {
        prefWidth = 900.0
        prefHeight = 400.0
        title = "Translation O Matik"
        addStageIcon(Image(TranslatorView::class.java.getResourceAsStream("/icon.png")))
        primaryStage.setOnCloseRequest { TranslationController.onExit(it) }

        top = menubar {
            menu("_File") {
                isMnemonicParsing = true
                menu("New") {
                    item("Lang file") {
                        action { TranslationController.createFile() }
                        disableProperty().bind(notEditingProperty)
                    }
                }
                item("Open", "Shortcut+O").action {
                    val langFolder =
                        fileChooser.showDialog(currentStage)
                    if (langFolder != null) {
                        loadLangFolder(langFolder)
                    }
                }
                separator()
                item("Save all", "Shortcut+S") {
                    disableProperty().bind(notEditingProperty)
                    action {
                        TranslationController.save()
                        println("Saved !")
                    }
                }
                menu("Export all") {
                    disableProperty().bind(notEditingProperty)
                    for (extension in TranslationLoader.knownExtensions) {
                        item(extension) {
                            action {
                                val langFolder = fileChooser.showDialog(currentStage)
                                if (langFolder != null) {
                                    status = "exporting lang files"
                                    TranslationController.export(langFolder, extension)
                                    status = "idle"
                                }
                            }
                        }
                    }
                }
                separator()
                checkmenuitem("Toggle autosave", selected = UserSettings.autosaveProperty)
                separator()
                item("Exit").action { Platform.exit() }
            }
            menu("_Edit") {
                isMnemonicParsing = true
                item("Undo", "Shortcut+Z") {
                    action {
                        TranslationController.undo()
                    }
                    disableProperty().bind(canUndo.not())
                }
                item("Redo", "Shortcut+Y") {
                    action {
                        TranslationController.redo()
                    }
                    disableProperty().bind(canRedo.not())
                }
                separator()
                item("Cut", "Shortcut+X").action { copySelectedCell().also { clearSelectedCell() } }
                item("Copy", "Shortcut+C").action { copySelectedCell() }
                item("Paste", "Shortcut+V").action { pasteInSelectedCell() }
                item("Delete", "Delete").action { clearSelectedCell() }
                separator()
                item("Find", "Shortcut+F")
                separator()
                item("Add translation key") {
                    action {
                        val d = TextInputDialog()
                        d.graphic = null
                        d.headerText = "Enter the new translation's key:"
                        d.title = "New translation"
                        val table = translationTable ?: return@action
                        table.selectionModel.clearSelection()
                        d.showAndWait()
                            .ifPresent(TranslationController::addTranslationKey)
                        table.sort()
                        table.requestFocus()
                    }
                }
                item("Change translation key").action { TranslationController.editTranslationKey() }
                item("Delete translation key").action { TranslationController.removeTranslationKey() }
                separator()
                item("Ask Google", "Shortcut+J") {
                    tooltip("Uses Google Translate to complete the cell based on the english value.")
                    action { TranslationController.joker() }
                }
                disableProperty().bind(notEditingProperty)
            }
            menu("_Help...") {
                isMnemonicParsing = true
                item("Maybe one day") {
                    action { }
                }
            }
        }

        bottom = label {
            textProperty().bind(statusProperty)
        }
    }

    fun loadLangFolder(langFolder: File) {
        status = "loading lang files"
        val langFiles = langFolder.listFiles { f -> TranslationLoader.supports(f) } ?: kotlin.error("Not a directory")
        val lockedFiles = SelectFilesDialog(Arrays.asList(*langFiles))
            .showAndWait().orElse(null) ?: return
        runAsync {
            TranslationController.chooseFolder(langFolder, lockedFiles)
        } success { (translationData, sourceFiles) ->
            fileChooser.initialDirectory = langFolder.parentFile
            status = if (translationData != null && sourceFiles != null) {
                genTable(translationData, sourceFiles)
                "idle"
            } else {
                "no lang folder selected"
            }
        } fail {
            status = "erred while reading the folder"
        }
    }

    private fun genTable(
        items: TranslationMap,
        sourceFiles: SourcesMap
    ) {
        root.center = tableview(items) {
            isEditable = true
            readonlyColumn("Lang Key", TranslationMap.TranslationRow::key)
            for (lang in items.languages) {
                val source = sourceFiles[lang]
                column(lang.name, valueProvider = {cell: TableColumn.CellDataFeatures<TranslationMap.TranslationRow, String> ->
                    SimpleStringProperty(cell.value[lang])
                }).apply {
                    isEditable = true
                    prefWidth = 300.0
                    editableProperty().bind(source.editableProperty)
                    properties[COLUMN_LANGUAGE] = lang
                    source.changedProperty.addListener { _, _, changed -> text = if (changed) "${lang.name}*" else lang.name }
                    graphic = ToggleButton().apply {
                        setPrefSize(12.0,12.0)
                        addClass(AppStyle.lockButton)
                        isSelected = source.isEditable
                        source.editableProperty.bindBidirectional(selectedProperty())
                    }
                    setCellFactory {
                        TextFieldTableCell<TranslationMap.TranslationRow, String>(DefaultStringConverter())
                    }
                }
            }
            selectionModel.isCellSelectionEnabled = true
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            selectionModel.select(0)
            contextmenu {
                item("Delete row").action { TranslationController.removeTranslationKey() }
                item("Change translation key").action { TranslationController.editTranslationKey() }
            }
            onEditCommit { TranslationController.onEditCommit(this) }
            requestFocus()
        }
        isNotEditing = false
    }

}
