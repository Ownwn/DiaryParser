package com.example.diaryparser;

import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public record GraphViewer(Pattern pattern) {

    public GraphViewer {
        Line l = new Line(0, 0, 400, 400);
        HBox graphBox = new HBox();
        draw(graphBox, pattern);
        graphBox.getChildren().add(l);
        Scene scene = new Scene(graphBox, 600, 240);
        Stage stage = new Stage();
        stage.setScene(scene);


        stage.setTitle("Graph");
        stage.setScene(scene);
        stage.setWidth(700);
        stage.setHeight(500);
        stage.show();
    }

    private void draw(HBox root, Pattern pattern) {
        Stream<Note> relevantNotes = Application.allNotes.stream()
                .filter(n -> pattern.matcher(n.totalContent()).find());
        root.getChildren().add(new Text(String.valueOf(relevantNotes.count())));

    }



}
