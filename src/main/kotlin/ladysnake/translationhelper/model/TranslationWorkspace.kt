package ladysnake.translationhelper.model

import ladysnake.translationhelper.model.data.TranslationMap
import java.io.File

class TranslationWorkspace private constructor(val folder: File, val translations: TranslationMap) {
    companion object {
        fun load(folder: File): TranslationWorkspace {
            val data = TranslationLoader.loadFolder(folder)
            return TranslationWorkspace(folder, data.toTranslationMap())
        }
    }

    fun save() {
        val toSave = translations.toLanguageMap()
        for (lang in toSave.values) {
            TranslationLoader.save(lang, File(folder, "${lang.language}.json"))
        }
    }

    override fun toString(): String {
        return "TranslationWorkspace(folder=$folder, translations=$translations)"
    }


}