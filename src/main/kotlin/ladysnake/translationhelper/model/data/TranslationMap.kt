package ladysnake.translationhelper.model.data

import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableList
import javafx.collections.ObservableMap

class TranslationMap(
    private val translations: ObservableList<TranslationRow> = FXCollections.observableArrayList()
): ObservableList<TranslationMap.TranslationRow> by translations {

    val languages: MutableSet<Language> = translations.flatMap(TranslationRow::keys).toMutableSet()
    private val languageUpdateListeners: MutableList<(MapChangeListener.Change<out Language, out String>) -> Unit> = mutableListOf()

    fun addLanguageUpdateListener(listener: (MapChangeListener.Change<out Language, out String>) -> Unit) {
        languageUpdateListeners += listener
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
            this.addListener(MapChangeListener { change ->
                languageUpdateListeners.forEach { it(change) }
                languages += change.key
            })
        }

        /**
         * Makes a copy of this translation row that uses [newKey]
         */
        fun withKey(newKey: String): TranslationRow {
            val map = FXCollections.observableHashMap<Language, String>()
            map.putAll(this)
            return TranslationRow(newKey, map)
        }

        override fun toString(): String {
            return "$key -> $localized"
        }

    }
}

