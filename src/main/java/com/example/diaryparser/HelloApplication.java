package com.example.diaryparser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HelloApplication extends Application {

    public static void main(String[] args) {
        launch();
    }

    public static void listFiles(List<File> files) {
        List<Note> notes = files.stream().map(HelloApplication::parseFile).toList();
    }

    public static List<File> loadFiles(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose notes directory");
        File directory = directoryChooser.showDialog(stage);
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory");
        }
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".md"));
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("Loading files failed");
        }

        return List.of(files);
    }

    public static Note parseFile(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse file", e);
        }

        if (lines.isEmpty()) {
            System.err.println("Warning: Empty file " + file.getName());
        }

        List<String> content = new ArrayList<>();
        Map<String, List<String>> entries = new LinkedHashMap<>();
        String headingName = null;

        for (String line : lines) {
            boolean isHeading = line.startsWith("## ");
            if (isHeading) {
                if (headingName != null) {
                    entries.put(headingName, content);
                }

                headingName = line.substring(3);


                content = new ArrayList<>();

                continue;
            }
            if (!line.isBlank()) {
                content.add(line);
            }

        }
        entries.put(headingName, content);

        return new Note(entries);
    }

    @Override
    public void start(Stage stage) {


        Button loadFiles = new Button("Load files");
        loadFiles.setOnAction(e -> {
            listFiles(loadFiles(stage));
        });

        VBox root = new VBox(10);
        root.getChildren().add(loadFiles);

        Scene scene = new Scene(root, 600, 240);

        stage.setTitle("Diary Parser");
        stage.setScene(scene);
        stage.show();
    }

    // map of headings, and all lines of text under heading
    record Note(Map<String, List<String>> entries) {
    }


}