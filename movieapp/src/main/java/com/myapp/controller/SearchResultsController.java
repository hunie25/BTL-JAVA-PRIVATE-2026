package com.myapp.controller;

import com.myapp.model.Movie;
import com.myapp.service.MovieService;
import com.myapp.util.SceneNavigator;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SearchResultsController {
    @FXML private FlowPane resultContainer;
    @FXML private TextField txtSearchInner;
    @FXML private Label lblStatus;

    private final MovieService movieService = new MovieService();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(500));

    // Card size giống Home/Category
    private static final double CARD_W = 140;
    private static final double CARD_H = 210;

    // Cache ảnh (để scroll không reload liên tục)
    private static final Map<String, SoftReference<Image>> IMG_CACHE = new ConcurrentHashMap<>();

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
        }, "search-loader").start();
    }

    private StackPane createMovieCard(Movie movie) {
        StackPane card = new StackPane();
        card.getStyleClass().add("movie-card");
        card.setPrefSize(CARD_W, CARD_H);

        ImageView poster = new ImageView();
        poster.setFitWidth(CARD_W);
        poster.setFitHeight(CARD_H);
        poster.setPreserveRatio(true);
        poster.setSmooth(true);
        poster.setCache(true);
        poster.setCacheHint(CacheHint.QUALITY);

        // ✅ Ưu tiên poster -> thumb
        String imgUrl = safe(movie.getFullPosterUrl());
        if (imgUrl.isBlank()) imgUrl = safe(movie.getFullThumbUrl());

        Image img = cachedImageHomeStyle(imgUrl);
        if (img != null) {
            poster.setImage(img);
            applyCoverWhenReady(poster, img, CARD_W, CARD_H);
        }

        Rectangle clip = new Rectangle(CARD_W, CARD_H);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        poster.setClip(clip);

        // Gradient đáy giống Home
        Region gradient = new Region();
        gradient.getStyleClass().add("card-gradient");
        gradient.setMaxHeight(CARD_H / 2.0);
        StackPane.setAlignment(gradient, Pos.BOTTOM_CENTER);

        // Info box giống Home (Year + Title + Meta)
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

        // Badges TOP-LEFT: HD + Vietsub/TM/LT giống Home
        HBox topLeftBadges = new HBox(6);
        topLeftBadges.setAlignment(Pos.TOP_LEFT);
        topLeftBadges.setMaxWidth(CARD_W - 52);
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

        // Badge TOP-RIGHT: Tập/Hoàn tất giống Home
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

        // Build layer
        if (bEp != null) card.getChildren().addAll(poster, gradient, infoBox, topLeftBadges, bEp);
        else card.getChildren().addAll(poster, gradient, infoBox, topLeftBadges);

        card.setOnMouseClicked(e -> SceneNavigator.loadWatchScene(movie));
        return card;
    }
    // ====== Load ảnh giống Home: new Image(url, true) => nét ======
    private static Image cachedImageHomeStyle(String url) {
        if (url == null || url.isBlank()) return null;

        SoftReference<Image> ref = IMG_CACHE.get(url);
        Image img = (ref != null) ? ref.get() : null;
        if (img != null) return img;

        img = new Image(url, true); // y hệt Home: nét
        IMG_CACHE.put(url, new SoftReference<>(img));
        return img;
    }

    // ====== Cover crop khi ảnh load xong (fix ảnh ngang/letterbox) ======
    private void applyCoverWhenReady(ImageView iv, Image img, double boxW, double boxH) {
        if (img == null) return;

        Runnable crop = () -> {
            double iw = img.getWidth();
            double ih = img.getHeight();
            if (iw <= 0 || ih <= 0) return;

            double boxRatio = boxW / boxH;
            double imgRatio = iw / ih;

            double vw, vh;
            if (imgRatio > boxRatio) {
                // ảnh quá rộng -> cắt 2 bên
                vh = ih;
                vw = ih * boxRatio;
            } else {
                // ảnh quá cao -> cắt trên/dưới
                vw = iw;
                vh = iw / boxRatio;
            }

            double vx = (iw - vw) / 2.0;
            double vy = (ih - vh) / 2.0;

            iv.setViewport(new Rectangle2D(vx, vy, vw, vh));
        };

        crop.run();
        img.widthProperty().addListener((o, ov, nv) -> crop.run());
        img.heightProperty().addListener((o, ov, nv) -> crop.run());
        img.progressProperty().addListener((o, ov, nv) -> {
            if (nv.doubleValue() >= 1.0) crop.run();
        });
    }

    private String safe(String s) { return s == null ? "" : s; }

    @FXML
    private void goBack() { SceneNavigator.loadHome(); }

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
            return "Tập " + curNum;
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
}