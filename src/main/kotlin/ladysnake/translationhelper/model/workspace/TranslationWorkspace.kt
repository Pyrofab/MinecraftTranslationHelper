package ladysnake.translationhelper.model.workspace

import javafx.application.Platform
import ladysnake.translationhelper.model.TranslationLoader
import ladysnake.translationhelper.model.data.Language
import ladysnake.translationhelper.model.data.LanguageMap
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
            val translationData = data.toTranslationMap()
            translationData.addLanguageUpdateListener { sourceFiles[it.key].hasChanged = true }
            return TranslationWorkspace(
                langFolder,
                translationData,
                sourceFiles
            )
        }
    }

    fun save() {
        val savedLanguages = translationData.toLanguageMap()
        runAsync {
            for (lang in savedLanguages.values) {
                if (sourceFiles[lang.language].hasChanged) {
                    save(lang)
                }
            }
        }
    }

    private fun save(lang: LanguageMap) {
        TranslationLoader.save(lang, sourceFiles[lang.language])
        println("$lang has been saved")
        Platform.runLater { sourceFiles[lang.language].hasChanged = false }
    }

    fun export(outputFolder: File, extension: String) {
        val exportedLanguages = translationData.toLanguageMap()
        runAsync {
            for (lang in exportedLanguages.values) {
                // If the exported file is already in the workspace, just save it
                val sourceFile = sourceFiles[lang.language]
                if (sourceFile.extension == extension && outputFolder == sourceFile.parentFile) {
                    save(lang)
                } else {
                    TranslationLoader.save(lang, File(outputFolder, "${lang.language}.$extension"))
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