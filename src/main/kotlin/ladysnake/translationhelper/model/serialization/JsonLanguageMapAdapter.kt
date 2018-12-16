package ladysnake.translationhelper.model.serialization

import ladysnake.translationhelper.model.data.LanguageMap
import java.util.regex.Pattern

class JsonLanguageMapAdapter : AbstractLanguageMapAdapter("json", JSON_PATTERN) {

    companion object {
        val JSON_PATTERN: Pattern = Pattern.compile(""""(?<key>.+)": "(?<value>.*)"""")
    }

    override fun serialize(languageMap: LanguageMap): String {
        return languageMap.entries.joinToString(separator = ",\n", prefix = "{\n", postfix = "\n}") { (key, value) -> "    $key:$value" }
    }
}