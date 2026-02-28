package com.myapp.controller;

import com.myapp.model.Movie;
import com.myapp.service.MovieService;
import com.myapp.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Rectangle2D;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CategoryController {

    @FXML private Label lbTitle;
    @FXML private ScrollPane spGrid;
    @FXML private TilePane gridContainer;

    private final MovieService movieService = new MovieService();
    private String listSlug;

    // ===== EXACT like HomeController.createMovieCard =====
    private static final double CARD_W = 140;
    private static final double CARD_H = 210;

    private static final Map<String, SoftReference<Image>> IMG_CACHE = new ConcurrentHashMap<>();

    public void initialize() {
        if (gridContainer == null) return;

        gridContainer.setPrefColumns(2);
        gridContainer.setHgap(18);
        gridContainer.setVgap(22);

        // tile size = card size (giống HOME)
        gridContainer.setPrefTileWidth(CARD_W);
        gridContainer.setPrefTileHeight(CARD_H);
    }

    public void initCategory(String title, String listSlug) {
        this.listSlug = listSlug;
        if (lbTitle != null) lbTitle.setText(title);
        loadPage1();
    }

    private void loadPage1() {
        final String slug = this.listSlug;
        if (slug == null || slug.isBlank()) return;

        new Thread(() -> {
            try {
                List<Movie> movies = movieService.getMoviesByList(slug, 1);
                Platform.runLater(() -> {
                    gridContainer.getChildren().clear();
                    if (movies == null) return;
                    for (Movie m : movies) {
                        gridContainer.getChildren().add(createHomeLikeCard(m));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "category-loader-" + slug).start();
    }

    // ===================== HOME-LIKE CARD =====================
    private StackPane createHomeLikeCard(Movie movie) {
        StackPane card = new StackPane();
        card.getStyleClass().add("movie-card");
        card.setPrefSize(CARD_W, CARD_H);

        ImageView poster = new ImageView();
        poster.setFitWidth(CARD_W);
        poster.setFitHeight(CARD_H);
        poster.setPreserveRatio(true);     // giữ tỉ lệ
        poster.setSmooth(true);            // Home đang smooth true và nét vì ảnh gốc to
        poster.setCache(true);
        poster.setCacheHint(CacheHint.QUALITY);


        String url = safe(movie.getFullPosterUrl());
        if (url.isBlank()) url = safe(movie.getFullThumbUrl());

        Image img = cachedImageHomeStyle(url);
        if (img != null) {
            poster.setImage(img);
            applyCoverWhenReady(poster, img, CARD_W, CARD_H); // cover-crop để "ăn khung"
        }

        Rectangle clip = new Rectangle(CARD_W, CARD_H);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        poster.setClip(clip);

        Region gradient = new Region();
        gradient.getStyleClass().add("card-gradient");
        gradient.setMaxHeight(CARD_H / 2.0);
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

        if (bEp != null) card.getChildren().addAll(poster, gradient, infoBox, topLeftBadges, bEp);
        else card.getChildren().addAll(poster, gradient, infoBox, topLeftBadges);

        card.setOnMouseClicked(e -> SceneNavigator.loadWatchScene(movie));
        return card;
    }

    private static Image cachedImageHomeStyle(String url) {
        if (url == null || url.isBlank()) return null;

        // cache theo URL giống Home (ảnh gốc -> nét)
        SoftReference<Image> ref = IMG_CACHE.get(url);
        Image img = (ref != null) ? ref.get() : null;
        if (img != null) return img;

        // Y HỆT HomeController.loadImageWithFallback: new Image(url, true)
        img = new Image(url, true);

        IMG_CACHE.put(url, new SoftReference<>(img));
        return img;
    }

    // ===== Helpers copy from HomeController style =====
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

    private String safe(String s) { return s == null ? "" : s; }

    @FXML
    private void goBack() { SceneNavigator.loadHome(); }

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

        // thử crop ngay (nếu ảnh đã load xong)
        crop.run();

        // nếu ảnh load nền, crop khi ready
        img.widthProperty().addListener((o, ov, nv) -> crop.run());
        img.heightProperty().addListener((o, ov, nv) -> crop.run());
        img.progressProperty().addListener((o, ov, nv) -> {
            if (nv.doubleValue() >= 1.0) crop.run();
        });
    }
}