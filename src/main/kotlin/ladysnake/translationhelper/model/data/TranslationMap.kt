package ladysnake.translationhelper.model.data

import javafx.collections.*

class TranslationMap(
    private val translations: ObservableList<TranslationRow> = FXCollections.observableArrayList()
): ObservableList<TranslationMap.TranslationRow> by translations {

    var languages: Set<Language> = setOf()
        private set
    private val languageUpdateListeners: MutableList<(MapChangeListener.Change<out Language, out String>) -> Unit> = mutableListOf()

    init {
        translations.addListener(ListChangeListener {
            languages = translations.flatMap(TranslationRow::keys).toSet()
        })
    }

    fun addLanguageUpdateListener(listener: (MapChangeListener.Change<out Language, out String>) -> Unit) {
        languageUpdateListeners += listener
        translations.forEach { it.addListener(MapChangeListener { event -> listener(event) }) }
    }

    fun toLanguageMap(): MultiLangMap {
        val languageMap = MultiLangMap(languages)
        translations.forEach { row ->
            row.forEach { lang, value ->
                if (!value.isEmpty()) {
                    languageMap[lang, row.key] = value
                }
            }
        }
        return languageMap
    }

    operator fun get(key: String): TranslationRow? {
        return find { it.key == key }
    }

    operator fun get(key: String, language: Language): String? {
        return this[key]?.get(language)
    }

    operator fun set(key: String, language: Language, value: String) {
        val translationRow = this[key] ?:  TranslationRow(key).also {
            translations += it
            it.addListener(MapChangeListener { change ->
                languages = translations.flatMap(TranslationRow::keys).toSet()
                languageUpdateListeners.forEach { it(change) }
            })
        }
        translationRow[language] = value
    }

    override fun toString(): String {
        return "TranslationMap$translations"
    }

    inner class TranslationRow internal constructor(
        val key: String,
        private val localized: ObservableMap<Language, String> = FXCollections.observableHashMap()
    ): ObservableMap<Language, String> by localized {

        override fun toString(): String {
            return "$key -> $localized"
        }

    }
}

