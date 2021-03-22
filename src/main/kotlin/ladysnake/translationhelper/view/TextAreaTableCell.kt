package ladysnake.translationhelper.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.scene.control.TableCell
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.text.Text
import javafx.util.StringConverter

class TextAreaTableCell<S, T>(converter: StringConverter<T>? = null) : TableCell<S, T>() {

    private fun resizeToFitText() {
        // Hack: to find out the text's actual height, we use a temporary Text widget
        val txt = Text("${this.text}\n")
        txt.wrappingWidth = this.tableColumn.width  // not using this cell's width, it can be 0 during init
        this.prefHeight = txt.layoutBounds.height
    }

    private var textArea: TextArea? = null
    private val converter: ObjectProperty<StringConverter<T>?> = SimpleObjectProperty(this, "converter")
    fun converterProperty(): ObjectProperty<StringConverter<T>?> {
        return converter
    }

    fun setConverter(value: StringConverter<T>?) {
        converterProperty().set(value)
    }

    fun getConverter(): StringConverter<T>? {
        return converterProperty().get()
    }

    override fun startEdit() {
        if (!isEditable || !tableView.isEditable || !tableColumn.isEditable) {
            return
        }
        super.startEdit()
        if (isEditing) {
            val textArea = this.textArea ?: this.createTextArea().also { this.textArea = it }
            textArea.text = getItemText()
            this.text = null
            this.graphic = textArea
            textArea.selectAll()
            textArea.requestFocus()
        }
    }

    private fun getItemText() = getConverter()?.toString(this.item) ?: this.item?.toString() ?: ""

    private fun createTextArea(): TextArea {
        val converter = getConverter()
        val textArea = TextArea(getItemText())
        textArea.addEventFilter(KeyEvent.KEY_PRESSED) { t: KeyEvent ->
            if (t.code == KeyCode.ENTER && !t.isShiftDown) {
                checkNotNull(converter) {
                    ("Attempting to convert text input into Object, but provided "
                            + "StringConverter is null. Be sure to set a StringConverter "
                            + "in your cell factory.")
                }
                this.commitEdit(converter.fromString(textArea.text))
                t.consume()
            }
        }
        textArea.onKeyReleased = EventHandler { t: KeyEvent ->
            if (t.code == KeyCode.ESCAPE) {
                this.cancelEdit()
                t.consume()
            }
        }
        textArea.isWrapText = true
        return textArea
    }

    override fun cancelEdit() {
        super.cancelEdit()
        this.text = getItemText()
        this.graphic = null
    }

    override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)
        if (this.isEmpty) {
            this.text = null
            this.setGraphic(null)
        } else {
            if (this.isEditing) {
                val textArea = textArea
                if (textArea != null) {
                    textArea.text = getItemText()
                }
                this.text = null
                this.setGraphic(textArea)
            } else {
                this.text = getItemText()
                this.setGraphic(null)
            }
        }
        this.resizeToFitText()
    }

    init {
        styleClass.add("text-area-table-cell")
        setConverter(converter)
    }
}