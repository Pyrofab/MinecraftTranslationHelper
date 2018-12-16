package ladysnake.translationhelper.model.serialization

import ladysnake.translationhelper.model.data.LanguageMap
import java.util.stream.Stream

interface LanguageMapAdapter {
    fun accepts(fileExtension: String): Boolean

    fun serialize(languageMap: LanguageMap): String

    fun deserialize(lines: Stream<String>, into: LanguageMap)
}