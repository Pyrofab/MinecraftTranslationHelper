package ladysnake.translatorhelper.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class allows simple translation operations
 * @author Fabien
 *
 */
public class TranslateAPI {
	
	private static final Pattern resultParser = Pattern.compile(".*?\"(.*?)\".*");
	private static final String userAgent="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/53.0.2785.143 Chrome/53.0.2785.143 Safari/537.36";
	
	
	public static String translate(String sourceText, String targetLang) throws IOException {
		return translate(sourceText, targetLang, "en");
	}
	
	public static String translate(String sourceText, String targetLang, String sourceLang) throws IOException {
		String ret = "";
		if(!sourceText.trim().isEmpty()) {
			String uriString = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" 
			        + sourceLang + "&tl=" + targetLang + "&dt=t&q=" + URLEncoder.encode(sourceText, "UTF-8");
			System.out.println(uriString);
			URL url = new URL(uriString);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", userAgent);
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String res = "";
			while(reader.ready())
				res += reader.readLine();
			reader.close();
			System.out.println(res);
			Matcher m3 = resultParser.matcher(res);
			if(m3.matches())
				ret = m3.group(1);
		}
		return ret;
	}

}
