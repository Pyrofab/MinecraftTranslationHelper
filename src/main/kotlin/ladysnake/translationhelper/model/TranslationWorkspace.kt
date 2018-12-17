package ladysnake.translationhelper.model

import ladysnake.translationhelper.model.data.Language
import ladysnake.translationhelper.model.data.MultiLangMap
import ladysnake.translationhelper.model.data.TranslationMap
import java.io.File

class TranslationWorkspace private constructor(
    val folder: File,
    val translations: TranslationMap,
    val sourceFiles: MutableMap<Language, File>
) {
    companion object {
        fun load(langFolder: File): TranslationWorkspace {
            if (!langFolder.isDirectory) {
                throw IllegalArgumentException("$langFolder is not a directory")
            }
            val data = MultiLangMap()
            val sourceFiles = mutableMapOf<Language, File>()
            for (file in langFolder.listFiles()) {
                val langData = TranslationLoader.load(file) ?: continue
                data += langData
                sourceFiles[langData.language] = file
            }
            return TranslationWorkspace(langFolder, data.toTranslationMap(), sourceFiles)
        }
    }

    fun save() {
        val toSave = translations.toLanguageMap()
        for (lang in toSave.values) {
            TranslationLoader.save(lang, sourceFiles[lang.language] ?: File(folder, "${lang.language}.json"))
        }
    }

    override fun toString(): String {
        return "TranslationWorkspace(folder=$folder, translations=$translations)"
    }


}