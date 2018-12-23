package ladysnake.translationhelper.model.workspace

import javafx.application.Platform
import ladysnake.translationhelper.model.TranslationLoader
import ladysnake.translationhelper.model.data.Language
import ladysnake.translationhelper.model.data.LanguageMap
import ladysnake.translationhelper.model.data.MultiLangMap
import ladysnake.translationhelper.model.data.TranslationMap
import ladysnake.translationhelper.model.transaction.*
import tornadofx.runAsync
import java.io.File
import java.util.regex.Pattern

class TranslationWorkspace private constructor(
    val folder: File,
    val translationData: TranslationMap,
    val sourceFiles: SourcesMap
) {
    val transactionManager = TransactionManager(this)

    companion object {
        fun load(langFolder: File, sourceFiles: SourcesMap): TranslationWorkspace {
            if (!langFolder.isDirectory) {
                throw IllegalArgumentException("$langFolder is not a directory")
            }
            val data = MultiLangMap()
            for (file in sourceFiles.values) {
                val langData = TranslationLoader.load(file) ?: continue
                data += langData
            }
            val translationData = data.toTranslationMap()
            translationData.addLanguageUpdateListener { sourceFiles[it.key].markDirty() }
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

    fun addTranslationRow(key: String, row: TranslationMap.TranslationRow? = null) {
        transactionManager.run(InsertRow(key, row))
    }

    fun deleteTranslation(key: String) {
        transactionManager.run(DeleteRow(key))
    }

    fun updateTranslation(
        index: Int,
        language: Language,
        text: String,
        oldValue: String = translationData[index][language] ?: ""
    ) {
        val row = translationData[index]
        updateTranslation(row.key, language, text, oldValue)
    }

    fun updateTranslation(
        key: String,
        language: Language,
        newValue: String,
        oldValue: String = translationData[key, language] ?: ""
    ) {
        if (sourceFiles[language].isEditable) {
            transactionManager.run(Update(key, language, newValue, oldValue))
        }
    }

    fun updateTranslationKey(oldKey: String, newKey: String) {
        transactionManager.run(UpdateKey(oldKey, newKey))
    }

    fun searchReplace(
        fromLang: Language,
        toLang: Language,
        regex: Pattern,
        replacePattern: String,
        replaceExistingTranslations: Boolean
    ) {
        val translationList = translationData
        for ((i, translationRow) in translationList.withIndex()) {
            if (!translationRow.containsKey(fromLang) || translationRow.containsKey(toLang) && !replaceExistingTranslations) continue
            val matcher = regex.matcher(translationRow[fromLang])
            if (matcher.matches()) {
                var replace = replacePattern
                for (j in 0..matcher.groupCount())
                    replace = replace.replace("$$j", matcher.group(j))
                this.updateTranslation(i, toLang, replace, translationRow[toLang] ?: "")
            }
        }
    }
}