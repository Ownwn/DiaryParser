package com.example.diaryparser;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Application extends javafx.application.Application {

    private static TableView<Note> notesTable;
    private static List<Note> allNotes = new ArrayList<>();
    private static Set<String> colNames = new HashSet<>();

    public static void main(String[] args) {
        launch();
    }

    private static TextField getSearchBox() {
        TextField searchBox = new TextField();
        searchBox.setPromptText("Regex filter...");
        searchBox.setPrefHeight(150);


        searchBox.setOnKeyTyped(_ -> {
            String text = searchBox.getText();
            if (text == null || text.isBlank()) {
                notesTable.setItems(FXCollections.observableArrayList(allNotes));
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

            List<Note> filteredNotes = allNotes.stream()
                    .filter(note -> colNames.stream()
                            .map(name -> String.join("\n", note.entries().getOrDefault(name, List.of())))
                            .anyMatch(content -> p.matcher(content.toLowerCase()).find()))
                    .toList();


            notesTable.setItems(FXCollections.observableList(filteredNotes));

            notesTable.getColumns().forEach(column -> {
                // There must be a better way than cast trickery right?
                TableColumn<Note, String> typedColumn = (TableColumn<Note, String>) column;
                typedColumn.setCellValueFactory(cellData -> {
                    Note note = cellData.getValue();
                    String content = String.join("\n", note.entries().getOrDefault(typedColumn.getText(), List.of()));

                    return new SimpleStringProperty(content);
                });
            });

            notesTable.refresh();
        });

        return searchBox;
    }

    private static Button getLoadFilesButton(Stage stage) {
        Button loadFiles = new Button("Load files");

        colNames = new HashSet<>();

        loadFiles.setOnAction(_ -> {
            List<File> files = DiaryParser.loadFiles(stage);
            notesTable.getColumns().clear();

            allNotes = DiaryParser.getNotes(files).<Note>mapMulti((note, c) -> {
                colNames.addAll(note.entries().keySet());
                c.accept(note);
            }).toList();


            colNames.forEach(columnName -> {
                TableColumn<Note, String> col = new TableColumn<>(columnName);

                col.setCellValueFactory(cellData -> {
                    Note note = cellData.getValue();
                    return new SimpleStringProperty(String.join("\n", note.entries().getOrDefault(columnName, List.of())));
                });

                col.setMaxWidth(300);

                notesTable.getColumns().add(col);
            });
            notesTable.setItems(FXCollections.observableArrayList(allNotes));

        });
        return loadFiles;
    }

    @Override
    public void start(Stage stage) {
        notesTable = new TableView<>();


        ObservableList<String> fileNames = FXCollections.observableArrayList();
        ListView<String> items = new ListView<>(fileNames);


        Button loadFiles = getLoadFilesButton(stage);
        TextField searchBox = getSearchBox();


        VBox root = new VBox(10);
        root.getChildren().addAll(loadFiles, items, notesTable, searchBox);

        Scene scene = new Scene(root, 600, 240);

        stage.setTitle("Diary Parser");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.show();
    }
}