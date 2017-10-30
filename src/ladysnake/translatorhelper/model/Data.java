package ladysnake.translatorhelper.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ladysnake.translatorhelper.controller.ControllerFx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Data {

	public static final String TRANSLATION_KEY = "Translation key";

	private static final Pattern TRANSLATION_PATTERN = Pattern.compile("(.*?)=(.*)");
	public static final String EN_US = "en_us.lang";

	private Map<String, Boolean> editedFiles;
	private Map<String, Boolean> lockedFiles;
	private ObservableList<Map<String, String>> translationList;
	private Deque<Runnable> undoOperations = new LinkedList<>();
	private Deque<Runnable> redoOperations = new LinkedList<>();
	private boolean isUndoing;

	public Data() {
		editedFiles = new HashMap<>();
		translationList = FXCollections.observableArrayList();
	}

	public ObservableList<Map<String, String>> load(Map<File, Boolean> langFiles) {
		this.lockedFiles = new HashMap<>();
		Map<String, Map<String, String>> translations = new TreeMap<>();

		for(File file : langFiles.keySet()) {
			this.lockedFiles.put(file.getName(), langFiles.get(file));
			editedFiles.put(file.getName(), false);
			try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
				while(reader.ready()) {
					String line = reader.readLine();
					Matcher m = TRANSLATION_PATTERN.matcher(line);
					if(m.matches()) {
						String key = m.group(1);
						Map<String, String> values = translations.get(key);
						if(values == null)
							translations.put(m.group(1), values = new HashMap<>());
						values.put(file.getName(), m.group(2));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.translationList = generateDataInMap(translations);

		return this.translationList;
	}

	public void save(File folder) {
		editedFiles.forEach((file, edited) -> {
			if(edited && !isLocked(file)) {
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
		return editedFiles.keySet().stream()
				.peek(f -> System.out.println(f + (editedFiles.get(f) ? " has" : " doesn't have") + " unsaved modifications"))
				.filter(f -> !isLocked(f))
				.anyMatch(editedFiles::get);
	}

	public void setUnsaved() {
		editedFiles.replaceAll((s, b) -> !isLocked(s));
	}

	public boolean isLocked(String lang) {
		return Objects.equals(lang, TRANSLATION_KEY) ? true : lockedFiles.getOrDefault(lang, false);
	}

	private ObservableList<Map<String, String>> generateDataInMap(Map<String, Map<String, String>> translations) {
        ObservableList<Map<String, String>> allData = FXCollections.observableArrayList();
        translations.forEach((translatKey, values) -> {
        	Map<String, String> dataRow = new HashMap<>();

        	dataRow.put(TRANSLATION_KEY, translatKey);
        	values.forEach(dataRow::put);

        	allData.add(dataRow);
        });
        return allData;
    }

	public void updateTranslationKey(String newKey, int selectedRow) {
		String old = translationList.get(selectedRow).put(TRANSLATION_KEY, newKey);
		addUndoOperation(() -> updateTranslationKey(old, selectedRow));
		translationList.get(selectedRow).keySet().stream().filter(s -> !isLocked(s)).forEach(l -> editedFiles.put(l, true));
	}

	public void updateTranslation(final int selectedRow, String newValue, final String lang) {
		System.out.println(selectedRow + " " + newValue + " " + lang);
		if(isLocked(lang)) return;
		final String oldValue = translationList.get(selectedRow).put(lang, newValue);
		addUndoOperation(() -> updateTranslation(selectedRow, oldValue == null ? "" : oldValue, lang));
		editedFiles.put(lang, true);
	}

	private void removeTranslation(String key) {
		for(int i = 0; i < translationList.size(); i++)
			if(translationList.get(i).get(TRANSLATION_KEY).equals(key)) {
				removeTranslation(i);
				break;
			}
	}

	public void removeTranslation(int selectedRow) {
		Map<String, String> translations = translationList.remove(selectedRow);
		translations.keySet().stream().filter(s -> !s.equals(TRANSLATION_KEY)).forEach((lang) -> editedFiles.put(lang, true));
		addUndoOperation(() -> addTranslation(translations.get(TRANSLATION_KEY), translations));
	}

	public void removeTranslation(int selectedRow, String lang) {
		if(isLocked(lang)) return;
		String old = translationList.get(selectedRow).remove(lang);
		addUndoOperation(() -> updateTranslation(selectedRow, old, lang));
		editedFiles.put(lang, true);
	}

	public void addTranslation(String key) {
		Map<String, String> newMap = new HashMap<>();
		newMap.put(Data.TRANSLATION_KEY, key);
		addTranslation(key, newMap);
	}

	private void addTranslation(String key, Map<String, String> newMap) {
		translationList.add(newMap);
		translationList.sort(Comparator.comparing(o -> o.get(TRANSLATION_KEY)));
		addUndoOperation(() -> removeTranslation(key));
	}

	public String generateTranslation(String lang, int selectedRow) throws IOException {
		Matcher m1 = ControllerFx.LANG_PATTERN.matcher(lang);
		if(!m1.matches())
			throw new IllegalArgumentException("The provided String must be a proper lang file");

		return TranslateAPI.translate(translationList.get(selectedRow).containsKey(EN_US)
				? translationList.get(selectedRow).get(EN_US)
				: translationList.get(selectedRow).getOrDefault("en_US.lang", ""), m1.group(1));
	}

	public void searchReplace(String fromLang, String toLang, Pattern regex, String replacePattern, boolean replaceExistingTranslations) {
		for (int i = 0; i < translationList.size(); i++) {
			Map<String, String> translationRow = translationList.get(i);
			if(!translationRow.containsKey(fromLang) || (translationRow.containsKey(toLang) && !replaceExistingTranslations)) continue;
			Matcher matcher = regex.matcher(translationRow.get(fromLang));
			if (matcher.matches()) {
				String replace = replacePattern;
				for (int j = 0; j <= matcher.groupCount(); j++)
					replace = replace.replace("$" + j, matcher.group(j));
				this.updateTranslation(i, replace, toLang);
			}
		}
	}
	
	private void addUndoOperation(Runnable operation) {
		if(isUndoing)
			redoOperations.push(operation);
		else
			undoOperations.push(operation);
	}

	public void undo() {
		if(this.undoOperations.isEmpty()) return;
		try {
			isUndoing = true;
			this.undoOperations.pop().run();
		} finally {
			isUndoing = false;
		}
	}

	public void redo() {
		if(!this.redoOperations.isEmpty())
			this.redoOperations.pop().run();
	}

}
