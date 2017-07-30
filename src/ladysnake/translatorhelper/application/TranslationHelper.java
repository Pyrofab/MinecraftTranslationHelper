package ladysnake.translatorhelper.application;
	
import java.util.Map;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import ladysnake.translatorhelper.controller.ControllerFx;
import ladysnake.translatorhelper.model.Data;


public class TranslationHelper extends Application {
	
	private ControllerFx control;
	private Stage stage;
	private BorderPane borderPane;
	private ContextMenu contextMenu;
	private TableView<Map<String, String>> trTable;

	@Override
	public void start(Stage primaryStage) {
		try {
			this.stage = primaryStage;
			
			borderPane = new BorderPane();
			
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(10, 10, 10, 10));
			borderPane.setTop(grid);

			control = new ControllerFx(this);

			Scene scene = new Scene(borderPane,900,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	        
			{	// load button
		        Button loadbtn = new Button();
		        loadbtn.setText("Load a lang folder");
		        loadbtn.setOnAction(control::onChooseFolder);
		        grid.add(loadbtn, 0, 0, 2, 1);
	        }
			
			{	// save button
	        	Button savebtn = new Button();
	        	savebtn.setText("Save");
	        	savebtn.setOnAction(control::onSave);
	        	grid.add(savebtn, 2, 0);
	        }

			{	// context menu (right click)
				contextMenu = new ContextMenu();
				MenuItem item1 = new MenuItem("Delete row");
				item1.setOnAction(control::onDeleteRow);
				MenuItem item2 = new MenuItem("New translation key");
				item2.setOnAction(control::onInsertRow);
				contextMenu.getItems().addAll(item1, item2);
			}
			
			primaryStage.setScene(scene);
			primaryStage.setTitle("Translation O Matik");
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generateTable(ObservableList<Map<String, String>> allTranslations, String[] langNames) {
		trTable = new TableView<Map<String, String>>(allTranslations);
		trTable.setEditable(true);
		trTable.setContextMenu(contextMenu);
        
        Callback<TableColumn<Map<String, String>, String>, TableCell<Map<String, String>, String>>
        cellFactoryForMap = p -> new TextFieldTableCell<Map<String, String>, String>(new StringConverter<String>() {
            @Override
            public String toString(String t) {
            	if(t == null)
            		return "";
                return t.toString();
            }
            @Override
            public String fromString(String string) {
                return string;
            }                                    
        });
		 
        trTable.getColumns().clear();

        for(int i = 0; i < langNames.length; i++) {
            TableColumn<Map<String, String>, String> langColumn = new TableColumn<>(langNames[i]);
            langColumn.setPrefWidth(300);
            langColumn.setCellValueFactory(new MapValueFactory(langNames[i]));
            langColumn.setCellFactory(cellFactoryForMap);
            if(i == 0)
            	langColumn.setOnEditCommit(control::onEditCommitKey);
            else
            	langColumn.setOnEditCommit(control::onEditCommit);
            trTable.getColumns().add(langColumn);
        }
        
        System.out.println(allTranslations);
        
        borderPane.setCenter(trTable);
	}
	
	public TableView<Map<String, String>> getTable() {
		return this.trTable;
	}
	
	public Stage getStage() {
		return this.stage;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
