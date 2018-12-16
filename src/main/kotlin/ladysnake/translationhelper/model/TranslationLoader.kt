package ladysnake.translationhelper.model

import ladysnake.translationhelper.model.data.Language
import ladysnake.translationhelper.model.data.LanguageMap
import ladysnake.translationhelper.model.data.MultiLangMap
import ladysnake.translationhelper.model.serialization.LanguageMapAdapter
import java.io.File
import java.nio.file.Files

object TranslationLoader {
    private val adapters: MutableList<LanguageMapAdapter> = mutableListOf()

    fun registerAdapter(adapter: LanguageMapAdapter) {
        adapters += adapter
    }

    fun loadFolder(langFolder: File): MultiLangMap {
        if (!langFolder.isDirectory) {
            throw IllegalArgumentException("$langFolder is not a directory")
        }
        val ret = MultiLangMap()
        for (file in langFolder.listFiles()) {
            ret += load(file)
        }
        return ret
    }

    fun load(langFile: File): LanguageMap {
        val locale = langFile.nameWithoutExtension
        val extension = langFile.extension
        val languageMap =
            LanguageMap(Language(locale))
        for (adapter in adapters) {
            if (adapter.accepts(extension)) {
                adapter.deserialize(Files.lines(langFile.toPath(), Charsets.UTF_8), languageMap)
                break
            }
        }
        return languageMap
    }

    fun save(languageMap: LanguageMap, langFile: File) {
        val extension = langFile.extension
        for (adapter in adapters) {
            if (adapter.accepts(extension)) {
                langFile.bufferedWriter().use {writer ->
                    writer.write(adapter.serialize(languageMap))
                }
            }
        }
    }
}