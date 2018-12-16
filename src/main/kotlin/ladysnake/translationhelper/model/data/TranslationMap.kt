package ladysnake.translationhelper.model.data

import javafx.collections.*

class TranslationMap(
    private val translations: ObservableList<TranslationRow> = FXCollections.observableArrayList()
): ObservableList<TranslationMap.TranslationRow> by translations {

    var languages: Set<Language> = setOf()
        private set

    init {
        translations.addListener(ListChangeListener {
            languages = translations.flatMap(TranslationRow::keys).toSet()
        })
    }

    fun toLanguageMap(): MultiLangMap {
        val languageMap = MultiLangMap(languages)
        translations.forEach { row ->
            row.forEach { lang, value ->
                languageMap[lang, row.key] = value
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
        val translationRow = this[key] ?:  TranslationRow(key).also { translations += it }
        translationRow[language] = value
    }

    override fun toString(): String {
        return "TranslationMap$translations"
    }

    inner class TranslationRow internal constructor(
        val key: String,
        private val localized: ObservableMap<Language, String> = FXCollections.observableHashMap()
    ): ObservableMap<Language, String> by localized {
        init {
            localized.addListener(MapChangeListener {
                languages = translations.flatMap(TranslationRow::keys).toSet()
            })
        }

        override fun toString(): String {
            return "$key -> $localized"
        }

    }
}

