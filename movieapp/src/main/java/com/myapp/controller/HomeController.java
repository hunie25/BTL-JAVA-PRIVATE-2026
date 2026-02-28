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
import javafx.scene.control.*;
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

    @FXML
    private StackPane phoneFrame;
    @FXML
    private StackPane appContent;

    @FXML
    private StackPane heroContainer;
    @FXML
    private StackPane sliderClipPane;
    @FXML
    private ImageView imgView1, imgView2;
    private ImageView activeImageView, hiddenImageView;

    @FXML
    private VBox heroInfoContainer;
    @FXML
    private Label heroTitle, heroOriginName, heroYear;
    @FXML
    private HBox heroDots;

    @FXML private HBox newMoviesContainer;
    @FXML private HBox seriesHotContainer;
    @FXML private HBox singleNewContainer;
    @FXML private HBox animeFeaturedContainer;

    @FXML
    private VBox loadingBox;
    @FXML
    private ScrollPane mainContentScroll;

    @FXML
    private ScrollPane spType;
    @FXML
    private HBox hbType;
    @FXML
    private Label lbSectionTitle;

    @FXML private StackPane exploreOverlay;
    @FXML private GridPane exploreGrid;


    @FXML private HBox searchBar;
    @FXML private TextField txtSearch;
    @FXML private Button btnUser;

    private final ToggleGroup typeGroup = new ToggleGroup();
    private double dragStartX;

    private record TypeItem(String title, String listSlug) {
    }

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

    // Thứ tự hiển thị giống Hub (2 cột như screenshot)
    private final List<TypeItem> exploreItems = List.of(
            new TypeItem("Phim mới", "phim-moi"),
            new TypeItem("Phim bộ", "phim-bo"),
            new TypeItem("Phim lẻ", "phim-le"),
            new TypeItem("TV Shows", "tv-shows"),
            new TypeItem("Hoạt hình", "hoat-hinh"),
            new TypeItem("Vietsub", "phim-vietsub"),
            new TypeItem("Thuyết minh", "phim-thuyet-minh"),
            new TypeItem("Lồng tiếng", "phim-long-tien"),
            new TypeItem("Bộ đang chiếu", "phim-bo-dang-chieu"),
            new TypeItem("Bộ hoàn thành", "phim-bo-hoan-thanh"),
            new TypeItem("Sắp chiếu", "phim-sap-chieu"),
            new TypeItem("Subteam", "subteam"),
            new TypeItem("Chiếu rạp", "phim-chieu-rap")
    );

    private int selectedTypeIndex = 0;

    private List<Movie> heroMovies = new ArrayList<>();
    private int currentHeroIndex = 0;

    private final Map<Integer, Image> imageCache = new HashMap<>();

    private final Map<String, String> heroUrlBySlugCache = new HashMap<>();

    private final Map<Integer, Boolean> heroResolving = new HashMap<>();

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

        searchScheduler = new PauseTransition(Duration.millis(500));
        searchScheduler.setOnFinished(event -> performSearch());

        loadCategoryToContainer("phim-bo", seriesMoviesContainer);
        loadCategoryToContainer("phim-le", singleMoviesContainer);

        if (appContent != null) {
            Rectangle screenClip = new Rectangle();
            screenClip.widthProperty().bind(appContent.widthProperty());
            screenClip.heightProperty().bind(appContent.heightProperty());
            screenClip.setArcWidth(35);
            screenClip.setArcHeight(35);
            appContent.setClip(screenClip);
        }

        if (sliderClipPane != null && heroContainer != null && imgView1 != null && imgView2 != null) {
            sliderClipPane.prefWidthProperty().bind(heroContainer.widthProperty());
            sliderClipPane.prefHeightProperty().bind(heroContainer.heightProperty());

            Rectangle heroClip = new Rectangle();
            heroClip.widthProperty().bind(sliderClipPane.widthProperty());
            heroClip.heightProperty().bind(sliderClipPane.heightProperty());
            sliderClipPane.setClip(heroClip);

            imgView1.fitWidthProperty().bind(sliderClipPane.widthProperty());
            imgView1.fitHeightProperty().bind(sliderClipPane.heightProperty());
            imgView2.fitWidthProperty().bind(sliderClipPane.widthProperty());
            imgView2.fitHeightProperty().bind(sliderClipPane.heightProperty());

            imgView1.setPreserveRatio(true);
            imgView2.setPreserveRatio(true);

            imgView1.setSmooth(true);
            imgView2.setSmooth(true);

            imgView1.setCache(true);
            imgView1.setCacheHint(CacheHint.QUALITY);
            imgView2.setCache(true);
            imgView2.setCacheHint(CacheHint.QUALITY);

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

        initExploreOverlay();

        loadHeroOnce();
        loadTypeContent(0);

        loadExtraHomeSections();
    }

    private void loadExtraHomeSections() {
        loadListInto("phim-bo", seriesHotContainer, 12);
        loadListInto("phim-le", singleNewContainer, 12);
        loadListInto("hoat-hinh", animeFeaturedContainer, 12);
    }

    private void loadListInto(String listSlug, HBox target, int limit) {
        if (target == null) return;

        Task<List<Movie>> task = new Task<>() {
            @Override
            protected List<Movie> call() {
                try {
                    return movieService.getMoviesByList(listSlug, 1);
                } catch (Exception e) {
                    return new ArrayList<>();
                }
            }
        };

        task.setOnSucceeded(e -> {
            List<Movie> movies = task.getValue();
            target.getChildren().clear();

            if (movies == null || movies.isEmpty()) return;

            int n = Math.min(limit, movies.size());
            for (int i = 0; i < n; i++) {
                target.getChildren().add(createMovieCard(movies.get(i)));
            }
        });

        Thread t = new Thread(task, "home-section-" + listSlug);
        t.setDaemon(true);
        t.start();
    }

    private void loadHeroOnce() {
        Task<List<Movie>> task = new Task<>() {
            @Override
            protected List<Movie> call() {
                try {
                    List<Movie> home = movieService.getHomeMovies();
                    if (home != null && !home.isEmpty()) return home;
                } catch (Exception ignored) {
                }

                try {
                    List<Movie> list = movieService.getMoviesByList("phim-moi", 1);
                    if (list != null && !list.isEmpty()) return list;
                } catch (Exception ignored) {
                }

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

    private void startCycle(int index) {
        if (heroMovies == null || heroMovies.isEmpty() || activeImageView == null) return;

        Image img = imageCache.get(index);
        if (img == null) {
            String quickUrl = pickHeroImageUrl(heroMovies.get(index));
            if (quickUrl != null && !quickUrl.isBlank()) {
                img = new Image(quickUrl, true);
                imageCache.put(index, img);
            }
            resolveHeroImageAsync(index);
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

        String quickUrl = pickHeroImageUrl(heroMovies.get(index));
        if (quickUrl != null && !quickUrl.isBlank()) {
            Image img = new Image(quickUrl, true);
            imageCache.put(index, img);
        }
        resolveHeroImageAsync(index);
    }

    private String pickHeroImageUrl(Movie movie) {
        if (movie == null) return "";

        String slug = safe(movie.getSlug()).trim();
        if (!slug.isEmpty()) {
            String cached = heroUrlBySlugCache.get(slug);
            if (cached != null && !cached.isBlank()) return cached;
        }

        try {
            String poster = movie.getFullPosterUrl();
            if (poster != null && !poster.isBlank()) return poster;
        } catch (Exception ignored) {
        }

        try {
            String thumb = movie.getFullThumbUrl();
            if (thumb != null && !thumb.isBlank()) return thumb;
        } catch (Exception ignored) {
        }

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

                if (!slug.isEmpty()) {
                    String cached = heroUrlBySlugCache.get(slug);
                    if (cached != null && !cached.isBlank()) return cached;
                }

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
                    } catch (Exception ignored) {
                    }
                }

                try {
                    String poster = movie.getFullPosterUrl();
                    if (poster != null && !poster.isBlank()) return poster;
                } catch (Exception ignored) {
                }

                try {
                    String thumb = movie.getFullThumbUrl();
                    if (thumb != null && !thumb.isBlank()) return thumb;
                } catch (Exception ignored) {
                }

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
            viewportH = imgH;
            viewportW = imgH * boxRatio;
            viewportX = (imgW - viewportW) / 2.0;
            viewportY = 0;
        } else {
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
            String y = movie.getYear();
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
        } catch (Exception ignored) {
        }

        try {
            String poster = movie.getFullPosterUrl();
            if (poster != null && !poster.isBlank() && !urls.contains(poster)) urls.add(poster);
        } catch (Exception ignored) {
        }

        return urls;
    }

    private void tryResolveCardImageFromApi(Movie movie, ImageView imageView) {
        if (movie == null || movie.getSlug() == null || movie.getSlug().isBlank()) {
            setPlaceholderPoster(imageView);
            return;
        }

        String slug = movie.getSlug();

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
                } catch (Exception ignored) {
                }
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

        HBox topLeftBadges = new HBox(6);
        topLeftBadges.setAlignment(Pos.TOP_LEFT);
        topLeftBadges.setMaxWidth(140 - 52);// chừa chỗ bên phải cho badge tập/hoàn tất
        StackPane.setAlignment(topLeftBadges, Pos.TOP_LEFT);
        StackPane.setMargin(topLeftBadges, new Insets(8, 8, 0, 8));

        String quality = normalizeQuality(movie.getQuality());
        if (!quality.isBlank()) {
            Label bQ = new Label(quality);
            bQ.setMaxWidth(36);
            bQ.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
            bQ.getStyleClass().addAll("badge-pill", "badge-quality");
            topLeftBadges.getChildren().add(bQ);
        }

        String lang = normalizeLang(movie.getLang());
        if (!lang.isBlank()) {
            Label bL = new Label(lang);
            bL.setMaxWidth(68);
            bL.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
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

    @FXML
    private void handleUserClick() {
        if (com.myapp.util.SessionManager.getUser() == null) {
            System.out.println("Chưa đăng nhập -> Chuyển sang màn hình Login");
            com.myapp.util.SceneNavigator.goLogin();
        } else {
            com.myapp.util.SceneNavigator.goProfile();
        }
    }

    @FXML
    private void toggleSearch() {
        if (searchBar == null) return;

        boolean isVisible = searchBar.isVisible();

        if (!isVisible) {
            searchBar.setManaged(true);
            searchBar.setVisible(true);
            searchBar.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(200), searchBar);
            ft.setToValue(1.0);
            ft.play();
            txtSearch.requestFocus();
        } else {
            searchBar.setVisible(false);
            searchBar.setManaged(false);
            txtSearch.clear();
            selectType(0);
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) return;

        if (lbSectionTitle != null) {
            lbSectionTitle.setText("KẾT QUẢ: " + keyword.toUpperCase());
        }

        if (newMoviesContainer != null) {
            newMoviesContainer.getChildren().clear();
            newMoviesContainer.setOpacity(0.5);
        }

        Task<List<Movie>> searchTask = new Task<>() {
            @Override
            protected List<Movie> call() throws Exception {
                return movieService.getOphimClient().searchMovies(keyword, 1);
            }
        };

        searchTask.setOnSucceeded(e -> {
            List<Movie> results = searchTask.getValue();
            if (newMoviesContainer != null) {
                newMoviesContainer.setOpacity(1.0);
                if (results == null || results.isEmpty()) {
                    Label noResult = new Label("Không tìm thấy phim nào phù hợp.");
                    noResult.setStyle("-fx-text-fill: gray; -fx-padding: 20;");
                    newMoviesContainer.getChildren().add(noResult);
                } else {
                    for (Movie m : results) {
                        newMoviesContainer.getChildren().add(createMovieCard(m));
                    }
                }
            }
        });

        Thread t = new Thread(searchTask);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void goHome() {
        mainContentScroll.setVvalue(0);
    }

    private PauseTransition searchScheduler;

    @FXML
    private void handleSearchKeyReleased() {
        searchScheduler.playFromStart();
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.length() >= 2) {
            System.out.println("Đang tìm kiếm: " + keyword);

            SceneNavigator.loadSearchScene(keyword);
        }
    }

    private void loadCategoryToContainer(String typeSlug, HBox container) {
        Task<List<Movie>> task = new Task<>() {
            @Override
            protected List<Movie> call() {
                return movieService.getMoviesByList(typeSlug, 1);
            }
        };

        task.setOnSucceeded(e -> {
            List<Movie> movies = task.getValue();
            if (movies != null) {
                container.getChildren().clear();
                int limit = Math.min(12, movies.size());
                for (int i = 0; i < limit; i++) {
                    container.getChildren().add(createMovieCard(movies.get(i)));
                }
            }
        }

    private void initExploreOverlay() {
        if (exploreGrid == null) return;

        exploreGrid.getChildren().clear();

        // 2 cột cân nhau
        if (exploreGrid.getColumnConstraints().isEmpty()) {
            ColumnConstraints c1 = new ColumnConstraints();
            c1.setPercentWidth(50);
            c1.setHgrow(Priority.ALWAYS);

            ColumnConstraints c2 = new ColumnConstraints();
            c2.setPercentWidth(50);
            c2.setHgrow(Priority.ALWAYS);

            exploreGrid.getColumnConstraints().addAll(c1, c2);
        }

        for (int i = 0; i < typeItems.size(); i++) {
            TypeItem item = typeItems.get(i);

            Button b = new Button(item.title());
            b.getStyleClass().add("explore-item");
            b.setMaxWidth(Double.MAX_VALUE);

            final String title = item.title();
            final String slug = item.listSlug();
            b.setOnAction(e -> {
                closeExplore();
                SceneNavigator.loadCategoryScene(title, slug);
            });

            int col = i % 2;
            int row = i / 2;

            exploreGrid.add(b, col, row);
            GridPane.setHgrow(b, Priority.ALWAYS);
        }
    }

    @FXML
    private void openExplore() {
        System.out.println(">>> CLICK KHAM PHA");
        if (exploreOverlay == null) {
            System.out.println("!!! exploreOverlay is NULL (chưa add overlay vào home.fxml hoặc fx:id sai)");
            return;
        }
        exploreOverlay.setVisible(true);
        exploreOverlay.setManaged(true);
        if (mainContentScroll != null) mainContentScroll.setDisable(true);
    }

    @FXML
    private void closeExplore() {
        if (exploreOverlay == null) return;
        exploreOverlay.setVisible(false);
        exploreOverlay.setManaged(false);
        if (mainContentScroll != null) mainContentScroll.setDisable(false);
    }
}