package ladysnake.translatorhelper.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TextInputDialog;
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
            view.generateTable(data.getTranslationList(), langNames);
        }
    }
	
	public void onSave(ActionEvent event) {
		data.save(langFolder);
	}
	
	public void onEditCommitKey(CellEditEvent<Map<String, String>, String> event) {
		data.updateTranslationKey(event.getOldValue(), event.getNewValue(), event.getTablePosition().getRow());
	}

	public void onEditCommit(CellEditEvent<Map<String, String>, String> event) {
		data.updateTranslation(event.getTablePosition().getRow(), event.getNewValue(), event.getTableColumn().getText());
	}
	
	public void onDeleteRow(ActionEvent event) {
		data.removeTranslation(view.getTable().getSelectionModel().getSelectedIndex());
	}
	
	public void onInsertRow(ActionEvent event) {
		Dialog<String> d = new TextInputDialog();
		d.setGraphic(null);
		d.setHeaderText("Enter the new translation key:");
		d.setTitle("New translation");
		d.showAndWait().ifPresent(data::addTranslation);
		view.getTable().sort();
	}
}
