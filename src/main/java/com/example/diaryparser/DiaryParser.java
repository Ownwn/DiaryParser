package com.example.diaryparser;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DiaryParser {
    private static final Pattern fileDatePattern = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})\\.md");
    private static final Comparator<File> fileOrder = (f1, f2) -> {
        Matcher m1 = fileDatePattern.matcher(f1.getName());
        Matcher m2 = fileDatePattern.matcher(f2.getName());

        if (!m1.find()) {
            return Integer.MIN_VALUE/2;
        } else if (!m2.find()) {
            return Integer.MAX_VALUE/2;
        }

        for (int i = 1; i < 4; i++) {
            int compare = Integer.parseInt(m1.group(i)) - Integer.parseInt(m2.group(i));
            if (compare != 0) return compare;

        }
        return 0;

    };

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
            List<File> files = new ArrayList<>(getChildrenRecursively(directory).filter(f -> f.getName().endsWith(".md")).toList());
            files.sort(fileOrder);
            return files;
        } else {
            File[] filesArray = directory.listFiles((dir, name) -> name.endsWith(".md"));
            assert filesArray != null;
            List<File> files = new ArrayList<>(List.of(filesArray));
            files.sort(fileOrder);
            return files;
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
