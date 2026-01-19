package com.myapp.controller;

import com.myapp.model.Movie;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MovieCardCell extends ListCell<Movie> {
    private final HBox root = new HBox(12);
    private final ImageView poster = new ImageView();
    private final VBox infoBox = new VBox(6);
    private final Label lbTitle = new Label();
    private final Label lbYear = new Label();

    public MovieCardCell() {
        poster.setFitWidth(90);
        poster.setFitHeight(130);
        poster.setPreserveRatio(true);

        lbTitle.getStyleClass().add("movie-title");
        lbYear.getStyleClass().add("movie-year");

        infoBox.getChildren().addAll(lbTitle, lbYear);
        root.getChildren().addAll(poster, infoBox);

        root.setPadding(new Insets(12));
        root.getStyleClass().add("movie-card");
    }

    @Override
    protected void updateItem(Movie movie, boolean empty) {
        super.updateItem(movie, empty);

        if (empty || movie == null) {
            setGraphic(null);
            return;
        }

        lbTitle.setText(movie.getName());
        lbYear.setText(movie.getYear() > 0 ? "Năm " + movie.getYear() : "");

        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isBlank()) {
            try {
                poster.setImage(new Image(movie.getPosterUrl(), true));
            } catch (Exception e) {
                poster.setImage(null);
            }
        } else {
            poster.setImage(null);
        }

        setGraphic(root);
    }
}
