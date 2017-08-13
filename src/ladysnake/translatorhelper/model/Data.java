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
import ladysnake.translatorhelper.controller.ControllerFx;

public class Data {

	public static final String TRANSLATION_KEY = "translation key";
	
	private static final Pattern TRANSLATION_PATTERN = Pattern.compile("(.*?)=(.*)");

	private Map<String, Boolean> editedFiles;
	private ObservableList<Map<String, String>> translationList;

	public Data() {
		editedFiles = new HashMap<>();
		translationList = FXCollections.observableArrayList();
		new TranslateAPI();
	}
	
	public void load(File[] langFiles) {
		if(langFiles == null || langFiles.length == 0)
			return;
		
		Map<String, Map<String, String>> translations = new TreeMap<>();
		
		for(File file : langFiles) {
			editedFiles.put(file.getName(), false);
			try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
				while(reader.ready()) {
					String line = reader.readLine();
					Matcher m = TRANSLATION_PATTERN.matcher(line);
					if(m.matches()) {
						String key = m.group(1);
						Map<String, String> values = translations.get(key);
						if(values == null) {
							values = new HashMap<>();
							translations.put(m.group(1), values);
						}
						values.put(file.getName(), m.group(2));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		this.translationList = generateDataInMap(translations);
	}
	
	public void save(File folder) {
		editedFiles.forEach((file, edited) -> {
			if(edited) {
				try (BufferedWriter writer = Files.newBufferedWriter(new File(folder, file).toPath())) {
					for(Map<String, String> pairs : translationList) {
						if(pairs.get(file) != null) {
							writer.write(pairs.get(TRANSLATION_KEY) + "=" + pairs.get(file));
							writer.newLine();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				editedFiles.put(file, false);
			}
		});
	}
	
	public boolean isUnsaved() {
		return editedFiles.keySet().stream().anyMatch(editedFiles::get);
	}
	
	private ObservableList<Map<String, String>> generateDataInMap(Map<String, Map<String, String>> translations) {
        ObservableList<Map<String, String>> allData = FXCollections.observableArrayList();
        translations.forEach((translatKey, values) -> {
        	Map<String, String> dataRow = new HashMap<>();

        	dataRow.put(TRANSLATION_KEY, translatKey);
        	values.forEach((translatFile, translatValue) -> {
       			dataRow.put(translatFile, translatValue);
        	});
        	
        	allData.add(dataRow);
        });
        return allData;
    }
	
	public ObservableList<Map<String, String>> getTranslationList() {
		return this.translationList;
	}
	
	public void updateTranslationKey(String oldKey, String newKey, int selectedRow) {
		translationList.get(selectedRow).put(TRANSLATION_KEY, newKey);
		translationList.get(selectedRow).keySet().stream().filter(s -> !s.equals(TRANSLATION_KEY)).forEach(l -> editedFiles.put(l, true));
	}
	
	public void updateTranslation(int selectedRow, String newValue, String lang) {
		System.out.println(selectedRow + " " + newValue + " " + lang);
		editedFiles.put(lang, true);
		translationList.get(selectedRow).put(lang, newValue);
	}
	
	public void removeTranslation(int selectedRow) {
		translationList.remove(selectedRow).keySet().stream().filter(s -> !s.equals(TRANSLATION_KEY)).forEach((lang) -> editedFiles.put(lang, true));
	}
	
	public void addTranslation(String key) {
		Map<String, String> newMap = new HashMap<>();
		newMap.put(Data.TRANSLATION_KEY, key);
		translationList.add(newMap);
		translationList.sort((o1, o2) -> o1.get(TRANSLATION_KEY).compareTo(o2.get(TRANSLATION_KEY)));
	}
	
	public void generateTranslation(String lang, int selectedRow) {
		Matcher m1 = ControllerFx.LANG_PATTERN.matcher(lang);
		if(!m1.matches())
			return;
		String badTransl = TranslateAPI.translate(translationList.get(selectedRow).get("en_us.lang"), m1.group(1));
		updateTranslation(selectedRow, badTransl, lang);
	}
	
}
