package com.myapp.controller;

import com.myapp.model.Movie;
import com.myapp.service.MovieService;
import com.myapp.util.SceneNavigator;
import com.myapp.util.UiUtils;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeController {

    @FXML private StackPane phoneFrame;
    @FXML private StackPane appContent;

    @FXML private StackPane heroContainer;
    @FXML private StackPane sliderClipPane;
    @FXML private ImageView imgView1, imgView2;
    private ImageView activeImageView, hiddenImageView;

    @FXML private VBox heroInfoContainer; // Chỉ chứa chữ
    @FXML private Label heroTitle, heroOriginName, heroYear;
    @FXML private HBox heroDots; // Thanh chạy (đã tách riêng)

    @FXML private HBox newMoviesContainer, recMoviesContainer;
    @FXML private VBox loadingBox;
    @FXML private ScrollPane mainContentScroll;

    private List<Movie> heroMovies = new ArrayList<>();
    private int currentHeroIndex = 0;
    private Map<Integer, Image> imageCache = new HashMap<>();
    private final MovieService movieService = new MovieService();

    private PauseTransition autoSlideTimer;
    private Timeline barAnimation;
    private ScaleTransition currentZoomAnimation;

    private double startX;
    private boolean isDragging = false;
    private boolean isTransitioning = false;
    private int lastPreviewIndex = -1;

    private static final Interpolator CINEMATIC_EASE = Interpolator.SPLINE(0.2, 0.0, 0.2, 1.0);
    private static final Duration SLIDE_TIME = Duration.seconds(5);
    private static final Duration TRANSITION_SPEED = Duration.millis(1000);

    @FXML
    public void initialize() {
        if (appContent != null) {
            Rectangle screenClip = new Rectangle();
            screenClip.widthProperty().bind(appContent.widthProperty());
            screenClip.heightProperty().bind(appContent.heightProperty());
            screenClip.setArcWidth(35);
            screenClip.setArcHeight(35);
            appContent.setClip(screenClip);
        }

        if (loadingBox != null) loadingBox.setVisible(true);
        if (mainContentScroll != null) mainContentScroll.setVisible(false);

        if (sliderClipPane != null && heroContainer != null) {
            sliderClipPane.prefWidthProperty().bind(heroContainer.widthProperty());

            imgView1.fitWidthProperty().bind(sliderClipPane.widthProperty());
            imgView2.fitWidthProperty().bind(sliderClipPane.widthProperty());

            imgView1.setPreserveRatio(true);
            imgView2.setPreserveRatio(true);

            imgView1.setCache(true); imgView1.setCacheHint(CacheHint.SPEED);
            imgView2.setCache(true); imgView2.setCacheHint(CacheHint.SPEED);

            activeImageView = imgView1;
            hiddenImageView = imgView2;
            imgView1.setOpacity(1); imgView2.setOpacity(0); imgView2.setVisible(false);

            sliderClipPane.setOnMousePressed(this::handleMousePressed);
            sliderClipPane.setOnMouseDragged(this::handleMouseDragged);
            sliderClipPane.setOnMouseReleased(this::handleMouseReleased);
        }

        if (heroYear != null) {
            heroYear.setStyle("-fx-text-fill: linear-gradient(to right, #22d3ee, #818cf8);");
        }

        loadMovies();
    }

// ==========================================
    // TỐI ƯU HIỆU NĂNG: DÙNG TASK (TƯƠNG TỰ SWINGWORKER)
    // ==========================================

    private void loadMovies() {
        // 1. Hiện Loading ngay lập tức để người dùng biết app đang chạy
        if (loadingBox != null) loadingBox.setVisible(true);
        if (mainContentScroll != null) mainContentScroll.setVisible(false);

        // 2. Tạo TASK (Chạy ngầm - Background Thread)
        // Đây là nơi xử lý nặng: Kết nối API, Database...
        Task<List<Movie>> dataLoadingTask = new Task<>() {
            @Override
            protected List<Movie> call() throws Exception {
                // Giả lập độ trễ mạng (nếu cần test)
                // Thread.sleep(1000);

                // Gọi Service lấy dữ liệu (Nặng)
                return movieService.getNewMovies();
            }
        };

        // 3. KHI HOÀN TẤT (Success) -> Cập nhật UI (JavaFX Application Thread)
        dataLoadingTask.setOnSucceeded(event -> {
            List<Movie> movies = dataLoadingTask.getValue();

            if (movies != null && !movies.isEmpty()) {
                // A. Setup Hero Banner
                int bannerCount = Math.min(8, movies.size());
                heroMovies = new ArrayList<>(movies.subList(0, bannerCount));

                // Preload ảnh đầu tiên
                if (!heroMovies.isEmpty()) {
                    preloadImage(0);
                    preloadImage(1);
                }

                // Khởi động Slider
                startCycle(0);

                // B. Setup List Phim Mới
                if (newMoviesContainer != null) {
                    newMoviesContainer.getChildren().clear();
                    int limit = Math.min(10, movies.size());
                    for (int i = 0; i < limit; i++) {
                        newMoviesContainer.getChildren().add(createMovieCard(movies.get(i)));
                    }
                }

                // C. Setup List Gợi Ý (Đảo ngược list để giả lập random)
                if (recMoviesContainer != null) {
                    recMoviesContainer.getChildren().clear();
                    List<Movie> reversed = new ArrayList<>(movies);
                    Collections.reverse(reversed);
                    int limit = Math.min(10, reversed.size());
                    for (int i = 0; i < limit; i++) {
                        recMoviesContainer.getChildren().add(createMovieCard(reversed.get(i)));
                    }
                }

                // D. Ẩn Loading, Hiện nội dung (Fade In mượt mà)
                if (loadingBox != null) loadingBox.setVisible(false);
                if (mainContentScroll != null) {
                    mainContentScroll.setVisible(true);
                    FadeTransition ft = new FadeTransition(Duration.millis(600), mainContentScroll);
                    ft.setFromValue(0);
                    ft.setToValue(1);
                    ft.play();
                }
            } else {
                // Xử lý nếu không có dữ liệu (Ví dụ: Hiện thông báo lỗi)
                if (loadingBox != null) loadingBox.setVisible(false);
                System.out.println("Không tải được dữ liệu phim.");
            }
        });

        // 4. Xử lý lỗi (Nếu mất mạng hoặc API chết)
        dataLoadingTask.setOnFailed(event -> {
            if (loadingBox != null) loadingBox.setVisible(false);
            Throwable error = dataLoadingTask.getException();
            error.printStackTrace(); // In lỗi ra console để debug
            // Có thể hiện một Label báo lỗi lên màn hình ở đây
        });

        // 5. KÍCH HOẠT LUỒNG CHẠY
        new Thread(dataLoadingTask).start();
    }

    private void startCycle(int index) {
        Image img = imageCache.get(index);
        if (img == null) img = new Image(heroMovies.get(index).getFullThumbUrl(), true);

        activeImageView.setImage(img);
        activeImageView.setVisible(true);
        activeImageView.setOpacity(1);
        activeImageView.setTranslateX(0);
        activeImageView.setScaleX(1.0); activeImageView.setScaleY(1.0);

        updateInfo(heroMovies.get(index));

        runZoomAnimation(activeImageView);
        runProgressBarAndTimer(index);
    }

    private void runZoomAnimation(ImageView img) {
        if (currentZoomAnimation != null) currentZoomAnimation.stop();
        currentZoomAnimation = new ScaleTransition(Duration.seconds(7), img);
        currentZoomAnimation.setFromX(1.0); currentZoomAnimation.setFromY(1.0);
        currentZoomAnimation.setToX(1.08);  currentZoomAnimation.setToY(1.08);
        currentZoomAnimation.play();
    }

    private void runProgressBarAndTimer(int index) {
        if (heroMovies.isEmpty()) return;

        setupProgressBars();
        preloadImage((index + 1) % heroMovies.size());

        StackPane container = (StackPane) heroDots.getChildren().get(index);
        Rectangle bar = (Rectangle) container.getChildren().get(1);

        if (barAnimation != null) barAnimation.stop();
        barAnimation = new Timeline(new KeyFrame(SLIDE_TIME, new KeyValue(bar.scaleXProperty(), 1)));
        barAnimation.play();

        if (autoSlideTimer != null) autoSlideTimer.stop();
        autoSlideTimer = new PauseTransition(SLIDE_TIME);
        autoSlideTimer.setOnFinished(e -> autoNextSlide((index + 1) % heroMovies.size()));
        autoSlideTimer.play();
    }

    private void autoNextSlide(int nextIndex) {
        if (isTransitioning || isDragging) return;
        isTransitioning = true;

        Image nextImg = imageCache.get(nextIndex);
        if (nextImg == null) nextImg = new Image(heroMovies.get(nextIndex).getFullThumbUrl(), true);

        hiddenImageView.setImage(nextImg);
        hiddenImageView.setVisible(true);
        hiddenImageView.setOpacity(1);
        hiddenImageView.setTranslateX(sliderClipPane.getWidth());

        hiddenImageView.setScaleX(1.0); hiddenImageView.setScaleY(1.0);
        hiddenImageView.toFront();

        // CHỈ ANIMATE PHẦN CHỮ (heroInfoContainer), KHÔNG ĐỤNG NÚT PLAY HAY DOTS
        TranslateTransition textOutY = new TranslateTransition(Duration.millis(400), heroInfoContainer);
        textOutY.setByY(-40);
        FadeTransition textOutFade = new FadeTransition(Duration.millis(300), heroInfoContainer);
        textOutFade.setToValue(0);

        TranslateTransition imgActiveSlide = new TranslateTransition(TRANSITION_SPEED, activeImageView);
        imgActiveSlide.setToX(-sliderClipPane.getWidth() * 0.3);
        imgActiveSlide.setInterpolator(CINEMATIC_EASE);

        TranslateTransition imgHiddenSlide = new TranslateTransition(TRANSITION_SPEED, hiddenImageView);
        imgHiddenSlide.setToX(0);
        imgHiddenSlide.setInterpolator(CINEMATIC_EASE);

        ParallelTransition master = new ParallelTransition(textOutY, textOutFade, imgActiveSlide, imgHiddenSlide);
        master.setOnFinished(e -> finalizeTransition(nextIndex));
        master.play();
    }

    private void finalizeTransition(int nextIndex) {
        ImageView temp = activeImageView;
        activeImageView = hiddenImageView;
        hiddenImageView = temp;

        currentHeroIndex = nextIndex;
        hiddenImageView.setVisible(false);
        hiddenImageView.setTranslateX(0);
        hiddenImageView.setScaleX(1.0); hiddenImageView.setScaleY(1.0);

        updateInfo(heroMovies.get(nextIndex));

        playTextEntrance();
        runZoomAnimation(activeImageView);

        isTransitioning = false;
        runProgressBarAndTimer(nextIndex);
    }

    private void playTextEntrance() {
        // Hồi sinh Text (Fade In + Bay lên)
        heroInfoContainer.setTranslateY(40);
        heroInfoContainer.setOpacity(0);

        TranslateTransition textInY = new TranslateTransition(Duration.millis(600), heroInfoContainer);
        textInY.setToY(0); textInY.setInterpolator(CINEMATIC_EASE);

        FadeTransition textInFade = new FadeTransition(Duration.millis(600), heroInfoContainer);
        textInFade.setToValue(1);

        new ParallelTransition(textInY, textInFade).play();
    }

    private void handleMousePressed(MouseEvent event) {
        if (isTransitioning) return;
        if (autoSlideTimer != null) autoSlideTimer.stop();
        if (barAnimation != null) barAnimation.pause();
        if (currentZoomAnimation != null) currentZoomAnimation.pause();

        startX = event.getX();
        isDragging = true;
        lastPreviewIndex = -1;

        preloadImage((currentHeroIndex + 1) % heroMovies.size());
        preloadImage((currentHeroIndex - 1 + heroMovies.size()) % heroMovies.size());
    }

    private void handleMouseDragged(MouseEvent event) {
        if (!isDragging || isTransitioning) return;

        double deltaX = event.getX() - startX;
        double width = sliderClipPane.getWidth();

        activeImageView.setTranslateX(deltaX);

        int targetIndex = (deltaX < 0) ? (currentHeroIndex + 1) % heroMovies.size()
                : (currentHeroIndex - 1 + heroMovies.size()) % heroMovies.size();

        double hiddenStart = (deltaX < 0) ? width : -width;

        if (targetIndex != lastPreviewIndex) {
            Image img = imageCache.get(targetIndex);
            if (img != null) {
                hiddenImageView.setImage(img);
                hiddenImageView.setVisible(true);
                hiddenImageView.setOpacity(1);
                hiddenImageView.setScaleX(1.0); hiddenImageView.setScaleY(1.0);
            }
            lastPreviewIndex = targetIndex;
        }
        hiddenImageView.setTranslateX(hiddenStart + deltaX);

        // Chỉ mờ chữ, không mờ nút Play
        heroInfoContainer.setOpacity(1 - Math.abs(deltaX) / (width * 0.5));
    }

    private void handleMouseReleased(MouseEvent event) {
        if (!isDragging || isTransitioning) return;
        isDragging = false;
        double deltaX = event.getX() - startX;

        if (Math.abs(deltaX) > 50) {
            int nextIndex = (deltaX < 0) ? (currentHeroIndex + 1) % heroMovies.size()
                    : (currentHeroIndex - 1 + heroMovies.size()) % heroMovies.size();
            finishSwipe(nextIndex, deltaX < 0);
        } else {
            snapBack();
        }
    }

    private void finishSwipe(int nextIndex, boolean isNext) {
        isTransitioning = true;
        double endXActive = isNext ? -sliderClipPane.getWidth() : sliderClipPane.getWidth();

        TranslateTransition tActive = new TranslateTransition(Duration.millis(300), activeImageView);
        tActive.setToX(endXActive);

        TranslateTransition tHidden = new TranslateTransition(Duration.millis(300), hiddenImageView);
        tHidden.setToX(0);

        ParallelTransition pt = new ParallelTransition(tActive, tHidden);
        pt.setOnFinished(e -> finalizeTransition(nextIndex));
        pt.play();
    }

    private void snapBack() {
        isTransitioning = true;
        TranslateTransition tActive = new TranslateTransition(Duration.millis(300), activeImageView);
        tActive.setToX(0);

        TranslateTransition tHidden = new TranslateTransition(Duration.millis(300), hiddenImageView);
        tHidden.setToX(hiddenImageView.getTranslateX() > 0 ? sliderClipPane.getWidth() : -sliderClipPane.getWidth());

        ParallelTransition pt = new ParallelTransition(tActive, tHidden);
        pt.setOnFinished(e -> {
            hiddenImageView.setVisible(false);
            heroInfoContainer.setOpacity(1);
            isTransitioning = false;

            if (autoSlideTimer != null) autoSlideTimer.play();
            if (barAnimation != null) barAnimation.play();
            if (currentZoomAnimation != null) currentZoomAnimation.play();
        });
        pt.play();
    }

    private void setupProgressBars() {
        if (heroDots == null) return;
        heroDots.getChildren().clear();
        heroDots.setSpacing(6);
        heroDots.setAlignment(Pos.CENTER_LEFT);
        heroDots.setPrefHeight(10);

        for (int i = 0; i < heroMovies.size(); i++) {
            StackPane container = new StackPane();
            container.setAlignment(Pos.CENTER_LEFT);
            container.getStyleClass().add("indicator-container");

            Rectangle track = new Rectangle(35, 4);
            track.setArcWidth(10); track.setArcHeight(10);
            track.getStyleClass().add("indicator-track");

            Rectangle bar = new Rectangle(35, 4);
            bar.setArcWidth(10); bar.setArcHeight(10);
            bar.getStyleClass().add("indicator-bar");
            bar.setScaleX(0);

            container.getChildren().addAll(track, bar);
            heroDots.getChildren().add(container);
        }
    }

    private void preloadImage(int index) {
        if (index < 0 || index >= heroMovies.size()) return;
        if (!imageCache.containsKey(index)) {
            imageCache.put(index, new Image(heroMovies.get(index).getFullThumbUrl(), true));
        }
    }

    private void updateInfo(Movie movie) {
        if (heroTitle != null) heroTitle.setText(movie.getName().toUpperCase());
        if (heroOriginName != null) heroOriginName.setText(movie.getOriginName());
        if (heroYear != null) heroYear.setText(String.valueOf(movie.getYear()));
    }

    @FXML void watchHeroMovie() { if (!heroMovies.isEmpty()) SceneNavigator.loadWatchScene(heroMovies.get(currentHeroIndex)); }

    private StackPane createMovieCard(Movie movie) {
        StackPane card = new StackPane();
        card.getStyleClass().add("movie-card");

        // Kích thước khung thẻ giữ nguyên
        card.setPrefSize(140, 210);

        ImageView poster = new ImageView();
        poster.setFitWidth(140);
        poster.setFitHeight(210);
        poster.setPreserveRatio(true);

        // [FIX ẢNH MỜ] Tải ảnh chất lượng gốc (Full Size)
        // Bỏ tham số 140, 210 đi để lấy ảnh gốc sắc nét nhất
        // true: backgroundLoading (Tải ngầm để không đơ máy)
        try {
            poster.setImage(new Image(movie.getFullThumbUrl(), true));
        } catch (Exception e) {
            // Xử lý lỗi nếu ảnh hỏng (ví dụ hiện ảnh placeholder)
        }

        // Bo góc ảnh
        Rectangle clip = new Rectangle(140, 210);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        poster.setClip(clip);

        // Hiệu ứng Gradient đen ở chân (giúp chữ dễ đọc)
        Region gradient = new Region();
        gradient.setStyle("-fx-background-color: linear-gradient(to top, rgba(0,0,0,0.9), transparent); -fx-background-radius: 0 0 10 10;");
        gradient.setMaxHeight(80);
        StackPane.setAlignment(gradient, Pos.BOTTOM_CENTER);

        // Thông tin phim
        VBox infoBox = new VBox(2);
        infoBox.setStyle("-fx-padding: 8;");
        infoBox.setAlignment(Pos.BOTTOM_LEFT);

        Label lbYear = new Label(String.valueOf(movie.getYear()));
        lbYear.setStyle("-fx-text-fill: #22d3ee; -fx-font-weight: bold; -fx-font-size: 9px;");

        Label lbName = new Label(movie.getName());
        lbName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        lbName.setWrapText(true);
        lbName.setMaxWidth(120);

        infoBox.getChildren().addAll(lbYear, lbName);

        // Badge HD
        Label badge = new Label("HD");
        badge.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: white; -fx-font-size: 8px; -fx-padding: 2 4; -fx-background-radius: 4;");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new javafx.geometry.Insets(8));

        // [QUAN TRỌNG] Tối ưu hiệu năng cho danh sách dài
        // Cache ảnh ở mức chất lượng cao để khi cuộn không bị giật
        poster.setCache(true);
        poster.setCacheHint(CacheHint.QUALITY);

        card.getChildren().addAll(poster, gradient, infoBox, badge);
        card.setOnMouseClicked(e -> SceneNavigator.loadWatchScene(movie));

        return card;
    }

    public void stopTimerLogic() {
        if (autoSlideTimer != null) autoSlideTimer.stop();
        if (barAnimation != null) barAnimation.stop();
        if (currentZoomAnimation != null) currentZoomAnimation.stop();
    }
}