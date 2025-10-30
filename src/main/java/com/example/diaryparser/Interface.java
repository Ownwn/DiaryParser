package com.example.diaryparser;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class Interface {

    public static TextField getSearchBox() {
        TextField searchBox = new TextField();
        searchBox.setPromptText("Regex filter...");
        searchBox.setPrefHeight(20);
        searchBox.setPrefWidth(160);


        searchBox.setOnKeyTyped(_ -> {
            String text = searchBox.getText();
            if (text == null || text.isBlank()) {
                Application.notesTable.setItems(FXCollections.observableArrayList(Application.allNotes));
                return;
            }
            Optional<Pattern> p = tryCompileText(searchBox);
            if (p.isEmpty()) {
                return;
            }

            filterNotes(s -> p.get().matcher(s.toLowerCase()).find());
        });

        return searchBox;
    }

    private static void filterNotes(Predicate<String> predicate) {
        List<Note> filteredNotes = Application.allNotes.stream()
                .filter(note -> Application.colNames.stream()
                        .map(name -> String.join("\n", note.entries().getOrDefault(name, List.of())))
                        .anyMatch(predicate))
                .toList();


        Application.notesTable.setItems(FXCollections.observableList(filteredNotes));

        Application.notesTable.getColumns().forEach(column -> {
            // There must be a better way than cast trickery right?
            TableColumn<Note, String> typedColumn = (TableColumn<Note, String>) column;
            typedColumn.setCellValueFactory(cellData -> {
                Note note = cellData.getValue();
                String content = String.join("\n", note.entries().getOrDefault(typedColumn.getText(), List.of()));

                return new SimpleStringProperty(content);
            });
        });

        Application.notesTable.refresh();
    }

    private static Optional<Pattern> tryCompileText(TextField box) {
        try {
            Pattern p = Pattern.compile(box.getText().toLowerCase(), Pattern.MULTILINE);
            box.setStyle("-fx-text-fill: black;");
            return Optional.of(p);
        } catch (PatternSyntaxException e) {
            box.setStyle("-fx-text-fill: red;");
            return Optional.empty();
        }
    }

    public static TextField getMergeBox() {
        TextField mergeBox = new TextField();
        mergeBox.setPromptText("Merge");
        mergeBox.setPrefHeight(20);
        mergeBox.setPrefWidth(160);

        mergeBox.setOnAction(_ -> {
            String text = mergeBox.getText();
            if (text == null || text.isEmpty()) {
                return;
            }
            recomputeColumns(text.toLowerCase());

            mergeBox.clear();
        });

        return mergeBox;
    }

    public static TextField getFuzzyFindBox() {
        TextField findBox = new TextField();
        findBox.setPromptText("Fuzzy Find");
        findBox.setPrefHeight(20);
        findBox.setPrefWidth(160);

        findBox.setOnKeyTyped(_ -> {
            String text = findBox.getText();
            if (text == null || text.isBlank()) {
                Application.notesTable.setItems(FXCollections.observableArrayList(Application.allNotes));
                return;
            }
            filterNotes(s -> StringHelper.checkSimilar(text.toLowerCase(), s.toLowerCase()));
        });

        return findBox;
    }

    public static HBox graphTools() {
        HBox box = new HBox(4);
        Button button = new Button("Graph");
        TextField textField = new TextField();
        textField.setPrefWidth(200);
        box.getChildren().addAll(button, textField);
        textField.setPromptText("Regex group to graph (numerical)");

        textField.setOnKeyTyped(_ -> {
            tryCompileText(textField);
        });

        button.setOnAction(_ -> {
            Optional<Pattern> p = tryCompileText(textField);
            if (p.isEmpty()) {
                return;
            }
            new GraphViewer(p.get());
        });

        return box;
    }

    private static void recomputeColumns(String startsWith) {
        Set<String> toMerge = Application.colNames.stream()
                .filter(name -> name.toLowerCase().startsWith(startsWith))
                .collect(Collectors.toSet());

        String newColumnName = "placeholder";
        Application.colNames.removeAll(toMerge);
        Application.colNames.add(newColumnName);

        List<Note> mergedNotes = Application.allNotes.stream()
                .map(note -> {
                    Map<String, List<String>> entries = new LinkedHashMap<>(note.entries());

                    List<String> mergedContent = toMerge.stream()
                            .flatMap(colName -> entries.getOrDefault(colName, List.of()).stream())
                            .toList();

                    entries.put(newColumnName, mergedContent);

                    return new Note(note.date(), entries);
                })
                .toList();

        Application.allNotes = mergedNotes;
        Application.notesTable.getColumns().clear();

        updateNotesTable();

    }

    private static void loadALlNotes(List<File> files) {
        Application.allNotes = DiaryParser.getNotes(files).<Note>mapMulti((note, c) -> {
            Application.colNames.addAll(note.entries().keySet());
            c.accept(note);
        }).toList();
    }

    private static void updateNotesTable() {
        Application.colNames.forEach(columnName -> {
            TableColumn<Note, String> col = new TableColumn<>(columnName);

            col.setCellValueFactory(cellData -> {
                Note note = cellData.getValue();
                return new SimpleStringProperty(String.join("\n", note.entries().getOrDefault(columnName, List.of())));
            });

            col.setMaxWidth(300);

            Application.notesTable.getColumns().add(col);
        });
        Application.notesTable.setItems(FXCollections.observableArrayList(Application.allNotes));
    }

    private static void loadFiles(Stage stage, boolean recursive) {
        List<File> files = DiaryParser.loadFiles(stage, recursive);
        Application.notesTable.getColumns().clear();
        Application.colNames = new HashSet<>();

        loadALlNotes(files);

        updateNotesTable();
    }

    public static Button getRecursiveLoadFilesButton(Stage stage) {
        Button loadFiles = new Button("Load files recursively - all subdirectories");

        loadFiles.setOnAction(_ -> loadFiles(stage, true));

        return loadFiles;
    }

    public static Button getLoadFilesButton(Stage stage) {
        Button loadFiles = new Button("Load files");

        loadFiles.setOnAction(_ -> loadFiles(stage, false));
        return loadFiles;
    }
}
