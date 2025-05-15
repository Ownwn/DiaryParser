package com.example.diaryparser;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

public class DiaryParser {

    public static Stream<Note> getNotes(List<File> files) {
        return files.stream().map(DiaryParser::parseFile);
    }

    public static List<File> loadFiles(Stage stage, boolean recursive) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose notes directory");
        File directory = directoryChooser.showDialog(stage);
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory");
        }


        if (recursive) {
           return getChildrenRecursively(directory).filter(f -> f.getName().endsWith(".md")).toList();
        } else {
            File[] filesArray = directory.listFiles((dir, name) -> name.endsWith(".md"));
            assert filesArray != null;
            return List.of(filesArray);
        }
    }

    public static Stream<File> getChildrenRecursively(File file) {
        return Stream.of(file).mapMulti((f, c) -> {
           c.accept(f);
           if (file.isDirectory()) {
               File[] files = file.listFiles();
               if (files != null) {
                   Arrays.stream(files).flatMap(DiaryParser::getChildrenRecursively).forEach(c);
               }
           }
        });
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
        String headingName = "Unknown";

        for (String line : lines) {
            boolean isHeading = line.startsWith("## ");
            if (isHeading) {

                entries.put(headingName, content);


                headingName = line.substring(3);


                content = new ArrayList<>();

                continue;
            }
            if (!line.isBlank()) {
                content.add(line);
            }

        }
        entries.put(headingName, content);

        return new Note(file.getName(), entries);
    }
}
