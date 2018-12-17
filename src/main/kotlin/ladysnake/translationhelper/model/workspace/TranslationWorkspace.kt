package ladysnake.translationhelper.model.workspace

import ladysnake.translationhelper.model.TranslationLoader
import ladysnake.translationhelper.model.data.Language
import ladysnake.translationhelper.model.data.MultiLangMap
import ladysnake.translationhelper.model.data.TranslationMap
import ladysnake.translationhelper.model.transaction.TransactionManager
import ladysnake.translationhelper.model.transaction.Update
import tornadofx.runAsync
import java.io.File

class TranslationWorkspace private constructor(
    val folder: File,
    val translationData: TranslationMap,
    val sourceFiles: SourcesMap
) {
    val transactionManager = TransactionManager(this)

    companion object {
        fun load(langFolder: File): TranslationWorkspace {
            if (!langFolder.isDirectory) {
                throw IllegalArgumentException("$langFolder is not a directory")
            }
            val data = MultiLangMap()
            val sourceFiles = SourcesMap()
            for (file in langFolder.listFiles()) {
                val langData = TranslationLoader.load(file) ?: continue
                data += langData
                sourceFiles[langData.language] = file.toSourceFile()
            }
            return TranslationWorkspace(
                langFolder,
                data.toTranslationMap(),
                sourceFiles
            )
        }
    }

    fun save() {
        val toSave = translationData.toLanguageMap()
        runAsync {
            for (lang in toSave.values) {
                if (sourceFiles[lang.language].hasChanged) {
                    TranslationLoader.save(lang, sourceFiles[lang.language])
                }
            }
        }
    }

    override fun toString(): String {
        return "TranslationWorkspace(folder=$folder, translationData=$translationData)"
    }

    fun updateTranslation(index: Int, language: Language, text: String) {
        val row = translationData[index]
        updateTranslation(row.key, language, text, oldValue = row[language])
    }

    fun updateTranslation(key: String, language: Language, text: String) {
        updateTranslation(key, language, text, oldValue = translationData[key]?.get(language))
    }

    private fun updateTranslation(key: String, language: Language, newValue: String, oldValue: String?) {
        if (sourceFiles[language].isEditable) {
            transactionManager.run(Update(key, language, newValue, oldValue ?: ""))
        }
    }
}