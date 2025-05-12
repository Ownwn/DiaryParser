package com.example.diaryparser;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Application extends javafx.application.Application {

    private static TableView<Note> notesTable;

    public static void main(String[] args) {
        launch();
    }



    @Override
    public void start(Stage stage) {
        notesTable = new TableView<>();



        ObservableList<String> fileNames = FXCollections.observableArrayList();
        ListView<String> items = new ListView<>(fileNames);



        Button loadFiles = getLoadFilesButton(stage);



        VBox root = new VBox(10);
        root.getChildren().addAll(loadFiles, items, notesTable);

        Scene scene = new Scene(root, 600, 240);

        stage.setTitle("Diary Parser");
        stage.setScene(scene);
        stage.show();
    }

    private static Button getLoadFilesButton(Stage stage) {
        Button loadFiles = new Button("Load files");

        Set<String> colNames = new HashSet<>();

        loadFiles.setOnAction(_ -> {
            List<File> files = DiaryParser.loadFiles(stage);
            notesTable.getColumns().clear();

            var allNotes = DiaryParser.getNotes(files).<Note>mapMulti((note, c) -> {
                colNames.addAll(note.entries().keySet());
                c.accept(note);
            }).toList();


            colNames.forEach(columnName -> {
                TableColumn<Note, String> col = new TableColumn<>(columnName);

                col.setCellValueFactory(cellData -> {
                    Note note = cellData.getValue();
                    return new SimpleStringProperty(String.join("\n", note.entries().getOrDefault(columnName, List.of())));
                });

                notesTable.getColumns().add(col);
            });
            notesTable.setItems(FXCollections.observableArrayList(allNotes));

        });
        return loadFiles;
    }
}