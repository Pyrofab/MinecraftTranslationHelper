package ladysnake.translatorhelper.application;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.regex.Pattern;

public class FindReplaceDialog extends Dialog<FindReplaceDialog.FindReplaceParameters> {
    private ComboBox<String> fromLang, toLang;
    private TextField regex, replace;
    private CheckBox replaceExistingTranslations;

    public FindReplaceDialog(ObservableList<String> languages) {
        this(languages, languages.isEmpty() ? "" : languages.get(0),
                languages.size() > 1 ? languages.get(1) : (languages.isEmpty() ? "" : languages.get(0)),
                "", "");
    }

    private FindReplaceDialog(ObservableList<String> languages, String fromLang, String toLang, String regex, String replace) {
        this.fromLang = new ComboBox<>(languages);
        this.fromLang.getSelectionModel().select(fromLang);
        this.toLang = new ComboBox<>(languages);
        this.toLang.getSelectionModel().select(toLang);
        this.regex = new TextField(regex);
        this.replace = new TextField(replace);
        this.replaceExistingTranslations = new CheckBox("Replace existing translations");
        this.replaceExistingTranslations.setSelected(true);
        GridPane grid = new GridPane();
        grid.add(this.fromLang, 1, 1);
        grid.add(this.toLang, 3, 1);
        grid.add(this.regex, 1, 2, 2, 1);
        grid.add(this.replace, 3, 2, 2, 1);
        grid.add(this.replaceExistingTranslations, 1, 3);
        this.getDialogPane().setContent(grid);
        this.setTitle("Search and replace");
        this.setHeaderText("Search for a string from a language and convert it to something else");
        this.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        this.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        this.setResultConverter(type -> type == ButtonType.APPLY ? new FindReplaceParameters() : null);
    }

    public void setFromLang(String fromLang) {
        this.fromLang.getSelectionModel().select(fromLang);
    }

    public void setToLang(String toLang) {
        this.toLang.getSelectionModel().select(toLang);
    }

    public void setRegex(String regex) {
        this.regex.setText(regex == null ? "" : regex);
    }

    public void setReplace(String replace) {
        this.replace.setText(replace);
    }

    public void setReplaceExistingTranslations(boolean replaceExistingTranslations) {
        this.replaceExistingTranslations.setSelected(replaceExistingTranslations);
    }

    public class FindReplaceParameters {
        public final String fromLang, toLang, replace;
        public final Pattern regex;
        public final boolean replaceExistingTranslations;

        FindReplaceParameters() {
            this.fromLang = FindReplaceDialog.this.fromLang.getValue();
            this.toLang = FindReplaceDialog.this.toLang.getValue();
            this.regex = Pattern.compile(FindReplaceDialog.this.regex.getText());
            this.replace = FindReplaceDialog.this.replace.getText();
            this.replaceExistingTranslations = FindReplaceDialog.this.replaceExistingTranslations.isSelected();
        }
    }
}
