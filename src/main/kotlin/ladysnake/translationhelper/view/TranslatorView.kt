package ladysnake.translationhelper.view

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.image.Image
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.stage.DirectoryChooser
import javafx.util.converter.DefaultStringConverter
import ladysnake.translationhelper.UserSettings
import ladysnake.translationhelper.controller.TranslationController
import ladysnake.translationhelper.model.TranslationLoader
import ladysnake.translationhelper.model.data.TranslationMap
import ladysnake.translationhelper.model.workspace.SourcesMap
import tornadofx.*
import java.io.File

class TranslatorView : View() {
    val translationTable: TableView<*>? get() = root.center as? TableView<*>

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

        top = menubar {
            menu("_File") {
                isMnemonicParsing = true
                item("Open", "Shortcut+O").action {
                    val langFolder =
                        fileChooser.showDialog(currentStage)
                    if (langFolder != null) {
                        status = "loading lang files"
                        runAsync {
                            TranslationController.chooseFolder(langFolder)
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
                item("Undo", "Shortcut+Z").action {
                    TranslationController.undo()
                }
                item("Redo", "Shortcut+Y").action {
                    TranslationController.redo()
                }
                separator()
                item("Cut", "Shortcut+X").action {

                }
                item("Copy", "Shortcut+C").action {
                    val table = center as? TableView<*> ?: return@action
                    val tablePosition = table.focusModel.focusedCell
                    val row = table.items[tablePosition.row] as? TranslationMap.TranslationRow ?: return@action
                    val content = ClipboardContent()
                    val contentString = row[tablePosition.tableColumn.language]
                    println("copying $contentString")
                    content.putString(contentString)
                    content.putHtml("<td>$contentString</td>")
                    Clipboard.getSystemClipboard().setContent(content)
                }
                item("Paste", "Shortcut+V").action {
                    val tablePositions = translationTable?.selectionModel?.selectedCells ?: return@action
                    for (tablePosition in tablePositions) {
                        TranslationController.pasteInto(tablePosition.row, tablePosition.tableColumn)
                    }
                }
                separator()
                item("Find", "Shortcut+F")
                separator()
                item("Joker", "Shortcut+J") {
                    tooltip("Uses Google Translate to complete the cell based on the english value.")
                    action {
                        status = "fetching translation"
                        runAsync {
                            TranslationController.joker()
                        } success {
                            status = "idle"
                        } fail {
                            val d = Alert(Alert.AlertType.ERROR)
                            d.headerText = "Failed to retrieve answer. Maybe you are offline ?"
                            d.contentText = it.toString()
                            it.printStackTrace()
                            d.showAndWait()
                            status = ("failed to retrieve translation")

                        }
                    }
                }
                disableProperty().bind(notEditingProperty)
            }
            menu("_Add...") {
                isMnemonicParsing = true
                item("language file") {
                    action { TranslationController.createFile() }
                }
                item("translation key") {
                    action { TranslationController.addTranslationKey() }
                }
                disableProperty().bind(notEditingProperty)
            }
        }

        bottom = label {
            textProperty().bind(statusProperty)
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
                        source.editableProperty.bind(selectedProperty())
                    }
                    setCellFactory {
                        TextFieldTableCell<TranslationMap.TranslationRow, String>(DefaultStringConverter())
                    }
                }
            }
            selectionModel.isCellSelectionEnabled = true
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            selectionModel.select(0)
            onEditCommit { TranslationController.onEditCommit(this) }
            requestFocus()
        }
        isNotEditing = false
    }

}
