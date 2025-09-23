package com.example.diaryparser;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Gatherers;

public record GraphViewer(Pattern pattern) {
    private static final int WIDTH = 700;
    private static final double PADDING = 40;

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

    private double getYScale(double max, double min, double value) {
        double range = max - min;
        return 440 - ((value - min) / range) * 400;
    }

    public List<GraphPoint> findDrawPoints(Pattern pattern) {
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
        return matches;
    }


    private void draw(Pane root, Pattern pattern) {
        List<GraphPoint> matches = findDrawPoints(pattern);

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

        List<DrawPoint> drawPoints = getDrawPoints(matches, max, min, noteWidth);




        playDataAnimation(drawPoints, root);


        Text text = new Text(100, 100, "");
        Line visualiserLine = new Line(-1, -1, -1, -1);

        root.setOnMouseMoved(e -> {
            int numIndex = (int) ((e.getX() - (PADDING / 2d) + 4) / noteWidth);
            if (numIndex < numRelevant && numIndex >= 0) {
                GraphPoint point = matches.get(numIndex);

                double lineX = (PADDING / 2d) + numIndex * noteWidth;

                visualiserLine.setVisible(true);
                visualiserLine.setStartX(lineX);
                visualiserLine.setEndX(lineX);

                visualiserLine.setStartY(0);
                visualiserLine.setEndY(root.getHeight() - 30);

                text.setText(point.allContent() + "\n" + point.date());
                text.setX(lineX + 5);
                text.setY(root.getHeight() - 30);
                visualiserLine.setOpacity(0.2d);
            } else {
                text.setText("");
                visualiserLine.setVisible(false);
            }
        });


        root.getChildren().addAll(text, visualiserLine);

    }

    public void playDataAnimation(List<DrawPoint> drawPoints, Pane root) {
        Timeline t = getTimeline(drawPoints, root);

        Button skip = new Button("Skip Animation");
        skip.setOnAction(_ -> {
            t.setRate(200d);
            t.play();
        });

        Button pause = new Button("Pause Animation");
        pause.setOnAction(_ -> t.pause());

        Button play = new Button("Play Animation");
        play.setOnAction(_ -> t.play());

        Button replay = new Button("Replay");
        replay.setOnAction(_ -> {

        });


        HBox buttons = new HBox(5);
        buttons.getChildren().addAll(pause, play, skip);
        root.getChildren().add(buttons);

        t.setCycleCount(drawPoints.size());
        t.setRate(10d);
        t.play();
    }

    private static Timeline getTimeline(List<DrawPoint> drawPoints, Pane root) {
        AtomicInteger i = new AtomicInteger(0);

        return new Timeline(
                new KeyFrame(Duration.seconds(1), _ -> {

                    DrawPoint d = drawPoints.get(i.getAndIncrement());

                    root.getChildren().add(d.circle);
                    if (d.line != null) {
                        Line line = d.line;
                        line.setStroke(line.getEndY() - line.getStartY() > 0 ? Color.RED : Color.BLUE);
                        root.getChildren().add(line);
                    }
                })
        );
    }

    public List<DrawPoint> getDrawPoints(List<GraphPoint> matches, double max, double min, double noteWidth) {
        List<DrawPoint> points = new ArrayList<>();
        // funny gatherer
        AtomicInteger i = new AtomicInteger();
        matches.stream()
                .gather(Gatherers.windowSliding(2))
                .forEach(list -> {
                    double x = (PADDING / 2d) + i.get() * noteWidth;

                    if (list.get(1).valid()) {
                        double y = getYScale(max, min, list.get(1).value());
                        Circle circle = new Circle(x + noteWidth, y - noteWidth / 2d, 2);
                        DrawPoint point = new DrawPoint(null, circle);
                        if (list.getFirst().valid()) {

                            double previousY = getYScale(max, min, list.getFirst().value());

                            Line line = new Line(x, previousY, x + noteWidth, y);
                            point = new DrawPoint(line, circle);
                        }
                        points.add(point);
                    }


                    i.getAndIncrement();
                });
        return points;
    }

    record DrawPoint(Line line, Circle circle) {
    }


    record GraphPoint(Double value, String date, String allContent, boolean valid) implements Comparable<GraphPoint> {
        @Override
        public int compareTo(GraphPoint o) {
            return value().compareTo(o.value());
        }
    }
}
