package com.example.diaryparser;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record GraphViewer(Pattern pattern) {
    private static final int WIDTH = 700;
    private static final int SCALE = 3;

    public GraphViewer {

        Pane pane = new Pane();
        draw(pane, pattern);

        Scene scene = new Scene(pane, 600, 240);
        Stage stage = new Stage();
        stage.setScene(scene);


        stage.setTitle("Graph");
        stage.setScene(scene);
        stage.setWidth(WIDTH);
        stage.setHeight(500);
        stage.show();
    }

    private void draw(Pane root, Pattern pattern) {
        List<Double> matches = Application.allNotes.stream()
                .<Double>mapMulti((n, c) -> {
                    Matcher matcher = pattern.matcher(n.totalContent());

                    if (matcher.find() && matcher.groupCount() >= 1) {
                        try {
                            double value = Double.parseDouble(matcher.group(1));
                            c.accept(value);
                        } catch (NumberFormatException e) {
                            // todo

                        }

                    }
                })
                .toList();
        int numRelevant = matches.size();
        if (numRelevant == 0) {
            return;
        }

        double min = Collections.min(matches);
        double max = Collections.max(matches);
        double range = max - min;

        int noteWidth = WIDTH / numRelevant;

        double previous = min;
        for (int i = 0; i < numRelevant; i++) {
            double x = i * noteWidth;
            double y = max * (matches.get(i) - min) / range;


            Line line = new Line(x, 300 - previous * SCALE, x + noteWidth, 300 - y * SCALE);
            line.setStroke(y - previous > 0 ? Color.RED : Color.BLUE);
            root.getChildren().add(line);

            previous = y;
        }


        root.getChildren().add(new Text(String.valueOf(matches.size())));

    }
}
