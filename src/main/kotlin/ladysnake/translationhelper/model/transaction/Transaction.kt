package ladysnake.translationhelper.model.transaction

import ladysnake.translationhelper.model.data.Language
import ladysnake.translationhelper.model.workspace.TranslationWorkspace

interface Transaction {
    val inverse: Transaction

    operator fun invoke(workspace: TranslationWorkspace)
}

class Update(private val key: String, private val language: Language, private val newVal: String, private val oldVal: String, parent: Transaction? = null): Transaction {
    override val inverse = parent ?: Update(key, language, newVal = this.oldVal, oldVal = this.newVal, parent = this)

    override fun invoke(workspace: TranslationWorkspace) {
        workspace.translationData[key, language] = newVal
    }
}