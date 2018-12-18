package ladysnake.translationhelper.view

import javafx.scene.control.TableColumn
import ladysnake.translationhelper.model.data.Language

const val COLUMN_LANGUAGE = "column-language"

val TableColumn<*,*>.language: Language? get() = properties[COLUMN_LANGUAGE] as? Language