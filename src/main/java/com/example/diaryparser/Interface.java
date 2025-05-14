package com.example.diaryparser;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Interface {

    public static TextField getSearchBox() {
        TextField searchBox = new TextField();
        searchBox.setPromptText("Regex filter...");
        searchBox.setPrefHeight(150);


        searchBox.setOnKeyTyped(_ -> {
            String text = searchBox.getText();
            if (text == null || text.isBlank()) {
                Application.notesTable.setItems(FXCollections.observableArrayList(Application.allNotes));
                return;
            }
            Pattern p;
            try {
                p = Pattern.compile(text.toLowerCase());
                searchBox.setStyle("-fx-text-fill: black;");
            } catch (PatternSyntaxException e) {
                searchBox.setStyle("-fx-text-fill: red;");
                return;
            }

            List<Note> filteredNotes = Application.allNotes.stream()
                    .filter(note -> Application.colNames.stream()
                            .map(name -> String.join("\n", note.entries().getOrDefault(name, List.of())))
                            .anyMatch(content -> p.matcher(content.toLowerCase()).find()))
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
        });

        return searchBox;
    }

    private static void loadFiles(Stage stage, boolean recursive) {
        List<File> files = DiaryParser.loadFiles(stage, recursive);
        Application.notesTable.getColumns().clear();
        Application.colNames = new HashSet<>();


        Application.allNotes = DiaryParser.getNotes(files).<Note>mapMulti((note, c) -> {
            Application.colNames.addAll(note.entries().keySet());
            c.accept(note);
        }).toList();



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
