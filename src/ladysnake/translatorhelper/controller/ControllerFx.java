package ladysnake.translatorhelper.controller;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.SortEvent;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.WindowEvent;
import ladysnake.translatorhelper.application.TranslationHelper;
import ladysnake.translatorhelper.model.Data;

public class ControllerFx {
	
	private TranslationHelper view;
	private Data data;
	private DirectoryChooser fileChooser;
	private File langFolder;
	
	public static final Pattern LANG_PATTERN = Pattern.compile("^(\\w*?)_(.*?)\\.lang$");
	
	public ControllerFx(TranslationHelper view) {
		super();
		this.view = view;
		this.data = new Data();
		fileChooser = new DirectoryChooser();
		fileChooser.setInitialDirectory(new File("."));
	}

	public void onChooseFolder(ActionEvent event) {
		fileChooser.setTitle("Open Resource File");
		langFolder = fileChooser.showDialog(view.getStage());
        if (langFolder != null) {
            File[] langFiles = langFolder.listFiles(f -> f.isFile() && LANG_PATTERN.matcher(f.getName()).matches());
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
	
	public void onExit(WindowEvent event) {
		if(data.isUnsaved()) {
			Alert confirm = new Alert(AlertType.CONFIRMATION);
			confirm.setHeaderText("You have some unsaved changes !");
			confirm.setContentText("Press OK to ignore");
			confirm.showAndWait().filter(b -> b == ButtonType.CANCEL).ifPresent(b -> event.consume());
		}
	}
	
	public void onKeyPressed(KeyEvent event) {
		if(KeyCodeCombination.keyCombination("Ctrl+S").match(event))
			data.save(langFolder);
	}
	
	public void onJoker(ActionEvent event) {
		try {
			data.generateTranslation(view.getTable().getSelectionModel().getSelectedCells().get(0).getTableColumn().getText(), 
					view.getTable().getSelectionModel().getSelectedIndex());
			view.getTable().refresh();
		} catch (NullPointerException | IndexOutOfBoundsException e) {
			Alert d = new Alert(AlertType.INFORMATION);
			d.setTitle("No cell selected");
			d.setHeaderText("Please select a cell to autocomplete");
			d.showAndWait();
		}
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
	
	public void onSort(SortEvent<TableView<Map<String, String>>> event) {
		data.setUnsaved();
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
