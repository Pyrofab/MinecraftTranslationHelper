package ladysnake.translationhelper.model.serialization

import ladysnake.translationhelper.model.data.LanguageMap
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Stream

abstract class AbstractLanguageMapAdapter(val fileExtension: String, val pattern: Pattern): LanguageMapAdapter {

    override fun accepts(fileExtension: String): Boolean {
        return this.fileExtension == fileExtension
    }

    override fun deserialize(lines: Stream<String>, into: LanguageMap) {
        lines.map(pattern::matcher).filter(Matcher::matches).forEach { matcher ->
            into[matcher.group("key")] = matcher.group("value")
        }
    }
}