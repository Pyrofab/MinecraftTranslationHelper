package ladysnake.translationhelper.model

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * This class allows simple translation operations
 * @author Fabien
 */
object TranslateAPI {

    private val resultParser = Pattern.compile(".*?\"(.*?)\".*")
    private val userAgent =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/53.0.2785.143 Chrome/53.0.2785.143 Safari/537.36"

    @Throws(IOException::class)
    @JvmOverloads
    fun translate(sourceText: String, targetLang: String, sourceLang: String = "en"): String {
        var ret = ""
        if (!sourceText.trim { it <= ' ' }.isEmpty()) {
            val uriString = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceLang&tl=$targetLang&dt=t&q=${URLEncoder.encode(
                sourceText,
                "UTF-8"
            )}"
            println(uriString)
            val url = URL(uriString)
            val conn = url.openConnection()
            conn.setRequestProperty("User-Agent", userAgent)
            val reader = BufferedReader(InputStreamReader(conn.getInputStream(), "UTF-8"))
            var res = ""
            while (reader.ready())
                res += reader.readLine()
            reader.close()
            println(res)
            val m3 = resultParser.matcher(res)
            if (m3.matches())
                ret = m3.group(1)
        }
        return ret
    }

}
