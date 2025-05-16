package com.example.diaryparser;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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


        HBox loadButtons = new HBox(6);
        Button loadFiles = Interface.getLoadFilesButton(stage);
        Button loadFilesRecursive = Interface.getRecursiveLoadFilesButton(stage);
        loadButtons.getChildren().addAll(loadFiles, loadFilesRecursive);

        VBox tableBox = new VBox(10);
        tableBox.getChildren().addAll(notesTable);

        VBox dataButtons = new VBox(5);
        dataButtons.setMaxWidth(Region.USE_PREF_SIZE);
        dataButtons.getChildren().addAll(Interface.getSearchBox(), Interface.getMergeBox());

        VBox root = new VBox();

        root.getChildren().addAll(loadButtons, tableBox, dataButtons, Interface.graphTools());

        Scene scene = new Scene(root, 600, 240);

        stage.setTitle("Diary Parser");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.show();
    }
}