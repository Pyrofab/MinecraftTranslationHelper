package ladysnake.translationhelper.view

import javafx.scene.control.TableColumn
import ladysnake.translationhelper.model.data.Language

const val COLUMN_LANGUAGE = "column-language"

val TableColumn<*,*>.language get() = properties[COLUMN_LANGUAGE] as Language