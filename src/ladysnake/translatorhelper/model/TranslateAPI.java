package ladysnake.translatorhelper.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * This class allows simple translation operations
 * @author Fabien
 *
 */
public class TranslateAPI {
	
	private static final Pattern resultParser = Pattern.compile(".*?\"(.*?)\".*");
	private static final String userAgent="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/53.0.2785.143 Chrome/53.0.2785.143 Safari/537.36";
	
	
	public static String translate(String sourceText, String targetLang) {
		return translate(sourceText, targetLang, "en");
	}
	
	public static String translate(String sourceText, String targetLang, String sourceLang) {
		String ret = "";
		try {
			String uriString = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" 
			        + sourceLang + "&tl=" + targetLang + "&dt=t&q=" + URLEncoder.encode(sourceText, "UTF-8");
			System.out.println(uriString);
			URL url = new URL(uriString);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", userAgent);
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String res = "";
				while(reader.ready())
					res += reader.readLine();
				Matcher m3 = resultParser.matcher(res);
				if(m3.matches())
					ret = m3.group(1);
			} catch (IOException e) {
				Alert d = new Alert(AlertType.ERROR);
				d.setHeaderText("Failed to retrieve answer. Maybe you are offline ?");
				d.setContentText(e.toString());
				System.err.println("Failed to retrieve answer from google translate servers");
				e.printStackTrace();
				d.showAndWait();
			}
		} catch (IOException e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setHeaderText("An unexpected error has occured");
			a.setContentText(e.toString());
			e.printStackTrace();
			a.showAndWait();
		}
		return ret;
	}
	
	/*
	"af_za", "Afrikaans",
	"ar_sa", "Arabic",
	"en_us", "English",
	"az_az", "Azerbaijani",
	"bg_bg", "Bulgarian",
	"ca_es", "Catalan",
	"cs_cz", "Czech",
	"da_dk", "Denmark",
	"de_de", "German",
	"el_gr", "Greek",
	"en_au", "English",
	"en_ca", "English",
	"en_gb", "English",
	"en_nz", "English",
	"en_us", "English",
	"eo_uy", "Esperanto",
	"es_ar", "Spanish",
	"es_es", "Spanish",
	"es_mx", "Spanish",
	"es_uy", "Spanish",
	"es_ve", "Spanish",
	"et_ee", "Estonian",
	"fa_ir", "Persian",
	"fi_fi", "Finnish",
	"fil_ph", "Filipino",
	"fr_ca", "French",
	"fr_fr", "French",
	"fy_nl", "frysian",
	"ga_ie", "Irish",
	"gl_es", "Galician",
	"hr_hr", "Croatian",
	"hu_hu", "Hungarian",
	"id_id", "Indonesian",
	"is_is", "Icelandic",
	"it_it", "Italian",
	"ja_jp", "Japanese",
	"ka_ge", "Georgian",
	"ko_kr", "Korean",
	"la_va", "Latin",
	"lb_lu", "Luxembourgish",
	"li_li", "Limburgish",
	"lt_lt", "Lithuanian",
	"lv_lv", "Latvian",
	"mi_nz", "Maori",
	"mk_mk", "Macedonian",
	"mn_mn", "Mongolian",
	"ms_my", "Malay",
	"mt_mt", "Maltese",
	"nb_no", "Norwegian",
	"nl_nl", "Dutch",
	"pl_pl", "Polish",
	"pt_br", "Portuguese",
	"pt_pt", "Portuguese",
	"ro_ro", "Romanian",
	"ru_ru", "Russian",
	"sk_sk", "Slovak",
	"sl_sl", "Slovenian",
	"so_so", "Somali",
	"sq_al", "Albanian",
	"sr_sp", "Serbian",
	"sv_se", "Swedish",
	"th_th", "Thai",
	"tr_tr", "Turkish",
	"uk_ua", "Ukrainian",
	"vi_vn", "Vietnamese",
	"zh_cn", "Chinese Simplified",
	"zh_tw", "Chinese Traditional"
	*/

}
