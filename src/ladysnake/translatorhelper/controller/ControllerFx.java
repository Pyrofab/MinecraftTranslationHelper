package ladysnake.translatorhelper.controller;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.input.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.WindowEvent;
import ladysnake.translatorhelper.application.FindReplaceDialog;
import ladysnake.translatorhelper.application.SelectFilesDialog;
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
		fileChooser.setTitle("Open Resource File");
		fileChooser.setInitialDirectory(new File("."));
	}

	/**
	 * Handles the "load a folder" button
	 */
	public void onChooseFolder(ActionEvent event) {
		langFolder = fileChooser.showDialog(view.getStage());
        if (langFolder != null) {
    		view.setStatus("loading lang files");
    		try {
	            Map<File, Boolean> lockedFiles = new SelectFilesDialog(
	            		Arrays.asList(langFolder.listFiles(f -> f.isFile() && LANG_PATTERN.matcher(f.getName()).matches()))).showAndWait().get();
	            List<String> langNames = new ArrayList<>(lockedFiles.keySet().stream()
	            		.map(File::getName)
	            		.sorted((s1, s2) -> s1.equalsIgnoreCase(Data.EN_US) ? -1 : s2.equalsIgnoreCase(Data.EN_US) ? 1 : s1.compareTo(s2))
	            		.collect(Collectors.toList()));
	            langNames.add(0, Data.TRANSLATION_KEY);
	            view.generateTable(data.load(lockedFiles), langNames, data::isLocked);
	            fileChooser.setInitialDirectory(langFolder.getParentFile());
	            view.setStatus("idle");
    		} catch (NoSuchElementException e) {
    			System.err.println("Operation cancelled : " + e.getLocalizedMessage());
    			view.setStatus("no lang folder selected");
    		}
        }
    }

	/**
	 * Handles the "Save" button
	 */
	public void onSave(ActionEvent event) {
		data.save(langFolder);
	}

	/**
	 * Handles the program exiting
	 */
	public void onExit(WindowEvent event) {
		if(data.isUnsaved()) {
			Alert confirm = new Alert(AlertType.CONFIRMATION);
			confirm.setHeaderText("You have some unsaved changes !");
			confirm.setContentText("Press OK to ignore");
			confirm.showAndWait().filter(b -> b == ButtonType.CANCEL).ifPresent(b -> event.consume());
		}
	}

	/**
	 * Handles key combinations
	 */
	public void onKeyPressed(KeyEvent event) {
		if(KeyCodeCombination.keyCombination("Ctrl+S").match(event))
			data.save(langFolder);
		else if(KeyCodeCombination.keyCombination("Ctrl+C").match(event)) {
			TablePosition tablePosition = view.getTable().getFocusModel().getFocusedCell();
			ClipboardContent content = new ClipboardContent();
			String contentString = view.getTable().getItems().get(tablePosition.getRow()).get(tablePosition.getTableColumn().getText());
			System.out.println("copying " + contentString);
			content.putString(contentString);
			content.putHtml("<td>" + contentString + "</td>");
			Clipboard.getSystemClipboard().setContent(content);
		} else if(KeyCodeCombination.keyCombination("Ctrl+V").match(event)) {
			List<TablePosition> tablePositions = view.getTable().getSelectionModel().getSelectedCells();
			for(TablePosition tablePosition : tablePositions)
				view.getTable().getItems().get(tablePosition.getRow()).put(tablePosition.getTableColumn().getText(), Clipboard.getSystemClipboard().getString());
			view.getTable().refresh();
		} else if(KeyCodeCombination.keyCombination("Ctrl+R").match(event)) {
			Dialog<FindReplaceDialog.FindReplaceParameters> findReplaceDialog;
			findReplaceDialog =	new FindReplaceDialog(view.getTable().getColumns().stream()
					.map(TableColumnBase::getText)
					.skip(1).collect(Collectors.toList()));
			findReplaceDialog.showAndWait().ifPresent(params ->
					data.searchReplace(params.fromLang, params.toLang, params.regex, params.replace));
			view.getTable().refresh();
		} else if(KeyCodeCombination.keyCombination("Ctrl+Z").match(event)) {
			data.undo();
			view.getTable().refresh();
		} else if(KeyCodeCombination.keyCombination("Ctrl+Shift+Z").match(event) || KeyCodeCombination.keyCombination("Ctrl+Y").match(event)) {
			data.redo();
			view.getTable().refresh();
		} else if(event.getCode().equals(KeyCode.DELETE)) {
			for(TablePosition tablePosition : view.getTable().getSelectionModel().getSelectedCells()) {
				data.removeTranslation(tablePosition.getRow(), tablePosition.getTableColumn().getText());
				view.getTable().refresh();
			}
		}
	}

	/**
	 * Handles the auto-completion button
	 */
	public void onJoker(ActionEvent event) {
		try {
			view.setStatus("fetching translation");
			TranslationHelper.THREADPOOL.submit(new Task<String>() {

				@Override
				protected String call() throws IOException {
					return data.generateTranslation(
							view.getTable().getSelectionModel().getSelectedCells().get(0).getTableColumn().getText(),
							view.getTable().getSelectionModel().getSelectedIndex());
				}

				@Override
				protected void succeeded() {
			        super.succeeded();
			        try {
			        	data.updateTranslation(
			        			view.getTable().getSelectionModel().getSelectedIndex(),
			        			this.get(),
			        			view.getTable().getSelectionModel().getSelectedCells().get(0).getTableColumn().getText());
						view.getTable().refresh();
						view.setStatus("idle");
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
						failed();
					}
			    }

				@Override
				protected void failed() {
				    super.failed();
					Alert d = new Alert(AlertType.ERROR);
					d.setHeaderText("Failed to retrieve answer. Maybe you are offline ?");
					d.setContentText(this.getException().toString());
					this.getException().printStackTrace();
					d.showAndWait();
				    view.setStatus("failed to retrieve translation");
				}

			});
		} catch (NullPointerException | IndexOutOfBoundsException e) {
			e.printStackTrace();
			Alert d = new Alert(AlertType.INFORMATION);
			d.setTitle("No cell selected");
			d.setHeaderText("Please select a cell to autocomplete");
			d.showAndWait();
		}
	}

	/**
	 * Handles changes made through the table
	 */
	public void onEditCommit(CellEditEvent<Map<String, String>, String> event) {
		data.updateTranslation(event.getTablePosition().getRow(), event.getNewValue(), event.getTableColumn().getText());
	}

	/**
	 * Handles context menu's row deletion
	 */
	public void onDeleteRow(ActionEvent event) {
		data.removeTranslation(view.getTable().getSelectionModel().getSelectedIndex());
	}

	/**
	 * Handles context menu's key editing
	 */
	public void onEditRowKey(ActionEvent event) {
		Dialog<String> d = new TextInputDialog();
		d.setGraphic(null);
		d.setHeaderText("Enter the new translation key:");
		d.setTitle("New translation key");
		d.showAndWait().ifPresent(s -> data.updateTranslationKey(s, view.getTable().getSelectionModel().getSelectedIndex()));
		view.getTable().refresh();
		view.getTable().sort();
	}

	public void onSort(SortEvent<TableView<Map<String, String>>> event) {
		data.setUnsaved();
	}

	/**
	 * Handles row insertion from "Add..." button
	 */
	public void onInsertRow(ActionEvent event) {
		Dialog<String> d = new TextInputDialog();
		d.setGraphic(null);
		d.setHeaderText("Enter the new translation's key:");
		d.setTitle("New translation");
		d.showAndWait().ifPresent(data::addTranslation);
		view.getTable().sort();
	}

	public void onNewFile(ActionEvent event) {
		Dialog<String> d = new TextInputDialog();
		d.setGraphic(null);
		d.setHeaderText("Enter the file's language code:");
		d.setTitle("New language");
		d.showAndWait().ifPresent(langName -> view.addColumn(langName + ".lang", false));
	}
}
