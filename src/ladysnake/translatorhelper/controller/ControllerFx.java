package ladysnake.translatorhelper.controller;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.stage.DirectoryChooser;
import ladysnake.translatorhelper.application.TranslationHelper;
import ladysnake.translatorhelper.model.Data;

public class ControllerFx {
	
	private TranslationHelper view;
	private Data data;
	private DirectoryChooser fileChooser;
	private File langFolder;
	private Pattern langFilesPattern;
	
	public ControllerFx(TranslationHelper view) {
		super();
		this.view = view;
		this.data = new Data();
		fileChooser = new DirectoryChooser();
		fileChooser.setInitialDirectory(new File("."));
		langFilesPattern = Pattern.compile(".*?\\.lang$");
	}

	public void onChooseFolder(ActionEvent event) {
		fileChooser.setTitle("Open Resource File");
		langFolder = fileChooser.showDialog(view.getStage());
        if (langFolder != null) {
            File[] langFiles = langFolder.listFiles(f -> f.isFile() && langFilesPattern.matcher(f.getName()).matches());
            data.load(langFiles);
            String[] langNames = new String[langFiles.length + 1];
            langNames[0] = Data.TRANSLATION_KEY;
            for (int i = 0; i < langFiles.length; i++) {
				langNames[i+1] = langFiles[i].getName();
			}
            view.generateTable(data.generateDataInMap(), langNames);
        }
    }
	
	public void onSave(ActionEvent event) {
		data.save(langFolder);
	}
	
	public void onEditCommitKey(CellEditEvent<Map<String, String>, String> event) {
		System.out.println("old translation key:" + event.getNewValue());
		data.updateTranslationKey(event.getOldValue(), event.getNewValue());
	}

	public void onEditCommit(CellEditEvent<Map<String, String>, String> event) {
		String translationKey = event.getRowValue().get(Data.TRANSLATION_KEY);
		System.out.println("translation key:" + translationKey);
		data.updateTranslation(translationKey, event.getNewValue(), event.getTableColumn().getText());
	}
}
