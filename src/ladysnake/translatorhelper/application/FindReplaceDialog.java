package ladysnake.translatorhelper.application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.regex.Pattern;

public class FindReplaceDialog extends Dialog<FindReplaceDialog.FindReplaceParameters> {
    private ComboBox<String> fromLang, toLang;
    private TextField regex, replace;

    public FindReplaceDialog(List<String> languages) {
        this(FXCollections.observableList(languages), "", "", "", "");
    }

    public FindReplaceDialog(ObservableList<String> languages, String fromLang, String toLang, String regex, String replace) {
        this.fromLang = new ComboBox<>(languages);
        this.fromLang.getSelectionModel().select(fromLang);
        this.toLang = new ComboBox<>(languages);
        this.toLang.getSelectionModel().select(toLang);
        this.regex = new TextField(regex);
        this.replace = new TextField(replace);
        GridPane grid = new GridPane();
        grid.add(this.fromLang, 1, 1);
        grid.add(this.toLang, 3, 1);
        grid.add(this.regex, 1, 2, 2, 1);
        grid.add(this.replace, 3, 2, 2, 1);
        this.getDialogPane().setContent(grid);
        this.setTitle("Search and replace");
        this.setHeaderText("Search for a string from a language and convert it to something else");
        this.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        this.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        this.setResultConverter(type -> type == ButtonType.APPLY ? new FindReplaceParameters() : null);
    }

    public class FindReplaceParameters {
        public final String fromLang, toLang, replace;
        public final Pattern regex;

        FindReplaceParameters() {
            this.fromLang = FindReplaceDialog.this.fromLang.getValue();
            this.toLang = FindReplaceDialog.this.toLang.getValue();
            this.regex = Pattern.compile(FindReplaceDialog.this.regex.getText());
            this.replace = FindReplaceDialog.this.replace.getText();
        }
    }
}
