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

    private static TableView<String> notesTable;

    public static void main(String[] args) {
        launch();
    }



    @Override
    public void start(Stage stage) {
        notesTable = new TableView<>();



        ObservableList<String> fileNames = FXCollections.observableArrayList();
        ListView<String> items = new ListView<>(fileNames);

        Set<String> colNames = new HashSet<>();

        Button loadFiles = getLoadFilesButton(stage, fileNames, colNames);



        VBox root = new VBox(10);
        root.getChildren().addAll(loadFiles, items, notesTable);

        Scene scene = new Scene(root, 600, 240);

        stage.setTitle("Diary Parser");
        stage.setScene(scene);
        stage.show();
    }

    private static Button getLoadFilesButton(Stage stage, ObservableList<String> fileNames, Set<String> colNames) {
        Button loadFiles = new Button("Load files");
        loadFiles.setOnAction(_ -> {
            List<File> files = DiaryParser.loadFiles(stage);

            fileNames.clear();

            var allNotes = DiaryParser.getNotes(files)
                    .<String>mapMulti((n, c) -> {
                        colNames.addAll(n.entries().keySet());
                        n.entries().values().stream().map(e -> String.join("", e)).forEach(c);
                    })
                    .toList();

            notesTable.setItems(FXCollections.observableList(allNotes));


            colNames.forEach(name -> {
                TableColumn<String, String> col = new TableColumn<>(name);
                col.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
                notesTable.getColumns().add(col);
            });
        });
        return loadFiles;
    }
}