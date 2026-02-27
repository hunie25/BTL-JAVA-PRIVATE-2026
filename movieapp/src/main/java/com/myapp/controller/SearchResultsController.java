package com.myapp.controller;

import com.myapp.model.Movie;
import com.myapp.service.MovieService;
import com.myapp.util.SceneNavigator;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.List;

public class SearchResultsController {
    @FXML private FlowPane resultContainer;
    @FXML private TextField txtSearchInner;
    @FXML private Label lblStatus;

    private final MovieService movieService = new MovieService();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(500));

    public void searchMovies(String keyword) {
        txtSearchInner.setText(keyword);
        lblStatus.setText("Kết quả cho: \"" + keyword + "\"");
        executeSearch(keyword);
    }

    @FXML
    private void handleSearchKeyReleased() {
        debounce.setOnFinished(e -> {
            String keyword = txtSearchInner.getText().trim();
            if (keyword.length() >= 2) {
                lblStatus.setText("Đang tìm: \"" + keyword + "\"");
                executeSearch(keyword);
            }
        });
        debounce.playFromStart();
    }

    private void executeSearch(String keyword) {
        new Thread(() -> {
            try {
                List<Movie> movies = movieService.getOphimClient().searchMovies(keyword, 1);

                Platform.runLater(() -> {
                    resultContainer.getChildren().clear();
                    if (movies == null || movies.isEmpty()) {
                        lblStatus.setText("Không tìm thấy phim nào cho \"" + keyword + "\"");
                    } else {
                        lblStatus.setText("Tìm thấy " + movies.size() + " kết quả cho \"" + keyword + "\"");
                        for (Movie m : movies) {
                            resultContainer.getChildren().add(createMovieCard(m));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private StackPane createMovieCard(Movie movie) {
        StackPane card = new StackPane();
        card.getStyleClass().add("movie-card");
        card.setPrefSize(110, 165);

        ImageView poster = new ImageView();
        poster.setFitWidth(110);
        poster.setFitHeight(165);
        poster.setPreserveRatio(false);

        String imgUrl = movie.getFullThumbUrl();
        if (imgUrl == null || imgUrl.contains("-poster.jpg")) {
            imgUrl = movie.getFullPosterUrl().replace("-poster.jpg", "-thumb.jpg");
        }

        try {
            Image img = new Image(imgUrl, true);
            poster.setImage(img);
        } catch (Exception e) {
            // Nếu lỗi thì dùng ảnh mặc định
        }

        Rectangle clip = new Rectangle(110, 165);
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        poster.setClip(clip);

        Region gradient = new Region();
        gradient.setStyle("-fx-background-color: linear-gradient(to top, rgba(0,0,0,0.8), transparent);");
        gradient.setMaxHeight(60);
        StackPane.setAlignment(gradient, Pos.BOTTOM_CENTER);

        Label lbName = new Label(movie.getName());
        lbName.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5;");
        lbName.setWrapText(true);
        lbName.setMaxWidth(100);
        StackPane.setAlignment(lbName, Pos.BOTTOM_LEFT);

        card.getChildren().addAll(poster, gradient, lbName);

        card.setOnMouseClicked(e -> SceneNavigator.loadWatchScene(movie));

        return card;
    }

    @FXML private void goBack() { SceneNavigator.loadHome(); }
}