package ladysnake.translatorhelper.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.input.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.WindowEvent;
import ladysnake.translatorhelper.application.FindReplaceDialog;
import ladysnake.translatorhelper.application.SelectFilesDialog;
import ladysnake.translatorhelper.application.TranslationHelper;
import ladysnake.translatorhelper.model.Data;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
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
        	if(view.isSmartSearchEnabled()) {
        		view.setStatus("Starting smart search from selected folder");
        		Instant i1 = Instant.now();
        		System.out.println(langFolder = findLangFolder2(langFolder));
        		System.out.println(Duration.between(i1, Instant.now()));
			}
    		view.setStatus("loading lang files");
    		try {
    			File[] langFiles = langFolder.listFiles(f -> f.isFile() && LANG_PATTERN.matcher(f.getName()).matches());
    			if(langFiles == null)
    				throw new IOException();
	            Map<File, Boolean> lockedFiles = new SelectFilesDialog(
	            		Arrays.asList(langFiles)).showAndWait().orElseThrow(NoSuchElementException::new);
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
    		} catch (IOException e) {
				System.err.println("The file selected isn't a valid folder");
				view.setStatus("erred while reading the folder");
			}
		}
    }

    private File findLangFolder(File rootFolder) {
		File[] subFiles = rootFolder.listFiles();
		if(subFiles == null || subFiles.length == 0)
			return rootFolder;
		return Arrays.stream(subFiles).parallel().filter(File::isDirectory).map(this::findLangFolder).max((f1, f2) -> {
			File[] subFiles1 = f1.listFiles();
			File[] subFiles2 = f2.listFiles();
			if(subFiles1 == null) return subFiles2 == null ? 0 : -1;
			if(subFiles2 == null) return 1;
			int score1 = 0;
			int score2 = 0;
			long langFiles1 = Arrays.stream(subFiles1).filter(f -> f.isFile() && LANG_PATTERN.matcher(f.getName()).matches()).count();
			long langFiles2 = Arrays.stream(subFiles2).filter(f -> f.isFile() && LANG_PATTERN.matcher(f.getName()).matches()).count();
			if(langFiles1 > langFiles2)		// a lang folder has lang files in it
				score1++;
			else if(langFiles2 > langFiles1)
				score2++;
			if("lang".equals(f1.getName()))		// a lang folder is called "lang"
				score1++;
			if("lang".equals(f2.getName()))
				score2++;
			if(Arrays.stream(subFiles1).noneMatch(File::isDirectory))	// a lang folder doesn't have any subfolder
				score1++;
			if(Arrays.stream(subFiles2).noneMatch(File::isDirectory))
				score2++;
//			System.out.println(f1 + ": " + score1 + "\n" + f2 + ": " + score2);
			return Integer.compare(score1, score2);
		}).orElse(rootFolder);
	}

	private File findLangFolder2(File rootFolder) {
		File[] subFiles = rootFolder.listFiles();
		if(subFiles == null || subFiles.length == 0)
			return rootFolder;
		return Arrays.stream(subFiles).parallel().filter(File::isDirectory).map(this::findLangFolder).max((f1, f2) -> {
			File[] langFiles1 = f1.listFiles(f -> f.isFile() && LANG_PATTERN.matcher(f.getName()).matches());
			File[] langFiles2 = f2.listFiles(f -> f.isFile() && LANG_PATTERN.matcher(f.getName()).matches());
			if(langFiles1 == null) return langFiles2 == null ? 0 : -1;
			if(langFiles2 == null) return 1;
			if(langFiles1.length > 0) {
				if(langFiles2.length > 0) {
					if (langFiles1.length != langFiles2.length)
						return Integer.compare(langFiles1.length, langFiles2.length);
					else
						return Long.compare(f1.lastModified(), f2.lastModified());
				}
				return 1;
			} else if(langFiles2.length > 0) return -1;
			if(f1.getName().equals("lang")) return f2.getName().equals("lang") ? 0 : 1;
			if(f2.getName().equals("lang")) return -1;
			File[] subDirs = f1.listFiles(File::isDirectory);
			File[] subDirs1 = f2.listFiles(File::isDirectory);
			return Integer.compare(subDirs1 == null ? 0 : subDirs1.length, subDirs == null ? 0 : subDirs.length);
		}).orElse(rootFolder);
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
		if(view.getTable() == null) return;
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
				data.updateTranslation(tablePosition.getRow(), Clipboard.getSystemClipboard().getString(), tablePosition.getTableColumn().getText());
			view.getTable().refresh();
		} else if(KeyCodeCombination.keyCombination("Ctrl+R").match(event)) {
			FindReplaceDialog findReplaceDialog;
			ObservableList<String> availableLanguages = FXCollections.observableList(view.getTable().getColumns().stream()
					.map(TableColumnBase::getText)
					.filter(s -> !s.equals(Data.TRANSLATION_KEY)).collect(Collectors.toList()));
			TableView.TableViewFocusModel focusModel = view.getTable().getFocusModel();
			findReplaceDialog = new FindReplaceDialog(availableLanguages);
			findReplaceDialog.setRegex((String) ((Map)focusModel.getFocusedItem()).get("en_us.lang"));
			if(focusModel.getFocusedCell().getTableColumn() != null) {
				String selectedLang = focusModel.getFocusedCell().getTableColumn().getText();
				findReplaceDialog.setToLang(selectedLang);
				findReplaceDialog.setReplace((String) ((Map) focusModel.getFocusedItem()).get(selectedLang));
			}
			findReplaceDialog.showAndWait().ifPresent(params ->
					data.searchReplace(params.fromLang, params.toLang, params.regex, params.replace, params.replaceExistingTranslations));
			view.getTable().refresh();
		} else if(KeyCodeCombination.keyCombination("Ctrl+Z").match(event)) {
			data.undo();
			view.getTable().refresh();
		} else if(KeyCodeCombination.keyCombination("Ctrl+Shift+Z").match(event) || KeyCodeCombination.keyCombination("Ctrl+Y").match(event)) {
			data.redo();
			view.getTable().refresh();
		} else if(KeyCodeCombination.keyCombination("Ctrl+N").match(event)) {
			view.showMenuNew();
		} else if(event.getCode().equals(KeyCode.ESCAPE)) {
			view.getTable().requestFocus();
		} else if(event.getCode().equals(KeyCode.DELETE)) {
			for(TablePosition tablePosition : view.getTable().getSelectionModel().getSelectedCells()) {
				data.removeTranslation(tablePosition.getRow(), tablePosition.getTableColumn().getText());
				view.getTable().refresh();
			}
		} else if(event.getCode().isLetterKey()) //noinspection unchecked
			view.getTable().edit(view.getTable().getFocusModel().getFocusedCell().getRow(),
				view.getTable().getFocusModel().getFocusedCell().getTableColumn());
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
	@SuppressWarnings("unchecked")
	public void onEditCommit(CellEditEvent<Map<String, String>, String> event) {
		data.updateTranslation(event.getTablePosition().getRow(), event.getNewValue(), event.getTableColumn().getText());
		TableView.TableViewFocusModel<Map<String, String>> focusModel = view.getTable().getFocusModel();
		//noinspection unchecked
		focusModel.focus(focusModel.getFocusedCell().getRow()+1, focusModel.getFocusedCell().getTableColumn());
		view.getTable().getSelectionModel().clearAndSelect(focusModel.getFocusedCell().getRow(), focusModel.getFocusedCell().getTableColumn());
		view.getTable().requestFocus();
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
		view.getTable().getSelectionModel().clearSelection();
		d.showAndWait().ifPresent(key -> view.getTable().getSelectionModel().select(data.addTranslation(key)));
		view.getTable().sort();
		view.getTable().requestFocus();
	}

	public void onNewFile(ActionEvent event) {
		Dialog<String> d = new TextInputDialog();
		d.setGraphic(null);
		d.setHeaderText("Enter the file's language code:");
		d.setTitle("New language");
		d.showAndWait().ifPresent(langName -> view.addColumn(langName + ".lang", false));
	}
}
