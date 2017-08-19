package ladysnake.translatorhelper.application;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class SelectFilesDialog extends Dialog<Map<File, Boolean>> {
	
	private Map<File, Boolean> locked;
	
	private static Tooltip openTip = new Tooltip("Whether this file will be opened in the editor");
	private static Tooltip lockTip = new Tooltip("If checked, any changes made to this file will not be saved");
	
	public SelectFilesDialog(List<File> files) {
		locked = new HashMap<>();
		files.forEach(f -> locked.put(f, false));
		ListView<File> list = new ListView(FXCollections.observableList(files));
		list.setCellFactory(v -> new FileListCell());
		GridPane pane = new GridPane();
		Border border = new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null));
		Background background = new Background(new BackgroundFill(Color.WHITE, null, null));
		{
			Label l = new Label("Open ");
			l.setBorder(border);
			l.setBackground(background);
			Tooltip.install(l, openTip);
			pane.add(l, 0, 0);
		}
		{
			Label l = new Label("Lock");
			l.setBorder(border);
			l.setBackground(background);
			Tooltip.install(l, lockTip);
			pane.add(l, 1, 0);
		}
		pane.add(list, 0, 1, 3, 1);
		this.getDialogPane().setContent(pane);
		this.getDialogPane().getButtonTypes().add(ButtonType.OK);
		this.setResultConverter(type -> locked);
	}
	
	private class FileListCell extends ListCell<File> {
		private GridPane grid;
		private Label fileName;
		private CheckBox open;
		private CheckBox lock;
		
		public FileListCell() {
			grid = new GridPane();
			fileName = new Label();
			open = new CheckBox();
			Tooltip.install(open, openTip);
			lock = new CheckBox();
			Tooltip.install(lock, lockTip);
			grid.add(open, 0, 0);
			grid.add(new Label("   "), 1, 0);	//TODO this is Sin incarnate
			grid.add(lock, 2, 0);
			grid.add(new Label("  "), 3, 0);
			grid.add(fileName, 4, 0);
		}
		
		@Override
		protected void updateItem(File item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null) {
		         setText(null);
		         setGraphic(null);
		     } else {
		         fileName.setText(item.getName());
		         open.setSelected(true);
		         open.setOnAction(event -> {
		        	 if(open.isSelected())
		        		 locked.put(item, lock.isSelected());
		        	 else
		        		 locked.remove(item);
		         });
		         lock.setSelected(false);
		         lock.setOnAction(event -> {
		        	 open.setSelected(true);
		        	 locked.put(item, lock.isSelected());
		         });
		         setGraphic(grid);
		     }
		}
	}

}
