package com.example.diaryparser;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Gatherers;

public record GraphViewer(Pattern pattern) {
    private static final int WIDTH = 700;
    private static final int SCALE = 5;

    public GraphViewer {

        Pane pane = new Pane();
        draw(pane, pattern);

        Scene scene = new Scene(pane, WIDTH, 240);
        Stage stage = new Stage();
        stage.setScene(scene);


        stage.setTitle("Graph");
        stage.setScene(scene);
        stage.setWidth(WIDTH);
        stage.setHeight(500);
        stage.show();
    }

    private final double getYScale(double max, double min, double value) {
        double range = max - min;
        return 400 - (max * (value - min) / range) * SCALE;
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

        int noteWidth = WIDTH / numRelevant;

        // funny gatherer
        AtomicInteger i = new AtomicInteger();
        matches.stream()
                .gather(Gatherers.windowSliding(2))
                .forEach(list -> {
                    double x = i.get() * noteWidth;


                    double y = getYScale(max, min, list.get(1));
                    double previousY = getYScale(max, min, list.get(0));

                    Circle point = new Circle(x + noteWidth, y - noteWidth/2d, 2);

                    Line line = new Line(x, previousY, x + noteWidth, y);
                    line.setStroke(y - previousY > 0 ? Color.RED : Color.BLUE);
                    root.getChildren().addAll(line, point);

                    i.getAndIncrement();
                });

        Text text = new Text(100, 100, "");

        root.setOnMouseMoved(e -> {
            int numIndex = (int) (e.getX() / noteWidth);
            if (numIndex < numRelevant) {
                double value = matches.get(numIndex);
                text.setText(String.valueOf(value));
                text.setX(e.getX());
                text.setY(getYScale(max, min, value));
            } else {
                text.setText("");
            }
        });


        root.getChildren().add(text);

    }
}
