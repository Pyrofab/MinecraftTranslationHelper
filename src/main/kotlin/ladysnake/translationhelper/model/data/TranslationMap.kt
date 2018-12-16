package ladysnake.translationhelper.model.data

import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap

class TranslationMap(
    private val translations: ObservableMap<String, TranslationRow> = FXCollections.observableHashMap()
): ObservableMap<String, TranslationMap.TranslationRow> by translations {

    var languages: Set<Language> = setOf()
        private set

    init {
        translations.addListener(MapChangeListener {
            languages = translations.values.flatMap(TranslationRow::keys).toSet()
        })
    }

    fun toLanguageMap(): MultiLangMap {
        val languageMap = MultiLangMap(languages)
        forEach { key, row ->
            row.forEach { lang, value ->
                languageMap[lang, key] = value
            }
        }
        return languageMap
    }

    operator fun plusAssign(translationRow: TranslationRow) {
        translations[translationRow.key] = translationRow
    }

    operator fun get(key: String, language: Language): String? {
        return translations[key]?.get(language)
    }

    operator fun set(key: String, language: Language, value: String) {
        translations.computeIfAbsent(key) { TranslationRow(key) }[language] = value
    }

    override fun toString(): String {
        return "TranslationMap${translations.values}"
    }

    inner class TranslationRow internal constructor(
        val key: String,
        private val localized: ObservableMap<Language, String> = FXCollections.observableHashMap()
    ): ObservableMap<Language, String> by localized {
        init {
            localized.addListener(MapChangeListener {
                languages = translations.values.flatMap(TranslationRow::keys).toSet()
            })
        }

        override fun toString(): String {
            return "$key -> $localized"
        }

    }
}

