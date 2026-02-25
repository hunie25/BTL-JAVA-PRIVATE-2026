package com.myapp.controller;

import com.myapp.model.Movie;
import com.myapp.service.MovieService;
import com.myapp.util.SceneNavigator;
import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeController {

    @FXML private StackPane phoneFrame;
    @FXML private StackPane appContent;

    @FXML private StackPane heroContainer;
    @FXML private StackPane sliderClipPane;
    @FXML private ImageView imgView1, imgView2;
    private ImageView activeImageView, hiddenImageView;

    @FXML private VBox heroInfoContainer;
    @FXML private Label heroTitle, heroOriginName, heroYear;
    @FXML private HBox heroDots;

    @FXML private HBox newMoviesContainer;

    // Nếu FXML hiện tại chưa có loadingBox thì để null vẫn chạy được
    @FXML private VBox loadingBox;
    @FXML private ScrollPane mainContentScroll;

    // ===== TYPE CHIPS =====
    @FXML private ScrollPane spType;
    @FXML private HBox hbType;
    @FXML private Label lbSectionTitle;

    private final ToggleGroup typeGroup = new ToggleGroup();
    private double dragStartX;

    private record TypeItem(String title, String listSlug) {}

    // Slug chuẩn theo ophim1 v1
    private final List<TypeItem> typeItems = List.of(
            new TypeItem("Phim Mới", "phim-moi"),
            new TypeItem("Phim Bộ", "phim-bo"),
            new TypeItem("Phim Lẻ", "phim-le"),
            new TypeItem("TV Shows", "tv-shows"),
            new TypeItem("Hoạt Hình", "hoat-hinh"),
            new TypeItem("Vietsub", "phim-vietsub"),
            new TypeItem("Thuyết Minh", "phim-thuyet-minh"),
            new TypeItem("Lồng Tiếng", "phim-long-tien"),
            new TypeItem("Bộ Đang Chiếu", "phim-bo-dang-chieu"),
            new TypeItem("Bộ Hoàn Thành", "phim-bo-hoan-thanh"),
            new TypeItem("Sắp Chiếu", "phim-sap-chieu"),
            new TypeItem("Chiếu Rạp", "phim-chieu-rap"),
            new TypeItem("Subteam", "subteam")
    );

    private int selectedTypeIndex = 0;

    // ===== HERO =====
    private List<Movie> heroMovies = new ArrayList<>();
    private int currentHeroIndex = 0;

    // Cache ảnh theo index slide đang dùng
    private final Map<Integer, Image> imageCache = new HashMap<>();

    // Cache URL hero đã resolve theo slug (ưu tiên backdrop từ API /images)
    private final Map<String, String> heroUrlBySlugCache = new HashMap<>();

    // Đánh dấu index hero đang resolve để tránh gọi lặp
    private final Map<Integer, Boolean> heroResolving = new HashMap<>();

    // Cache poster/card fallback TMDB theo slug
    private final Map<String, String> resolvedPosterUrlCache = new HashMap<>();

    private final MovieService movieService = new MovieService();

    private PauseTransition autoSlideTimer;
    private Timeline barAnimation;
    private ScaleTransition currentZoomAnimation;
    private boolean isTransitioning = false;

    private static final Interpolator CINEMATIC_EASE = Interpolator.SPLINE(0.2, 0.0, 0.2, 1.0);
    private static final Duration SLIDE_TIME = Duration.seconds(5);
    private static final Duration TRANSITION_SPEED = Duration.millis(900);

    @FXML
    public void initialize() {
        // Clip bo góc cho khung app
        if (appContent != null) {
            Rectangle screenClip = new Rectangle();
            screenClip.widthProperty().bind(appContent.widthProperty());
            screenClip.heightProperty().bind(appContent.heightProperty());
            screenClip.setArcWidth(35);
            screenClip.setArcHeight(35);
            appContent.setClip(screenClip);
        }

        // Setup hero slider
        if (sliderClipPane != null && heroContainer != null && imgView1 != null && imgView2 != null) {
            sliderClipPane.prefWidthProperty().bind(heroContainer.widthProperty());
            sliderClipPane.prefHeightProperty().bind(heroContainer.heightProperty());

            // Clip thật sự theo kích thước vùng hero ảnh
            Rectangle heroClip = new Rectangle();
            heroClip.widthProperty().bind(sliderClipPane.widthProperty());
            heroClip.heightProperty().bind(sliderClipPane.heightProperty());
            sliderClipPane.setClip(heroClip);

            // Ảnh fill theo vùng hero
            imgView1.fitWidthProperty().bind(sliderClipPane.widthProperty());
            imgView1.fitHeightProperty().bind(sliderClipPane.heightProperty());
            imgView2.fitWidthProperty().bind(sliderClipPane.widthProperty());
            imgView2.fitHeightProperty().bind(sliderClipPane.heightProperty());

            // Không méo ảnh
            imgView1.setPreserveRatio(true);
            imgView2.setPreserveRatio(true);

            // Render mượt hơn
            imgView1.setSmooth(true);
            imgView2.setSmooth(true);

            imgView1.setCache(true);
            imgView1.setCacheHint(CacheHint.QUALITY);
            imgView2.setCache(true);
            imgView2.setCacheHint(CacheHint.QUALITY);

            // Cover mode (crop đẹp thay vì stretch)
            installHeroCoverMode(imgView1);
            installHeroCoverMode(imgView2);

            activeImageView = imgView1;
            hiddenImageView = imgView2;

            imgView1.setOpacity(1);
            imgView2.setOpacity(0);
            imgView2.setVisible(false);
        }

        initTypeChips();
        enableTypeSwipe();

        // Load lần đầu:
        // 1) HERO load một lần
        // 2) List dưới theo tab mặc định
        loadHeroOnce();
        loadTypeContent(0);
    }

    // =========================
    // HERO: chỉ load 1 lần (không reload theo tab)
    // =========================
    private void loadHeroOnce() {
        Task<List<Movie>> task = new Task<>() {
            @Override
            protected List<Movie> call() {
                // Ưu tiên API home nếu service đã có
                try {
                    List<Movie> home = movieService.getHomeMovies();
                    if (home != null && !home.isEmpty()) return home;
                } catch (Exception ignored) {}

                // Fallback nếu chưa implement getHomeMovies()
                try {
                    List<Movie> list = movieService.getMoviesByList("phim-moi", 1);
                    if (list != null && !list.isEmpty()) return list;
                } catch (Exception ignored) {}

                return new ArrayList<>();
            }
        };

        task.setOnSucceeded(e -> {
            List<Movie> movies = task.getValue();
            if (movies == null || movies.isEmpty()) return;

            int bannerCount = Math.min(8, movies.size());
            heroMovies = new ArrayList<>(movies.subList(0, bannerCount));

            currentHeroIndex = 0;
            isTransitioning = false;
            imageCache.clear();
            heroResolving.clear();

            if (heroDots != null) heroDots.getChildren().clear();

            preloadImage(0);
            preloadImage(1);
            startCycle(0);
        });

        task.setOnFailed(e -> {
            Throwable err = task.getException();
            if (err != null) err.printStackTrace();
        });

        Thread t = new Thread(task, "home-hero-loader");
        t.setDaemon(true);
        t.start();
    }

    // =========================
    // TYPE CHIPS UI
    // =========================
    private void initTypeChips() {
        if (hbType == null) return;
        hbType.getChildren().clear();

        for (int i = 0; i < typeItems.size(); i++) {
            final int idx = i;
            ToggleButton chip = new ToggleButton(typeItems.get(i).title());
            chip.setToggleGroup(typeGroup);
            chip.getStyleClass().add("type-chip");
            chip.setOnAction(ev -> selectType(idx));
            hbType.getChildren().add(chip);
        }

        if (!hbType.getChildren().isEmpty()) {
            ((ToggleButton) hbType.getChildren().get(0)).setSelected(true);
        }
    }

    private void enableTypeSwipe() {
        if (hbType == null) return;

        hbType.addEventFilter(SwipeEvent.SWIPE_LEFT, e -> {
            selectType(selectedTypeIndex + 1);
            e.consume();
        });

        hbType.addEventFilter(SwipeEvent.SWIPE_RIGHT, e -> {
            selectType(selectedTypeIndex - 1);
            e.consume();
        });

        hbType.setOnMousePressed(e -> dragStartX = e.getScreenX());
        hbType.setOnMouseReleased(e -> {
            double dx = e.getScreenX() - dragStartX;
            if (Math.abs(dx) < 70) return;

            if (dx < 0) selectType(selectedTypeIndex + 1);
            else selectType(selectedTypeIndex - 1);
        });
    }

    private void selectType(int idx) {
        if (idx < 0 || idx >= typeItems.size()) return;
        if (idx == selectedTypeIndex) return; // tránh reload lại tab đang chọn

        selectedTypeIndex = idx;

        if (hbType != null && idx < hbType.getChildren().size()) {
            ((ToggleButton) hbType.getChildren().get(idx)).setSelected(true);
        }

        // CHỈ load phần dưới, không đụng hero
        loadTypeContent(idx);
        scrollChipIntoView(idx);
    }

    private void scrollChipIntoView(int idx) {
        if (spType == null || hbType == null) return;
        if (hbType.getChildren().isEmpty()) return;

        double totalW = hbType.getBoundsInLocal().getWidth();
        double viewportW = spType.getViewportBounds().getWidth();
        if (totalW <= viewportW || viewportW <= 0) return;

        Region node = (Region) hbType.getChildren().get(idx);
        double nodeMinX = node.getBoundsInParent().getMinX();
        double nodeW = node.getBoundsInParent().getWidth();
        double targetCenter = nodeMinX + nodeW / 2.0;

        double hVal = (targetCenter - viewportW / 2.0) / (totalW - viewportW);
        spType.setHvalue(Math.max(0, Math.min(1, hVal)));
    }

    // =========================
    // LIST DƯỚI HERO: load theo tab
    // =========================
    private void loadTypeContent(int idx) {
        if (idx < 0 || idx >= typeItems.size()) return;

        TypeItem item = typeItems.get(idx);

        if (lbSectionTitle != null) {
            lbSectionTitle.setText(item.title().toUpperCase(Locale.ROOT));
        }

        if (loadingBox != null) loadingBox.setVisible(true);
        if (mainContentScroll != null) mainContentScroll.setDisable(true);

        if (newMoviesContainer != null) {
            newMoviesContainer.getChildren().clear();
            newMoviesContainer.setOpacity(0.35);
            newMoviesContainer.setDisable(true);
        }

        Task<List<Movie>> task = new Task<>() {
            @Override
            protected List<Movie> call() {
                return movieService.getMoviesByList(item.listSlug(), 1);
            }
        };

        task.setOnSucceeded(e -> {
            List<Movie> movies = task.getValue();
            if (movies == null) movies = new ArrayList<>();

            if (newMoviesContainer != null) {
                int limit = Math.min(12, movies.size());
                for (int i = 0; i < limit; i++) {
                    newMoviesContainer.getChildren().add(createMovieCard(movies.get(i)));
                }
            }

            if (loadingBox != null) loadingBox.setVisible(false);
            if (mainContentScroll != null) mainContentScroll.setDisable(false);

            if (newMoviesContainer != null) {
                newMoviesContainer.setDisable(false);

                FadeTransition ft = new FadeTransition(Duration.millis(180), newMoviesContainer);
                ft.setFromValue(0.35);
                ft.setToValue(1.0);
                ft.play();
            }
        });

        task.setOnFailed(e -> {
            if (loadingBox != null) loadingBox.setVisible(false);
            if (mainContentScroll != null) mainContentScroll.setDisable(false);

            if (newMoviesContainer != null) {
                newMoviesContainer.setDisable(false);
                newMoviesContainer.setOpacity(1.0);
            }

            Throwable err = task.getException();
            if (err != null) err.printStackTrace();
        });

        Thread t = new Thread(task, "home-list-loader-" + item.listSlug());
        t.setDaemon(true);
        t.start();
    }

    // =========================
    // HERO SLIDER
    // =========================
    private void startCycle(int index) {
        if (heroMovies == null || heroMovies.isEmpty() || activeImageView == null) return;

        Image img = imageCache.get(index);
        if (img == null) {
            String quickUrl = pickHeroImageUrl(heroMovies.get(index));
            if (quickUrl != null && !quickUrl.isBlank()) {
                img = new Image(quickUrl, true);
                imageCache.put(index, img); // cache luôn ảnh fallback nhanh
            }
            resolveHeroImageAsync(index); // nâng cấp lên backdrop TMDB ở background
        }

        if (img != null) {
            activeImageView.setImage(img);
        }

        activeImageView.setVisible(true);
        activeImageView.setOpacity(1);
        activeImageView.setTranslateX(0);
        activeImageView.setScaleX(1.0);
        activeImageView.setScaleY(1.0);

        updateInfo(heroMovies.get(index));
        runZoomAnimation(activeImageView);
        runProgressBarAndTimer(index);
    }

    private void runZoomAnimation(ImageView img) {
        if (img == null) return;

        if (currentZoomAnimation != null) currentZoomAnimation.stop();

        currentZoomAnimation = new ScaleTransition(Duration.seconds(7), img);
        currentZoomAnimation.setFromX(1.0);
        currentZoomAnimation.setFromY(1.0);
        currentZoomAnimation.setToX(1.08);
        currentZoomAnimation.setToY(1.08);
        currentZoomAnimation.play();
    }

    private void runProgressBarAndTimer(int index) {
        if (heroMovies == null || heroMovies.isEmpty() || heroDots == null) return;

        setupProgressBars();
        preloadImage((index + 1) % heroMovies.size());

        if (index < 0 || index >= heroDots.getChildren().size()) return;

        StackPane container = (StackPane) heroDots.getChildren().get(index);
        if (container.getChildren().size() < 2) return;

        Rectangle bar = (Rectangle) container.getChildren().get(1);

        if (barAnimation != null) barAnimation.stop();
        barAnimation = new Timeline(new KeyFrame(
                SLIDE_TIME,
                new KeyValue(bar.scaleXProperty(), 1, Interpolator.LINEAR)
        ));
        barAnimation.play();

        if (autoSlideTimer != null) autoSlideTimer.stop();
        autoSlideTimer = new PauseTransition(SLIDE_TIME);
        autoSlideTimer.setOnFinished(e -> nextHero());
        autoSlideTimer.play();
    }

    private void setupProgressBars() {
        if (heroDots == null || heroMovies == null) return;

        heroDots.getChildren().clear();
        for (int i = 0; i < heroMovies.size(); i++) {
            Rectangle bg = new Rectangle(18, 3);
            bg.setArcWidth(10);
            bg.setArcHeight(10);
            bg.getStyleClass().add("indicator-track");

            Rectangle fg = new Rectangle(18, 3);
            fg.setArcWidth(10);
            fg.setArcHeight(10);
            fg.setScaleX(0);
            fg.setScaleY(1);
            fg.getStyleClass().add("indicator-bar");

            StackPane dot = new StackPane(bg, fg);
            dot.setAlignment(Pos.CENTER_LEFT);
            dot.getStyleClass().add("indicator-container");

            final int idx = i;
            dot.setOnMouseClicked(e -> jumpToHero(idx));

            heroDots.getChildren().add(dot);
        }
    }

    private void jumpToHero(int index) {
        if (heroMovies == null || heroMovies.isEmpty()) return;
        if (index < 0 || index >= heroMovies.size()) return;
        if (isTransitioning || index == currentHeroIndex) return;

        if (autoSlideTimer != null) autoSlideTimer.stop();
        if (barAnimation != null) barAnimation.stop();

        currentHeroIndex = index;
        startCycle(currentHeroIndex);
    }

    private void nextHero() {
        if (isTransitioning || heroMovies == null || heroMovies.isEmpty()) return;
        if (activeImageView == null || hiddenImageView == null) return;

        isTransitioning = true;
        int nextIndex = (currentHeroIndex + 1) % heroMovies.size();

        Image nextImg = imageCache.get(nextIndex);
        if (nextImg == null) {
            String quickUrl = pickHeroImageUrl(heroMovies.get(nextIndex));
            if (quickUrl != null && !quickUrl.isBlank()) {
                nextImg = new Image(quickUrl, true);
                imageCache.put(nextIndex, nextImg);
            }
            resolveHeroImageAsync(nextIndex);
        }

        if (nextImg != null) {
            hiddenImageView.setImage(nextImg);
        }

        hiddenImageView.setVisible(true);
        hiddenImageView.setOpacity(0);
        hiddenImageView.setTranslateX(40);
        hiddenImageView.setScaleX(1.0);
        hiddenImageView.setScaleY(1.0);

        FadeTransition fadeOut = new FadeTransition(TRANSITION_SPEED, activeImageView);
        fadeOut.setToValue(0);

        FadeTransition fadeIn = new FadeTransition(TRANSITION_SPEED, hiddenImageView);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(TRANSITION_SPEED, hiddenImageView);
        slideIn.setToX(0);
        slideIn.setInterpolator(CINEMATIC_EASE);

        ParallelTransition pt = new ParallelTransition(fadeOut, fadeIn, slideIn);
        pt.setOnFinished(e -> {
            ImageView tmp = activeImageView;
            activeImageView = hiddenImageView;
            hiddenImageView = tmp;

            hiddenImageView.setVisible(false);
            hiddenImageView.setOpacity(0);
            hiddenImageView.setTranslateX(0);

            currentHeroIndex = nextIndex;
            updateInfo(heroMovies.get(currentHeroIndex));
            runZoomAnimation(activeImageView);
            runProgressBarAndTimer(currentHeroIndex);

            isTransitioning = false;
        });
        pt.play();
    }

    private void preloadImage(int index) {
        if (heroMovies == null || heroMovies.isEmpty()) return;

        index = (index + heroMovies.size()) % heroMovies.size();
        if (imageCache.containsKey(index)) return;

        // preload nhanh bằng URL có sẵn trước
        String quickUrl = pickHeroImageUrl(heroMovies.get(index));
        if (quickUrl != null && !quickUrl.isBlank()) {
            Image img = new Image(quickUrl, true);
            imageCache.put(index, img);
        }

        // sau đó resolve backdrop chất lượng tốt hơn
        resolveHeroImageAsync(index);
    }

    private String pickHeroImageUrl(Movie movie) {
        if (movie == null) return "";

        String slug = safe(movie.getSlug()).trim();
        if (!slug.isEmpty()) {
            String cached = heroUrlBySlugCache.get(slug);
            if (cached != null && !cached.isBlank()) return cached;
        }

        // fallback nhanh từ dữ liệu list
        try {
            String poster = movie.getFullPosterUrl();
            if (poster != null && !poster.isBlank()) return poster;
        } catch (Exception ignored) {}

        try {
            String thumb = movie.getFullThumbUrl();
            if (thumb != null && !thumb.isBlank()) return thumb;
        } catch (Exception ignored) {}

        return "";
    }

    private void resolveHeroImageAsync(int index) {
        if (heroMovies == null || heroMovies.isEmpty()) return;

        index = (index + heroMovies.size()) % heroMovies.size();
        if (heroResolving.getOrDefault(index, false)) return;

        final int heroIdx = index;
        final Movie movie = heroMovies.get(heroIdx);
        if (movie == null) return;

        heroResolving.put(heroIdx, true);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                String slug = safe(movie.getSlug()).trim();

                // 1) cache theo slug
                if (!slug.isEmpty()) {
                    String cached = heroUrlBySlugCache.get(slug);
                    if (cached != null && !cached.isBlank()) return cached;
                }

                // 2) ưu tiên /images -> backdrop
                if (!slug.isEmpty()) {
                    try {
                        var imgs = movieService.getMovieImages(slug);
                        if (imgs != null) {
                            if (imgs.backdrops != null && !imgs.backdrops.isEmpty()) {
                                return imgs.backdrops.get(0);
                            }
                            if (imgs.posters != null && !imgs.posters.isEmpty()) {
                                return imgs.posters.get(0);
                            }
                        }
                    } catch (Exception ignored) {}
                }

                // 3) fallback local
                try {
                    String poster = movie.getFullPosterUrl();
                    if (poster != null && !poster.isBlank()) return poster;
                } catch (Exception ignored) {}

                try {
                    String thumb = movie.getFullThumbUrl();
                    if (thumb != null && !thumb.isBlank()) return thumb;
                } catch (Exception ignored) {}

                return "";
            }
        };

        task.setOnSucceeded(e -> {
            heroResolving.put(heroIdx, false);

            String url = task.getValue();
            if (url == null || url.isBlank()) return;

            String slug = safe(movie.getSlug()).trim();
            if (!slug.isEmpty()) {
                heroUrlBySlugCache.put(slug, url);
            }

            Image img = new Image(url, true);
            img.exceptionProperty().addListener((obs, oldEx, newEx) -> {
                if (newEx != null) {
                    System.out.println("Hero image error: " + url);
                    newEx.printStackTrace();
                }
            });

            imageCache.put(heroIdx, img);

            // chỉ update nếu slide hiện tại vẫn là heroIdx
            if (heroIdx == currentHeroIndex && activeImageView != null) {
                activeImageView.setImage(img);
            }
        });

        task.setOnFailed(e -> {
            heroResolving.put(heroIdx, false);
            Throwable ex = task.getException();
            if (ex != null) ex.printStackTrace();
        });

        Thread t = new Thread(task, "hero-image-resolver-" + heroIdx);
        t.setDaemon(true);
        t.start();
    }

    // Cover mode cho ImageView (crop thay vì méo)
    private void installHeroCoverMode(ImageView iv) {
        if (iv == null) return;

        iv.imageProperty().addListener((obs, oldImg, newImg) -> applyCoverViewport(iv));
        iv.fitWidthProperty().addListener((obs, o, n) -> applyCoverViewport(iv));
        iv.fitHeightProperty().addListener((obs, o, n) -> applyCoverViewport(iv));
        iv.boundsInLocalProperty().addListener((obs, o, n) -> applyCoverViewport(iv));
    }

    private void applyCoverViewport(ImageView iv) {
        if (iv == null) return;

        Image img = iv.getImage();
        if (img == null) {
            iv.setViewport(null);
            return;
        }

        double imgW = img.getWidth();
        double imgH = img.getHeight();
        double boxW = iv.getFitWidth();
        double boxH = iv.getFitHeight();

        // Ảnh chưa load xong hoặc khung chưa có size
        if (imgW <= 0 || imgH <= 0 || boxW <= 0 || boxH <= 0) {
            iv.setViewport(null);
            return;
        }

        double imageRatio = imgW / imgH;
        double boxRatio = boxW / boxH;

        double viewportW, viewportH, viewportX, viewportY;

        if (imageRatio > boxRatio) {
            // Ảnh rộng hơn khung -> cắt 2 bên
            viewportH = imgH;
            viewportW = imgH * boxRatio;
            viewportX = (imgW - viewportW) / 2.0;
            viewportY = 0;
        } else {
            // Ảnh cao hơn khung -> cắt trên/dưới, ưu tiên giữ vùng mặt (dịch lên nhẹ)
            viewportW = imgW;
            viewportH = imgW / boxRatio;
            viewportX = 0;
            viewportY = Math.max(0, (imgH - viewportH) * 0.32);
        }

        iv.setViewport(new Rectangle2D(viewportX, viewportY, viewportW, viewportH));
    }

    private void updateInfo(Movie movie) {
        if (movie == null) return;

        if (heroTitle != null) heroTitle.setText(safe(movie.getName()));
        if (heroOriginName != null) heroOriginName.setText(safe(movie.getOriginName()));

        if (heroYear != null) {
            Integer y = movie.getYear();
            heroYear.setText(y == null ? "" : String.valueOf(y));
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @FXML
    public void watchHeroMovie() {
        if (heroMovies == null || heroMovies.isEmpty()) return;
        SceneNavigator.loadWatchScene(heroMovies.get(currentHeroIndex));
    }

    // =========================
    // CARD IMAGE HELPERS
    // =========================
    private void setPlaceholderPoster(ImageView imageView) {
        try {
            Image ph = new Image(getClass().getResource("/images/placeholder-poster.png").toExternalForm());
            imageView.setImage(ph);
        } catch (Exception ignored) {
            // không có placeholder thì thôi
        }
    }

    private void loadImageWithFallback(ImageView imageView, List<String> candidates, Runnable onAllFailed) {
        loadImageWithFallback(imageView, candidates, 0, onAllFailed);
    }

    private void loadImageWithFallback(ImageView imageView, List<String> candidates, int index, Runnable onAllFailed) {
        if (candidates == null || index >= candidates.size()) {
            if (onAllFailed != null) onAllFailed.run();
            return;
        }

        String url = candidates.get(index);
        if (url == null || url.isBlank()) {
            loadImageWithFallback(imageView, candidates, index + 1, onAllFailed);
            return;
        }

        try {
            Image img = new Image(url, true);

            img.progressProperty().addListener((obs, ov, nv) -> {
                if (nv != null && nv.doubleValue() >= 1.0 && !img.isError()) {
                    imageView.setImage(img);
                }
            });

            img.exceptionProperty().addListener((obs, oldEx, ex) -> {
                if (ex != null) {
                    System.out.println("Image load error: " + url);
                    loadImageWithFallback(imageView, candidates, index + 1, onAllFailed);
                }
            });

        } catch (Exception e) {
            loadImageWithFallback(imageView, candidates, index + 1, onAllFailed);
        }
    }

    private List<String> buildCardImageCandidates(Movie movie) {
        List<String> urls = new ArrayList<>();
        if (movie == null) return urls;

        try {
            String thumb = movie.getFullThumbUrl();
            if (thumb != null && !thumb.isBlank()) urls.add(thumb);
        } catch (Exception ignored) {}

        try {
            String poster = movie.getFullPosterUrl();
            if (poster != null && !poster.isBlank() && !urls.contains(poster)) urls.add(poster);
        } catch (Exception ignored) {}

        return urls;
    }

    /**
     * Fallback cuối cùng: gọi API /phim/{slug}/images để lấy TMDB image.
     * Chạy nền, không block UI.
     */
    private void tryResolveCardImageFromApi(Movie movie, ImageView imageView) {
        if (movie == null || movie.getSlug() == null || movie.getSlug().isBlank()) {
            setPlaceholderPoster(imageView);
            return;
        }

        String slug = movie.getSlug();

        // cache nếu đã resolve trước đó
        if (resolvedPosterUrlCache.containsKey(slug)) {
            String cached = resolvedPosterUrlCache.get(slug);
            if (cached == null || cached.isBlank()) {
                setPlaceholderPoster(imageView);
                return;
            }
            loadImageWithFallback(imageView, List.of(cached), () -> setPlaceholderPoster(imageView));
            return;
        }

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                try {
                    var images = movieService.getMovieImages(slug);
                    if (images != null) {
                        if (images.posters != null && !images.posters.isEmpty()) {
                            return images.posters.get(0);
                        }
                        if (images.backdrops != null && !images.backdrops.isEmpty()) {
                            return images.backdrops.get(0);
                        }
                    }
                } catch (Exception ignored) {}
                return "";
            }
        };

        task.setOnSucceeded(e -> {
            String tmdbUrl = task.getValue();
            resolvedPosterUrlCache.put(slug, tmdbUrl == null ? "" : tmdbUrl);

            if (tmdbUrl != null && !tmdbUrl.isBlank()) {
                loadImageWithFallback(imageView, List.of(tmdbUrl), () -> setPlaceholderPoster(imageView));
            } else {
                setPlaceholderPoster(imageView);
            }
        });

        task.setOnFailed(e -> {
            resolvedPosterUrlCache.put(slug, "");
            setPlaceholderPoster(imageView);
        });

        Thread t = new Thread(task, "resolve-card-image-" + slug);
        t.setDaemon(true);
        t.start();
    }

    // =========================
    // MOVIE CARD
    // =========================
    private StackPane createMovieCard(Movie movie) {
        StackPane card = new StackPane();
        card.getStyleClass().add("movie-card");
        card.setPrefSize(140, 210);

        ImageView poster = new ImageView();
        poster.setFitWidth(140);
        poster.setFitHeight(210);
        poster.setPreserveRatio(true);
        poster.setSmooth(true);
        poster.setCache(true);
        poster.setCacheHint(CacheHint.QUALITY);

        List<String> candidates = buildCardImageCandidates(movie);

        // thử thumb/poster trước -> fallback cuối gọi API images (TMDB)
        loadImageWithFallback(poster, candidates, () -> tryResolveCardImageFromApi(movie, poster));

        Rectangle clip = new Rectangle(140, 210);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        poster.setClip(clip);

        Region gradient = new Region();
        gradient.getStyleClass().add("card-gradient");
        gradient.setMaxHeight(105);
        StackPane.setAlignment(gradient, Pos.BOTTOM_CENTER);

        VBox infoBox = new VBox(2);
        infoBox.getStyleClass().add("card-info");
        infoBox.setAlignment(Pos.BOTTOM_LEFT);

        Label lbYear = new Label(movie.getYear() == null ? "" : String.valueOf(movie.getYear()));
        lbYear.getStyleClass().add("card-year");

        Label lbName = new Label(safe(movie.getName()));
        lbName.getStyleClass().add("card-title");
        lbName.setWrapText(true);
        lbName.setMaxWidth(120);

        Label lbCate = new Label(buildCategoryText(movie));
        lbCate.getStyleClass().add("card-meta");
        lbCate.setWrapText(true);
        lbCate.setMaxWidth(120);

        infoBox.getChildren().addAll(lbYear, lbName, lbCate);

        // Badges góc trên (text-only)
        HBox topLeftBadges = new HBox(6);
        topLeftBadges.setAlignment(Pos.TOP_LEFT);
        StackPane.setAlignment(topLeftBadges, Pos.TOP_LEFT);
        StackPane.setMargin(topLeftBadges, new Insets(8, 8, 0, 8));

        String quality = normalizeQuality(movie.getQuality());
        if (!quality.isBlank()) {
            Label bQ = new Label(quality);
            bQ.getStyleClass().addAll("badge-pill", "badge-quality");
            topLeftBadges.getChildren().add(bQ);
        }

        String lang = normalizeLang(movie.getLang());
        if (!lang.isBlank()) {
            Label bL = new Label(lang);
            bL.getStyleClass().addAll("badge-pill", "badge-lang");
            topLeftBadges.getChildren().add(bL);
        }

        Label bEp = null;
        String epBadge = buildEpisodeBadge(movie);
        if (!epBadge.isBlank()) {
            bEp = new Label(epBadge);
            boolean done = epBadge.toLowerCase(Locale.ROOT).contains("hoàn tất")
                    || epBadge.toLowerCase(Locale.ROOT).contains("full");

            bEp.getStyleClass().addAll("badge-pill", done ? "badge-complete" : "badge-episode");
            StackPane.setAlignment(bEp, Pos.TOP_RIGHT);
            StackPane.setMargin(bEp, new Insets(8, 8, 0, 8));
        }

        if (bEp != null) {
            card.getChildren().addAll(poster, gradient, infoBox, topLeftBadges, bEp);
        } else {
            card.getChildren().addAll(poster, gradient, infoBox, topLeftBadges);
        }

        card.setOnMouseClicked(e -> SceneNavigator.loadWatchScene(movie));
        return card;
    }

    // =========================
    // NORMALIZE / FORMAT
    // =========================
    private String normalizeQuality(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.isEmpty()) return "";

        s = s.replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (s.contains("4K")) return "4K";
        if (s.contains("FHD") || s.contains("1080")) return "FHD";
        if (s.contains("HD") || s.contains("720")) return "HD";
        if (s.contains("CAM")) return "CAM";
        return s.length() > 10 ? s.substring(0, 10) : s;
    }

    private String normalizeLang(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.isEmpty()) return "";

        String low = s.toLowerCase(Locale.ROOT);

        if (low.contains("vietsub") || low.equals("sub") || low.contains("sub")) return "Vietsub";
        if (low.contains("thuyet") || low.equals("tm")) return "TM";
        if (low.contains("long") || low.equals("lt")) return "LT";

        return s.length() > 12 ? s.substring(0, 12) : s;
    }

    private String buildEpisodeBadge(Movie movie) {
        if (movie == null) return "";

        String cur = safe(movie.getEpisodeCurrent()).trim();
        String total = safe(movie.getEpisodeTotal()).trim();

        if (cur.isBlank() && total.isBlank()) return "";

        String lowCur = cur.toLowerCase(Locale.ROOT);
        if (lowCur.contains("hoàn") || lowCur.contains("full") || lowCur.contains("end")) {
            String totalNum = extractFirstNumber(total);
            if (!totalNum.isEmpty()) return "Hoàn tất " + totalNum + " tập";
            return "Hoàn tất";
        }

        String curNum = extractFirstNumber(cur);
        String totalNum = extractFirstNumber(total);

        if (!curNum.isEmpty() && !totalNum.isEmpty()) {
            if (curNum.equals(totalNum)) return "Hoàn tất " + totalNum + " tập";
            return curNum + "/" + totalNum;
        }

        if (!curNum.isEmpty()) return "Tập " + curNum;
        return cur;
    }

    private String extractFirstNumber(String s) {
        if (s == null) return "";
        var m = java.util.regex.Pattern.compile("(\\d+)").matcher(s);
        return m.find() ? m.group(1) : "";
    }

    private String buildCategoryText(Movie movie) {
        try {
            List<Movie.Taxonomy> cats = movie.getCategory();
            if (cats == null || cats.isEmpty()) return "";

            List<String> names = new ArrayList<>();
            for (Movie.Taxonomy c : cats) {
                if (c != null && c.getName() != null && !c.getName().isBlank()) {
                    names.add(c.getName().trim());
                }
                if (names.size() == 2) break;
            }
            return String.join(" • ", names);
        } catch (Exception e) {
            return "";
        }
    }

    public void stopTimerLogic() {
        if (autoSlideTimer != null) autoSlideTimer.stop();
        if (barAnimation != null) barAnimation.stop();
        if (currentZoomAnimation != null) currentZoomAnimation.stop();
    }
}