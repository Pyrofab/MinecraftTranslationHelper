package ladysnake.translationhelper.model

import java.util.regex.Pattern


private val WILDCARD_PATTERN = Pattern.compile("""(.*?)\*""")

fun wildcardToRegex(wildcard: String): Pattern {
    return wildcardToRegex(wildcard, true)
}

fun wildcardToRegex(wildcard: String, allowRawRegex: Boolean): Pattern {
    if (allowRawRegex && wildcard[0] == '/' && wildcard[wildcard.length - 1] == '/') {
        return Pattern.compile(wildcard.substring(1, wildcard.length - 1))
    }
    val m = WILDCARD_PATTERN.matcher(wildcard)
    val regex: String
    if (m.find()) {
        regex = m.replaceAll("""$1\\E(.*)\\Q""") // Text before the wildcard, followed by an unquoted regex wildcard
    } else {
        regex = wildcard
    }
    return regex.toQuotedRegex()
}

fun String.toQuotedRegex(): Pattern = Pattern.compile("""\Q$this\E""")