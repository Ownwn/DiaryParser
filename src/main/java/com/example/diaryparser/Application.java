package com.example.diaryparser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Application extends javafx.application.Application {

    public static TableView<Note> notesTable;
    public static List<Note> allNotes = new ArrayList<>();
    public static Set<String> colNames = new HashSet<>();

    public static void main(String[] args) {
        launch();
    }



    @Override
    public void start(Stage stage) {
        notesTable = new TableView<>();


        ObservableList<String> fileNames = FXCollections.observableArrayList();
        ListView<String> items = new ListView<>(fileNames);


        Button loadFiles = Interface.getLoadFilesButton(stage);
        Button loadFilesRecursive = Interface.getRecursiveLoadFilesButton(stage);
        TextField searchBox = Interface.getSearchBox();


        VBox root = new VBox(10);
        root.getChildren().addAll(loadFiles, loadFilesRecursive, notesTable, searchBox);

        Scene scene = new Scene(root, 600, 240);

        stage.setTitle("Diary Parser");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.show();
    }
}