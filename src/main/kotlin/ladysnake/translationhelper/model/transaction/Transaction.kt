package ladysnake.translationhelper.model.transaction

import ladysnake.translationhelper.model.data.Language
import ladysnake.translationhelper.model.data.TranslationMap
import ladysnake.translationhelper.model.workspace.TranslationWorkspace

interface Transaction {
    /**
     * @return the transaction that undoes this one
     */
    operator fun invoke(workspace: TranslationWorkspace): Transaction
}

class Update(private val key: String, private val language: Language, private val newVal: String, private val oldVal: String, parent: Transaction? = null): Transaction {
    private val inverse: Transaction = parent ?: Update(key, language, newVal = this.oldVal, oldVal = this.newVal, parent = this)

    override fun invoke(workspace: TranslationWorkspace): Transaction {
        workspace.translationData[key, language] = newVal
        return inverse
    }
}

class InsertRow(private val key: String, private val row: TranslationMap.TranslationRow? = null, private val index: Int = 0, parent: Transaction? = null): Transaction {
    private val inverse: Transaction = parent ?: DeleteRow(key, parent = this)

    override fun invoke(workspace: TranslationWorkspace): Transaction {
        if (row != null) {
            workspace.translationData.add(index, row)
        } else {
            workspace.translationData[key, Language("en_us")] = ""
        }
        return inverse
    }
}

class DeleteRow(private val key: String, parent: Transaction? = null): Transaction {
    private var inverse: Transaction? = parent

    override fun invoke(workspace: TranslationWorkspace): Transaction {
        val row = workspace.translationData[key]
        val index = workspace.translationData.indexOf(row)
        workspace.translationData.remove(row)
        if (row != null) {
            for (lang in row.keys) {
                val source = workspace.sourceFiles[lang]
                source.isEditable = true
                source.markDirty()
            }
        }
        return inverse ?: InsertRow(key, row, index, parent = this).also { inverse = it }
    }
}

class UpdateKey(private val oldKey: String, private val newKey: String, parent: Transaction? = null): Transaction {
    private val inverse: Transaction = parent ?: UpdateKey(oldKey = this.newKey, newKey = this.oldKey, parent = this)

    override fun invoke(workspace: TranslationWorkspace): Transaction {
        val data = workspace.translationData
        var index = data.indexOfFirst { it.key == oldKey }
        if (index < 0) {
            index = data.lastIndex
        }
        for (lang in data[index].keys) {
            val source = workspace.sourceFiles[lang]
            source.isEditable = true
            source.markDirty()
        }
        data[index] = data[index].withKey(this.newKey)
        return this.inverse
    }
}