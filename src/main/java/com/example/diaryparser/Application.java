package com.example.diaryparser;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class Application extends javafx.application.Application {

    public static void main(String[] args) {
        launch();
    }



    @Override
    public void start(Stage stage) {


        Button loadFiles = new Button("Load files");
        loadFiles.setOnAction(e -> {
            List<File> files = DiaryParser.loadFiles(stage);
            DiaryParser.listFiles(files);
        });

        VBox root = new VBox(10);
        root.getChildren().add(loadFiles);

        Scene scene = new Scene(root, 600, 240);

        stage.setTitle("Diary Parser");
        stage.setScene(scene);
        stage.show();
    }




}