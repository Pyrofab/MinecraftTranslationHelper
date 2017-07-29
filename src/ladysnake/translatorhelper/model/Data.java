package ladysnake.translatorhelper.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Data {

	public static final String TRANSLATION_KEY = "translation key";
	
	/**key, language file, value*/
	private Map<String, Map<String, String>> translations;
	/**language file, key, value*/
	private Map<String, Map<String, String>> translationFiles;
	private Pattern translationPattern;

	public Data() {
		translations = new HashMap<>();
		translationFiles = new HashMap<>();
		translationPattern = Pattern.compile("(.*?)=(.*)");
	}
	
	public void load(File[] langFiles) {
		if(langFiles == null || langFiles.length == 0)
			return;
		
		for(File file : langFiles) {
			translationFiles.put(file.getName(), new TreeMap<>());
			try (BufferedReader reader = Files.newBufferedReader(file.toPath())){
				while(reader.ready()) {
					String line = reader.readLine();
					Matcher m = translationPattern.matcher(line);
					if(m.matches()) {
						String key = m.group(1);
						Map<String, String> values = translations.get(key);
						if(values == null) {
							values = new HashMap<>();
							translations.put(m.group(1), values);
						}
						values.put(file.getName(), m.group(2));
						translationFiles.get(file.getName()).put(key, m.group(2));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("================================");
		translations.forEach((key, values) -> System.out.println(key + "=" + values));
		System.out.println("================================");
		translationFiles.forEach((key, values) -> {
			System.out.println(key + ":");
			values.forEach((trKey, trValue) -> System.out.println(trKey + "=" + trValue));
			System.out.println("-------------------------");
		});
	}
	
	public void save(File folder) {
		translationFiles.forEach((file, values) -> {
			try (BufferedWriter writer = Files.newBufferedWriter(new File(folder, file).toPath())) {
				for (String key : values.keySet()) {
					writer.write(key + "=" + values.get(key));
					writer.newLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public ObservableList<Map<String, String>> generateDataInMap() {
        ObservableList<Map<String, String>> allData = FXCollections.observableArrayList();
        translations.forEach((translatKey, values) -> {
        	Map<String, String> dataRow = new HashMap<>();
        	
        	System.out.println("\n" + translatKey);

        	dataRow.put(TRANSLATION_KEY, translatKey);
        	values.forEach((translatFile, translatValue) -> {
       			dataRow.put(translatFile, translatValue);
        		System.out.println(translatFile + ":" + translatValue);
        	});
        	
        	allData.add(dataRow);
        });
        System.out.println(allData);
        return allData;
    }
	
	public void updateTranslationKey(String oldKey, String newKey) {
		Map<String, String> tr = translations.get(oldKey);
		translations.remove(oldKey);
		translations.put(newKey, tr);
		translationFiles.forEach((file, pair) -> {
			String value = pair.get(oldKey);
			pair.remove(oldKey);
			pair.put(newKey, value);
		});
	}
	
	public void updateTranslation(String key, String newValue, String lang) {
		translations.get(key).put(lang, newValue);
		translationFiles.get(lang).put(key, newValue);
		System.out.println(translations);
		System.out.println(translationFiles);
	}
	
}
