package ladysnake.translationhelper.view

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.*
import ladysnake.translationhelper.controller.TranslationController
import tornadofx.*

class TranslatorView : View() {
    private val statusProperty: StringProperty = SimpleStringProperty("status: no lang folder selected")
    var status: String by statusProperty


    private lateinit var wimpTrnslBtn: Button

    override val root = borderpane {
        prefWidth = 900.0
        prefHeight = 400.0
        title = "Translation O Matik"

        top = menubar {
            menu("File") {
                item("Open", "Shortcut+O").action {
                    TranslationController.chooseFolder()
                }
                separator()
                item("Save all", "Shortcut+S") {
                    isDisable = true
                    action {
                        TranslationController.save()
                        println("Saved !")
                    }
                }
                item("Save as", "Shortcut+Shift+S") {
                    isDisable = true
                    action {
                        println("Saved as...")
                    }
                }
                separator()
                item("Exit")
            }
            menu("Edit") {
                item("Undo")
                item("Redo")
                separator()
                item("Cut", "Shortcut+X")
                item("Copy", "Shortcut+C")
                item("Paste", "Shortcut+V")
                separator()
                item("Find", "Shortcut+F")
                separator()
                item("Joker", "Shortcut+J")
                isDisable = true
            }
            menu("Add...") {
                item("language file") {
                    action { TranslationController.createFile() }
                }
                item("translation key") {
                    action { TranslationController.addTranslationKey() }
                }
                isDisable = true
            }
            wimpTrnslBtn = button("Joker") {
                tooltip("Uses Google Translate to complete the cell based on the english value.")
                action { TranslationController.joker() }
                isDisable = true
            }
        }

        bottom = label {
            textProperty().bind(statusProperty)
        }

/*
        top = hbox(10) {
            alignment = Pos.CENTER
            padding = Insets(10.0)

            smartSearch = checkbox("Use smart search") {
                tooltip("Uses recursive search to find the most likely lang folder from a selected root.")
            }
            button("Load a lang folder") {
                action { TranslationController.chooseFolder() }
            }
            saveBtn = button("Save") {
                action { TranslationController.save() }
                isDisable = true
            }
            newThing = menubutton("Add...") {
                item("language file") {
                    action { TranslationController.createFile() }
                }
                item("translation key") {
                    action { TranslationController.addTranslationKey() }
                }
                isDisable = true
            }
            contextMenuTable = contextmenu {
                item("Delete row") {
                    action { TranslationController.removeTranslationKey() }
                }
                item("Change translation key") {
                    action { TranslationController.editTranslationKey() }
                }
            }
        }
*/
    }
}
