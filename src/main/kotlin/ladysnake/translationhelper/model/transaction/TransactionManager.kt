package ladysnake.translationhelper.model.transaction

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import ladysnake.translationhelper.model.workspace.TranslationWorkspace
import java.util.*

class TransactionManager(private val workspace: TranslationWorkspace) {
    private val undoOperations: Deque<Transaction> = ArrayDeque()
    private val redoOperations: Deque<Transaction> = ArrayDeque()
    private val listeners: MutableList<(Transaction) -> Unit> = mutableListOf()
    val canUndo: BooleanProperty = SimpleBooleanProperty(false)
    val canRedo: BooleanProperty = SimpleBooleanProperty(false)

    fun addListener(listener: (Transaction) -> Unit) {
        listeners += listener
    }

    fun run(op: Transaction, type: Type = Type.DEFAULT) {
        val inverse = op(workspace)
        if (type.isUndo) {
            redoOperations.push(inverse)
        } else {
            undoOperations.push(inverse)
            if (!type.isRedo) { // Overwrite the redo history
                redoOperations.clear()
            }
        }
        Platform.runLater {
            canUndo.set(!undoOperations.isEmpty())
            canRedo.set(!redoOperations.isEmpty())
        }
        listeners.forEach { it(op) }
    }

    fun undo() {
        val transaction = this.undoOperations.poll() ?: return
        this.run(transaction, Type.UNDO)
    }

    fun redo() {
        val transaction = this.redoOperations.poll() ?: return
        this.run(transaction, Type.REDO)
    }

    enum class Type {
        DEFAULT, UNDO, REDO;

        val isUndo: Boolean get() = this == UNDO
        val isRedo: Boolean get() = this == REDO
    }

}