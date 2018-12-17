package ladysnake.translationhelper.model.transaction

import ladysnake.translationhelper.model.workspace.TranslationWorkspace
import java.util.*

class TransactionManager(private val workspace: TranslationWorkspace) {
    private val undoOperations: Deque<Transaction> = ArrayDeque()
    private val redoOperations: Deque<Transaction> = ArrayDeque()
    private val listeners: MutableList<(Transaction) -> Unit> = mutableListOf()

    fun addListener(listener: (Transaction) -> Unit) {
        listeners += listener
    }

    /**
     * [isUndo]: True when the operation is cancelling a former one -> pile onto redoOperations
     */
    fun run(op: Transaction, isUndo: Boolean = false) {
        op(workspace)
        if (isUndo) {
            redoOperations.push(op)
        } else {
            undoOperations.push(op)
        }
        listeners.forEach { it(op) }
    }

    fun undo() {
        val transaction = this.undoOperations.poll()?.inverse ?: return
        this.run(transaction, isUndo = true)
    }

    fun redo() {
        val transaction = this.redoOperations.poll()?.inverse ?: return
        this.run(transaction, isUndo = false)
    }

}