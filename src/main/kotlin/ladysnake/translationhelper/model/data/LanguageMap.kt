package ladysnake.translationhelper.model.data

class MultiLangMap(private val languages: MutableMap<Language, LanguageMap> = mutableMapOf()): MutableMap<Language, LanguageMap> by languages {
    constructor(locales: Set<Language>): this() {
        for (locale in locales) {
            languages += locale to LanguageMap(locale)
        }
    }

    operator fun plusAssign(languageMap: LanguageMap) {
        languages.computeIfAbsent(languageMap.language) { LanguageMap(it) }.putAll(languageMap)
    }

    operator fun get(language: Language, key: String): String? {
        return languages[language]?.get(key)
    }

    operator fun set(language: Language, key: String, value: String) {
        languages.computeIfAbsent(language) { LanguageMap(it) } [key] = value
    }

    fun toTranslationMap(): TranslationMap {
        val translationMap = TranslationMap()
        forEach { lang, entries ->
            entries.forEach { key, value ->
                translationMap[key, lang] = value
            }
        }
        return translationMap
    }

}

class LanguageMap(
    val language: Language,
    private val langEntries: MutableMap<String, String> = mutableMapOf()
): MutableMap<String, String> by langEntries