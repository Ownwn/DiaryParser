package com.example.diaryparser;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Gatherers;

public record GraphViewer(Pattern pattern) {
    private static final int WIDTH = 700;
    private static final int SCALE = 5;
    private static final double PADDING = 40;

    public GraphViewer {

        Pane pane = new Pane();
        draw(pane, pattern);
        addAveragePlotButton(pane);

        Scene scene = new Scene(pane, WIDTH, 240);
        Stage stage = new Stage();
        stage.setScene(scene);


        stage.setTitle("Graph");
        stage.setScene(scene);
        stage.setWidth(WIDTH);
        stage.setHeight(500);
        stage.show();
    }

    private double getYScale(double max, double min, double value) {
        double range = max - min;
        return 400 - (max * (value - min) / range) * SCALE;
    }

    private void addAveragePlotButton(Pane pane) {
        Button button = new Button("Show average trend line");

        pane.getChildren().add(button);
    }

    

    private void draw(Pane root, Pattern pattern) {
        List<GraphPoint> matches = Application.allNotes.stream()
                .<GraphPoint>mapMulti((note, c) -> {
                    Matcher matcher = pattern.matcher(note.totalContent());
                    String date = note.date().substring(0, note.date().length() - 3);

                    if (matcher.find() && matcher.groupCount() >= 1) {
                        try {
                            double value = Double.parseDouble(matcher.group(1));
                            c.accept(new GraphPoint(value, date, matcher.group(0), true));
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Error parsing regex number!");

                        }

                    } else {
                        c.accept(new GraphPoint(null, date, "Missing!", false));
                    }
                })
                .toList();
        int numRelevant = matches.size();
        if (numRelevant == 0) {
            Text emptyText = new Text("No files matched!");
            emptyText.setX(200);
            emptyText.setY(100);
            root.getChildren().add(emptyText);
            return;
        }

        double min = matches.stream().filter(GraphPoint::valid).mapToDouble(GraphPoint::value).min().getAsDouble();
        double max = matches.stream().filter(GraphPoint::valid).mapToDouble(GraphPoint::value).max().getAsDouble();

        double noteWidth = (WIDTH - PADDING) / numRelevant;

        // funny gatherer
        AtomicInteger i = new AtomicInteger();
        matches.stream()
                .gather(Gatherers.windowSliding(2))
                .forEach(list -> {
                    double x = (PADDING/2d) + i.get() * noteWidth;

                    if (list.get(1).valid()) {
                        double y = getYScale(max, min, list.get(1).value());
                        Circle point = new Circle(x + noteWidth, y - noteWidth / 2d, 2);

                        if (list.get(0).valid()) {
                            double previousY = getYScale(max, min, list.get(0).value());
                            Line line = new Line(x, previousY, x + noteWidth, y);
                            line.setStroke(y - previousY > 0 ? Color.RED : Color.BLUE);
                            root.getChildren().add(line);
                        }

                        root.getChildren().addAll(point);
                    }


                    i.getAndIncrement();
                });

        Text text = new Text(100, 100, "");
        Line visualiserLine = new Line(-1, -1, -1, -1);

        root.setOnMouseMoved(e -> {
            int numIndex = (int) ((e.getX() - (PADDING/2d) + 4) / noteWidth);
            if (numIndex < numRelevant && numIndex >= 0) {
                GraphPoint point = matches.get(numIndex);

                double lineX = (PADDING/2d) + numIndex * noteWidth;

                visualiserLine.setVisible(true);
                visualiserLine.setStartX(lineX);
                visualiserLine.setEndX(lineX);

                visualiserLine.setStartY(0);
                visualiserLine.setEndY(root.getHeight() - 30);

                text.setText(point.allContent() + "\n" + point.date());
                text.setX(lineX + 5);
                text.setY(root.getHeight() - 30);
            } else {
                text.setText("");
                visualiserLine.setVisible(false);
            }
        });


        root.getChildren().addAll(text, visualiserLine);

    }

    record GraphPoint(Double value, String date, String allContent, boolean valid) implements Comparable<GraphPoint> {
        @Override
        public int compareTo(GraphPoint o) {
            return value().compareTo(o.value());
        }
    }
}
