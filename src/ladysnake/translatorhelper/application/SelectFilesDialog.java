package ladysnake.translatorhelper.application;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

public class SelectFilesDialog extends Dialog<Map<File, Boolean>> {
	
	private Map<File, Boolean> locked;
	
	private static Tooltip openTip = new Tooltip("Whether this file will be opened in the editor");
	private static Tooltip lockTip = new Tooltip("If checked, any changes made to this file will not be saved");
	
	public SelectFilesDialog(List<File> files) {
		ObservableList<ExtendedFile> list = FXCollections
				.observableArrayList(files.stream()
						.map(f -> new ExtendedFile(f, true, false))
						.collect(Collectors.toList()));
		TableColumn<ExtendedFile, String> col1 = new TableColumn<>("Open");
		col1.setCellFactory(c -> new CheckBoxTableCell<ExtendedFile, String>(i -> list.get(i).isToOpen()));
		col1.setEditable(true);
		TableColumn<ExtendedFile, String> col2 = new TableColumn<>("Lock");
		col2.setCellFactory(c -> new CheckBoxTableCell<ExtendedFile, String>(i -> list.get(i).isToLock()));
		col2.setEditable(true);
		TableColumn<ExtendedFile, String> col3 = new TableColumn<>("File");
		col3.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().file.getName()));
		TableView<ExtendedFile> table = new TableView<>();
		table.setEditable(true);
		table.setSelectionModel(null);
		table.getColumns().addAll(col1, col2, col3);
		table.setItems(list);
		this.getDialogPane().setContent(table);
		this.setTitle("File Selector");
		this.setHeaderText("Choose which files you wish to load or lock");
		this.getDialogPane().getButtonTypes().add(ButtonType.OK);
		this.setResultConverter(type -> {
			if(type == ButtonType.OK) {
				Map<File, Boolean> lockedMap = new HashMap<>();
				list.stream().peek(System.out::println).filter(f -> f.isToOpen().getValue()).forEach(f -> lockedMap.put(f.getFile(), f.isToLock().getValue()));
				return lockedMap;
			}
			return null;
		});
	}
	
	private static class ExtendedFile {
		private File file;
		private SimpleBooleanProperty toOpen, toLock;
		
		protected ExtendedFile(File file, boolean toOpen, boolean toLock) {
			super();
			this.file = file;
			this.toOpen = new SimpleBooleanProperty(toOpen);
			this.toLock = new SimpleBooleanProperty(toLock);
		}
		protected File getFile() {
			return file;
		}
		protected SimpleBooleanProperty isToOpen() {
			return toOpen;
		}
		protected SimpleBooleanProperty isToLock() {
			return toLock;
		}

		@Override
		public String toString() {
			return "ExtendedFile [file=" + file + ", toOpen=" + toOpen + ", toLock=" + toLock + "]";
		}
		
	}
	
}
