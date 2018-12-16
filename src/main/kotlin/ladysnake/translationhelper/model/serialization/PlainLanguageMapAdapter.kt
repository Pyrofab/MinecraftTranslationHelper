package ladysnake.translationhelper.model.serialization

import ladysnake.translationhelper.model.data.LanguageMap
import java.util.regex.Pattern

class PlainLanguageMapAdapter : AbstractLanguageMapAdapter("lang", LANG_PATTERN) {
    companion object {
        val LANG_PATTERN: Pattern = Pattern.compile("""(?<key>.+)=(?<value>.*)""")
    }

    override fun serialize(languageMap: LanguageMap): String {
        return languageMap.entries.joinToString(separator = "\n") { (key, value) -> "$key=$value" }
    }
}