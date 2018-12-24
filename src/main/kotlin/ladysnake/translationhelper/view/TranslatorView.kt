package ladysnake.translationhelper.view

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.ToggleButton
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.image.Image
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.util.converter.DefaultStringConverter
import ladysnake.translationhelper.UserSettings
import ladysnake.translationhelper.controller.TranslationController
import ladysnake.translationhelper.controller.TranslationController.clearSelectedCell
import ladysnake.translationhelper.controller.TranslationController.copySelectedCell
import ladysnake.translationhelper.controller.TranslationController.pasteInSelectedCell
import ladysnake.translationhelper.model.TranslationLoader
import ladysnake.translationhelper.model.data.Language
import ladysnake.translationhelper.model.data.TranslationMap
import ladysnake.translationhelper.model.workspace.SourceFile
import ladysnake.translationhelper.model.workspace.SourcesMap
import tornadofx.*
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.*

class TranslatorView : View() {
    var translationTable: TableView<TranslationMap.TranslationRow>? = null
        private set
    val canUndo: BooleanProperty = SimpleBooleanProperty(false)
    val canRedo: BooleanProperty = SimpleBooleanProperty(false)

    private val statusProperty: StringProperty = SimpleStringProperty("status: no lang folder selected")
    var status: String by statusProperty
    private val notEditingProperty: BooleanProperty = SimpleBooleanProperty(true)
    var isNotEditing: Boolean by notEditingProperty
    private val langDirChooser: DirectoryChooser = DirectoryChooser().apply {
        title = "Choose lang folder"
        initialDirectory = File(".")
    }
    private val newLangChooser: FileChooser = FileChooser().apply {
        extensionFilters += FileChooser.ExtensionFilter("JSON language file", "json")
        extensionFilters += FileChooser.ExtensionFilter("Plain language file", "lang")
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
                        action {
                            val newLangFile =
                                newLangChooser.showSaveDialog(currentStage)
                            if (newLangFile != null) {
                                if (!newLangChooser.selectedExtensionFilter?.extensions.isNullOrEmpty()) {
                                    val actualFile = if (TranslationLoader.supports(newLangFile)) { newLangFile } else {
                                        File("$newLangFile.${newLangChooser.selectedExtensionFilter.extensions[0]}")
                                    }
                                    val (lang, file) = TranslationController.createLangFile(actualFile)
                                    translationTable?.genLangColumn(lang, file)
                                }
                            }
                        }
                    }
                    disableProperty().bind(notEditingProperty)
                }
                item("Open", "Shortcut+O").action {
                    val langFolder =
                        langDirChooser.showDialog(currentStage)
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
                                val langFolder = langDirChooser.showDialog(currentStage)
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
                item("Smart Replace", "Shortcut+R") {
                    action { TranslationController.findReplace()}
                }
                separator()
                item("Add translation key").action { TranslationController.addTranslationKey() }
                item("Change translation key").action { TranslationController.editTranslationKey() }
                item("Delete translation key").action { TranslationController.removeTranslationKey() }
                separator()
                item("Ask Google", "Shortcut+J") {
                    action { TranslationController.joker() }
                }
                disableProperty().bind(notEditingProperty)
            }
            menu("_Help...") {
                isMnemonicParsing = true
                item("README") {
                    action { Desktop.getDesktop().browse(URI("https://github.com/Pyrofab/MinecraftTranslationHelper/blob/master/README.md")) }
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
            langDirChooser.initialDirectory = langFolder.parentFile
            newLangChooser.initialDirectory = langFolder
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
                genLangColumn(lang, source)
            }
            selectionModel.isCellSelectionEnabled = true
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            selectionModel.select(0)
            contextmenu {
                item("Insert row").action { TranslationController.addTranslationKey(selectionModel.selectedIndex) }
                item("Delete row").action { TranslationController.removeTranslationKey() }
                item("Change translation key").action { TranslationController.editTranslationKey() }
            }
            onEditCommit { TranslationController.onEditCommit(this) }
            requestFocus()
        }.also { translationTable = it }
        isNotEditing = false
    }

    private fun TableView<TranslationMap.TranslationRow>.genLangColumn(
        lang: Language,
        source: SourceFile
    ) {
        column(lang.name, valueProvider = { cell: TableColumn.CellDataFeatures<TranslationMap.TranslationRow, String> ->
            SimpleStringProperty(cell.value[lang])
        }).apply {
            isEditable = true
            prefWidth = 300.0
            editableProperty().bind(source.editableProperty)
            properties[COLUMN_LANGUAGE] = lang
            source.changedProperty.addListener { _, _, changed -> text = if (changed) "${lang.name}*" else lang.name }
            graphic = ToggleButton().apply {
                setPrefSize(12.0, 12.0)
                addClass(AppStyle.lockButton)
                isSelected = source.isEditable
                source.editableProperty.bindBidirectional(selectedProperty())
            }
            setCellFactory {
                TextFieldTableCell<TranslationMap.TranslationRow, String>(DefaultStringConverter())
            }
        }
    }

}
