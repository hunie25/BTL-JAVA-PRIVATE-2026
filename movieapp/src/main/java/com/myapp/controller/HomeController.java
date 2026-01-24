package com.myapp.controller;

import com.myapp.model.Movie;
import com.myapp.service.MovieService;
import com.myapp.util.ImageCache;
import com.myapp.util.SceneNavigator;
import com.myapp.util.UiUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

public class HomeController {
    @FXML private FlowPane movieContainer;
    @FXML private Button btnPageInfo; // Nút hiển thị trang (Màu tím)
    @FXML private VBox heroSection; // Phần chữ to
    @FXML private ScrollPane scrollPane;

    private int currentPage = 1;
    private final MovieService movieService = new MovieService();

    @FXML
    public void initialize() {
        loadMovies(currentPage);
        setupScrollEffect();
    }

    // Hiệu ứng: Cuộn xuống thì chữ mờ dần
    private void setupScrollEffect() {
        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {

            double opacity = 1.0 - (newVal.doubleValue() * 3.5);
            if (opacity < 0) opacity = 0;
            heroSection.setOpacity(opacity);
        });
    }

    @FXML
    void resetHome() {
        currentPage = 1;
        loadMovies(1);

        scrollPane.setVvalue(0);
    }

    private void loadMovies(int page) {
        btnPageInfo.setText("ĐANG TẢI...");
        UiUtils.runAsync(() -> movieService.getNewMovies(page), movies -> {
            movieContainer.getChildren().clear();
            for (Movie m : movies) {
                movieContainer.getChildren().add(createMovieCard(m));
            }
            btnPageInfo.setText("TRANG " + page);

            scrollPane.setVvalue(0);
        });
    }



    private StackPane createMovieCard(Movie movie) {
        // Cấu hình kích thước chuẩn
        double CARD_WIDTH = 155;
        double IMG_HEIGHT = 215;
        double INFO_HEIGHT = 85;
        double TOTAL_HEIGHT = IMG_HEIGHT + INFO_HEIGHT;

        // 1. WRAPPER (Khung chính)
        StackPane wrapper = new StackPane();
        wrapper.getStyleClass().add("movie-card");
        wrapper.setPrefSize(CARD_WIDTH, TOTAL_HEIGHT);
        wrapper.setMinSize(CARD_WIDTH, TOTAL_HEIGHT);

        // 2. CONTENT (Lớp nội dung: Ảnh + Hộp xám dính liền)
        VBox content = new VBox(0); // Spacing 0 để dính chặt nhau
        content.setPrefSize(CARD_WIDTH, TOTAL_HEIGHT);

        // A. Ảnh Poster (Trên)
        ImageView poster = new ImageView(ImageCache.get(movie.getFullThumbUrl(), CARD_WIDTH, IMG_HEIGHT));
        poster.setFitWidth(CARD_WIDTH);
        poster.setFitHeight(IMG_HEIGHT);
        poster.setPreserveRatio(true);

        // B. Hộp thông tin (Dưới)
        VBox infoBox = new VBox(8);
        infoBox.getStyleClass().add("card-info-box");

        infoBox.setPrefSize(CARD_WIDTH, INFO_HEIGHT);
        infoBox.setMinSize(CARD_WIDTH, INFO_HEIGHT);

        Label title = new Label(movie.getName());
        title.getStyleClass().add("movie-title");
        title.setWrapText(true);
        title.setMaxWidth(CARD_WIDTH - 20);

        Label year = new Label(String.valueOf(movie.getYear()));
        year.getStyleClass().add("movie-meta");
        HBox metaWrapper = new HBox(year);

        infoBox.getChildren().addAll(title, metaWrapper);
        content.getChildren().addAll(poster, infoBox);

        // --- CẮT GÓC (CLIPPING) ---

        Rectangle clip = new Rectangle(CARD_WIDTH, TOTAL_HEIGHT);
        clip.setArcWidth(12); // Độ bo góc
        clip.setArcHeight(12);
        content.setClip(clip);

        // 3. BORDER OVERLAY (Lớp viền phủ trên cùng)
        Region borderOverlay = new Region();
        borderOverlay.getStyleClass().add("card-border-overlay");
        borderOverlay.setPrefSize(CARD_WIDTH, TOTAL_HEIGHT);
        borderOverlay.setMaxSize(CARD_WIDTH, TOTAL_HEIGHT);

        // 4. GHÉP: Content nằm dưới, BorderOverlay nằm trên
        wrapper.getChildren().addAll(content, borderOverlay);

        wrapper.setOnMouseClicked(e -> SceneNavigator.loadWatchScene(movie));

        return wrapper;
    }

    @FXML void nextPage() { loadMovies(++currentPage); }
    @FXML void prevPage() { if (currentPage > 1) loadMovies(--currentPage); }
}