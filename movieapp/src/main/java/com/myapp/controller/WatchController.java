package com.myapp.controller;

import com.myapp.model.Episode;
import com.myapp.model.MovieDetail;
import com.myapp.service.MovieService;
import com.myapp.util.SceneNavigator;
import com.myapp.util.UiUtils;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.util.List;
import java.util.prefs.Preferences;

public class WatchController {
    // ===== FXML =====
    @FXML
    private Button btnBack;
    @FXML private Button btnNextEp;

    @FXML private Label lbTitle;
    @FXML private Label lbDesc;

    @FXML private StackPane videoStack;
    @FXML private MediaView mediaView;

    @FXML private Region overlayDim;
    @FXML private VBox overlayControls;

    @FXML private Button btnRestart;
    @FXML private Button btnPlayPause;
    @FXML private Button btnNextEp2;

    @FXML private Label lbTime;
    @FXML private Slider slSeek;

    @FXML private FlowPane episodePane;

    // ===== Logic =====
    private final MovieService service = new MovieService();
    private final Preferences prefs = Preferences.userRoot().node("movieapp");

    private String slug;
    private MovieDetail detail;

    private MediaPlayer player;
    private int currentEpIndex = 0;

    private boolean userDragging = false;
    private long lastSaveMs = 0;

    private final PauseTransition autoHide = new PauseTransition(Duration.seconds(2.2));

    @FXML
    private void initialize() {
        // Overlay auto hide
        autoHide.setOnFinished(e -> hideOverlay());

        // Tap vào vùng video để hiện/ẩn controls (giống YouTube)
        videoStack.setOnMouseClicked(e -> {
            toggleOverlay();
            e.consume();
        });

        // Seek
        slSeek.setOnMousePressed(e -> userDragging = true);
        slSeek.setOnMouseReleased(e -> {
            userDragging = false;
            if (player != null) {
                player.seek(Duration.seconds(slSeek.getValue()));
                saveProgressNow();
                scheduleAutoHide();
            }
        });

        Platform.runLater(() -> {
            // khóa tỉ lệ 16:9 cho khung video
            videoStack.setMinHeight(Region.USE_PREF_SIZE);
            videoStack.setPrefHeight(Region.USE_COMPUTED_SIZE);

            videoStack.prefHeightProperty().bind(
                    videoStack.widthProperty().multiply(9.0 / 16.0)
            );

            // MediaView fit full khung videoStack
            mediaView.fitWidthProperty().bind(videoStack.widthProperty());
            mediaView.fitHeightProperty().bind(videoStack.heightProperty());
        });


        setOverlayVisible(false);
        lbTime.setText("00:00 / 00:00");
        syncPlayPauseText();
    }

    // SceneNavigator reflect gọi setSlug
    public void setSlug(String slug) {
        this.slug = slug;
        loadDetail();
    }

    private void loadDetail() {
        if (slug == null || slug.isBlank()) return;

        UiUtils.runAsync(
                () -> {
                    try {
                        return service.getDetail(slug);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                d -> {
                    this.detail = d;

                    lbTitle.setText(safe(d.getName(), "Phim") + (d.getYear() > 0 ? " (" + d.getYear() + ")" : ""));
                    lbDesc.setText(d.getContent() == null ? "" : d.getContent());

                    List<Episode> eps = d.getEpisodes();
                    if (eps == null || eps.isEmpty()) {
                        UiUtils.info("Phim này chưa có tập/link xem.");
                        return;
                    }

                    renderEpisodes(eps);

                    // Resume
                    int epIndex = prefs.getInt(key("epIndex"), 0);
                    double sec = prefs.getDouble(key("sec"), 0);

                    if (epIndex < 0 || epIndex >= eps.size()) epIndex = 0;
                    currentEpIndex = epIndex;

                    playEpisode(currentEpIndex, sec);
                    highlightActiveEpisode();
                },
                ex -> UiUtils.error("Lỗi tải chi tiết phim: " + rootMsg(ex))
        );
    }

    private void renderEpisodes(List<Episode> eps) {
        episodePane.getChildren().clear();

        for (int i = 0; i < eps.size(); i++) {
            int idx = i;
            Episode ep = eps.get(i);

            Button b = new Button("▶  " + safe(ep.getName(), String.valueOf(i + 1)));
            b.getStyleClass().add("episode-btn");
            b.setPrefWidth(110);
            b.setPrefHeight(54);

            b.setOnAction(e -> {
                currentEpIndex = idx;
                highlightActiveEpisode();
                playEpisode(currentEpIndex, 0);
                saveProgressNow();
            });

            episodePane.getChildren().add(b);
        }
    }

    private void highlightActiveEpisode() {
        for (int i = 0; i < episodePane.getChildren().size(); i++) {
            var n = episodePane.getChildren().get(i);
            n.getStyleClass().remove("episode-btn-active");
            if (i == currentEpIndex) n.getStyleClass().add("episode-btn-active");
        }
    }

    private void playEpisode(int epIndex, double resumeSeconds) {
        if (detail == null || detail.getEpisodes() == null || detail.getEpisodes().isEmpty()) return;

        List<Episode> eps = detail.getEpisodes();
        if (epIndex < 0 || epIndex >= eps.size()) return;

        String url = eps.get(epIndex).getFileUrl();
        if (url == null || url.isBlank()) {
            UiUtils.info("Tập này chưa có link xem.");
            return;
        }

        disposePlayer();

        try {
            player = new MediaPlayer(new Media(url));
            mediaView.setMediaPlayer(player);

            player.setOnReady(() -> {
                Duration total = player.getTotalDuration();
                double totalSec = (total != null && !total.isUnknown()) ? total.toSeconds() : 0;

                slSeek.setMax(Math.max(1, totalSec));

                if (resumeSeconds > 1 && totalSec > 0 && resumeSeconds < totalSec - 2) {
                    player.seek(Duration.seconds(resumeSeconds));
                }

                player.play();
                syncPlayPauseText();
                showOverlay();           // vào là hiện controls 1 nhịp
                scheduleAutoHide();
            });

            player.currentTimeProperty().addListener((obs, ov, nv) -> {
                if (player == null) return;
                Duration total = player.getTotalDuration();
                if (total == null || total.isUnknown()) return;

                double curSec = nv.toSeconds();
                double totalSec = total.toSeconds();

                if (!userDragging) slSeek.setValue(curSec);
                lbTime.setText(fmt(curSec) + " / " + fmt(totalSec));

                autoSaveThrottle(curSec, totalSec);
            });

            player.setOnEndOfMedia(() -> {
                saveProgressNow();
                // auto next episode
                goNextEpisode();
            });

            player.setOnError(() -> UiUtils.error("Lỗi phát video: " + player.getError()));

        } catch (Exception ex) {
            UiUtils.error("Không phát được video: " + ex.getMessage());
        }
    }

    // ===== Overlay controls actions =====
    @FXML
    private void onPlayPause() {
        if (player == null) return;

        MediaPlayer.Status st = player.getStatus();
        if (st == MediaPlayer.Status.PLAYING) player.pause();
        else player.play();

        syncPlayPauseText();
        saveProgressNow();
        scheduleAutoHide();
    }

    @FXML
    private void onRestart() {
        if (player == null) return;

        player.seek(Duration.ZERO);
        player.play();
        syncPlayPauseText();
        saveProgressNow();
        scheduleAutoHide();
    }

    @FXML
    private void onNextEpisode() {
        goNextEpisode();
    }

    private void goNextEpisode() {
        if (detail == null || detail.getEpisodes() == null) return;

        int size = detail.getEpisodes().size();
        if (size <= 0) return;

        int next = currentEpIndex + 1;
        if (next >= size) {
            UiUtils.info("Bạn đang ở tập cuối rồi.");
            return;
        }

        currentEpIndex = next;
        highlightActiveEpisode();
        playEpisode(currentEpIndex, 0);
        saveProgressNow();
    }

    @FXML
    private void onBack() {
        disposePlayer();
        SceneNavigator.goHome();
    }

    // ===== Overlay helpers =====
    private void toggleOverlay() {
        if (overlayControls.isVisible()) hideOverlay();
        else {
            showOverlay();
            scheduleAutoHide();
        }
    }

    private void showOverlay() {
        overlayControls.setVisible(true);
        overlayControls.setManaged(false); // LUÔN false
        overlayControls.setMouseTransparent(false);

        overlayDim.setVisible(true);
        overlayDim.setManaged(false);      // LUÔN false
    }

    private void hideOverlay() {
        overlayControls.setVisible(false);
        overlayControls.setManaged(false); // LUÔN false
        overlayControls.setMouseTransparent(true);

        overlayDim.setVisible(false);
        overlayDim.setManaged(false);      // LUÔN false
    }


    private void scheduleAutoHide() {
        autoHide.stop();
        autoHide.playFromStart();
    }

    private void setOverlayVisible(boolean show) {
        overlayControls.setVisible(show);
        overlayControls.setManaged(show);

        overlayDim.setVisible(show);
        overlayDim.setManaged(false);
    }

    private void syncPlayPauseText() {
        if (btnPlayPause == null) return;
        if (player == null) {
            btnPlayPause.setText("▶ Phát");
            return;
        }
        btnPlayPause.setText(player.getStatus() == MediaPlayer.Status.PLAYING ? "⏸ Tạm dừng" : "▶ Phát");
    }

    // ===== Progress save =====
    private void autoSaveThrottle(double curSec, double totalSec) {
        long now = System.currentTimeMillis();
        if (now - lastSaveMs < 3000) return;
        lastSaveMs = now;

        prefs.putDouble(key("sec"), curSec);
        prefs.putDouble(key("total"), totalSec);
        prefs.putInt(key("epIndex"), Math.max(0, currentEpIndex));
        prefs.putLong(key("updated"), now);
    }

    private void saveProgressNow() {
        long now = System.currentTimeMillis();

        prefs.putInt(key("epIndex"), Math.max(0, currentEpIndex));
        prefs.putLong(key("updated"), now);

        if (player == null) {
            prefs.putDouble(key("sec"), 0);
            prefs.putDouble(key("total"), 0);
            return;
        }

        Duration cur = player.getCurrentTime();
        Duration total = player.getTotalDuration();

        double curSec = cur != null ? cur.toSeconds() : 0;
        double totalSec = (total != null && !total.isUnknown()) ? total.toSeconds() : 0;

        prefs.putDouble(key("sec"), curSec);
        prefs.putDouble(key("total"), totalSec);
    }

    private void disposePlayer() {
        try {
            if (player != null) {
                saveProgressNow();
                player.stop();
                player.dispose();
                player = null;
            }
        } catch (Exception ignore) {}
    }

    private String key(String suffix) {
        return slug + ":" + suffix;
    }

    private String safe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    private String fmt(double sec) {
        int s = (int) Math.max(0, Math.floor(sec));
        int m = s / 60;
        int r = s % 60;
        return String.format("%02d:%02d", m, r);
    }

    private String rootMsg(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage();
    }
}
