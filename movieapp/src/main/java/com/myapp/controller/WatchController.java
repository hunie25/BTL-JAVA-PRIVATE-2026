package com.myapp.controller;

import com.myapp.model.Episode;
import com.myapp.model.Movie;
import com.myapp.model.MovieResponse;
import com.myapp.model.User;
import com.myapp.service.MovieService;
import com.myapp.util.SceneNavigator;
import com.myapp.util.UiUtils;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.myapp.util.SessionManager;
import com.myapp.dao.HistoryDAO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

public class WatchController {

    @FXML
    private StackPane rootStack;
    @FXML
    private BorderPane phoneFrame;
    @FXML
    private VBox playerWrapper;
    @FXML
    private StackPane videoContainer;
    @FXML
    private MediaView mediaView;
    @FXML
    private WebView webView;
    @FXML
    private BorderPane controlsPane;
    @FXML
    private VBox overlayGradient;
    @FXML
    private ProgressIndicator loadingSpinner;
    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private Button centerPlayBtn;
    @FXML
    private SVGPath iconPlayPause;
    @FXML
    private Slider progressSlider;
    @FXML
    private Slider volumeSlider;
    @FXML
    private SVGPath iconVolume;
    @FXML
    private Label lblVolume;
    @FXML
    private Label lblDuration;
    @FXML
    private VBox settingsMenu;

    @FXML
    private Label lblTitle, lblYear, lblCountry, lblTime, lblDesc;
    @FXML
    private HBox episodeContainer, bottomPosterContainer;

    private MediaPlayer mediaPlayer;
    private WebEngine webEngine;
    private final MovieService movieService = new MovieService();

    private PauseTransition hideControlsTimer;
    private boolean isSliding = false;
    private Rectangle roundedClip;

    private final String PATH_PLAY = "M8 5v14l11-7z";
    private final String PATH_PAUSE = "M6 19h4V5H6v14zm8-14v14h4V5h-4z";
    private final String PATH_VOL_ON = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z";
    private final String PATH_VOL_OFF = "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z";
    private Movie movie;

    // ===== Watch progress (real-time) =====
    private int currentEpisodeIndex = 1;
    private int resumeEpisodeIndex = 1;
    private int resumePositionSeconds = 0;
    private Timeline progressSaver;
    private int lastSavedSecond = -1;

    public void initData(Movie movie) {
        this.movie = movie;
        loadResumeProgress(movie);

        recordHistory(movie);
        if (mediaView != null && videoContainer != null) {
            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
            mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
        }

        if (webView != null) {
            webEngine = webView.getEngine();
            webView.setVisible(false);
            webView.setManaged(false);
        }

        if (phoneFrame != null) {
            roundedClip = new Rectangle();
            roundedClip.widthProperty().bind(phoneFrame.widthProperty());
            roundedClip.heightProperty().bind(phoneFrame.heightProperty());
            roundedClip.setArcWidth(35);
            roundedClip.setArcHeight(35);
            phoneFrame.setClip(roundedClip);
        }

        resetToPhoneMode();
        if (phoneFrame != null) phoneFrame.setTranslateY(0);

        if (controlsPane != null) {
            controlsPane.setVisible(true);
            controlsPane.setOpacity(1);
        }
        if (overlayGradient != null) overlayGradient.setVisible(true);
        if (loadingSpinner != null) loadingSpinner.setVisible(true);
        if (settingsMenu != null) settingsMenu.setVisible(false);

        if (lblTitle != null) lblTitle.setText(movie != null ? safe(movie.getName()) : "");
        if (lblDesc != null) lblDesc.setText("Đang tải...");
        if (lblYear != null)
            lblYear.setText(movie != null && movie.getYear() != null ? String.valueOf(movie.getYear()) : "");

        setupControls();
        loadMovieData(movie);
        loadBottomPosters();
    }

    private void resetToPhoneMode() {
        if (phoneFrame == null || videoContainer == null) return;

        phoneFrame.setScaleX(1.0);
        phoneFrame.setScaleY(1.0);

        phoneFrame.setMinWidth(375);
        phoneFrame.setPrefWidth(375);
        phoneFrame.setMaxWidth(375);

        phoneFrame.setMinHeight(812);
        phoneFrame.setPrefHeight(812);
        phoneFrame.setMaxHeight(812);

        videoContainer.prefHeightProperty().unbind();
        videoContainer.minHeightProperty().unbind();
        videoContainer.maxHeightProperty().unbind();
        videoContainer.prefWidthProperty().unbind();
        videoContainer.minWidthProperty().unbind();
        videoContainer.maxWidthProperty().unbind();

        videoContainer.setMinHeight(240);
        videoContainer.setPrefHeight(240);
        videoContainer.setMaxHeight(240);

        videoContainer.setMinWidth(375);
        videoContainer.setPrefWidth(375);
        videoContainer.setMaxWidth(375);

        if (playerWrapper != null) {
            VBox.setVgrow(videoContainer, Priority.NEVER);
        }

        if (mainScrollPane != null) {
            mainScrollPane.setVisible(true);
            mainScrollPane.setManaged(true);
        }

        if (phoneFrame != null) {
            phoneFrame.setClip(roundedClip);
        }
    }

    @FXML
    void toggleFullscreen() {
        if (videoContainer == null) return;
        Stage stage = (Stage) videoContainer.getScene().getWindow();
        if (stage == null) return;

        if (!stage.isFullScreen()) {
            stage.setFullScreen(true);

            if (phoneFrame != null) {
                phoneFrame.setScaleX(1.0);
                phoneFrame.setScaleY(1.0);

                phoneFrame.setMaxWidth(Double.MAX_VALUE);
                phoneFrame.setMaxHeight(Double.MAX_VALUE);
                phoneFrame.setPrefWidth(Region.USE_COMPUTED_SIZE);
                phoneFrame.setPrefHeight(Region.USE_COMPUTED_SIZE);
                phoneFrame.setClip(null);
            }

            if (mainScrollPane != null) {
                mainScrollPane.setVisible(false);
                mainScrollPane.setManaged(false);
            }

            videoContainer.setMaxWidth(Double.MAX_VALUE);
            videoContainer.setMaxHeight(Double.MAX_VALUE);

            if (rootStack != null) {
                videoContainer.prefHeightProperty().bind(rootStack.heightProperty());
                videoContainer.minHeightProperty().bind(rootStack.heightProperty());
            }

            if (playerWrapper != null) {
                VBox.setVgrow(videoContainer, Priority.ALWAYS);
            }

        } else {
            stage.setFullScreen(false);
            resetToPhoneMode();
        }
    }

    private void loadMovieData(Movie movie) {
        if (movie == null || movie.getSlug() == null || movie.getSlug().isBlank()) {
            if (loadingSpinner != null) loadingSpinner.setVisible(false);
            return;
        }

        UiUtils.runAsync(() -> {
            try {
                return movieService.getMovieDetails(movie.getSlug());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, result -> {
            if (loadingSpinner != null) loadingSpinner.setVisible(false);

            MovieResponse response = (MovieResponse) result;
            if (response == null || !response.isSuccess() || response.getMovie() == null) {
                showFallbackInfo(movie);
                return;
            }

            Movie detail = response.getMovie();
            bindMovieInfo(detail, movie);

            List<Episode.ServerData> eps = extractEpisodes(detail);

            int selectedIdx = 0;
            if (eps != null && !eps.isEmpty()) {
                int epIdx = Math.max(1, Math.min(resumeEpisodeIndex, eps.size()));
                selectedIdx = epIdx - 1;
            }
            renderEpisodes(eps, selectedIdx);

            if (eps != null && !eps.isEmpty()) {
                int epIdx = selectedIdx + 1;
                playEpisode(eps.get(selectedIdx), epIdx, resumePositionSeconds);
            }
        });
    }

    private void bindMovieInfo(Movie detail, Movie fallback) {
        if (detail == null && fallback == null) return;

        Movie src = (detail != null) ? detail : fallback;

        if (lblTitle != null) lblTitle.setText(safe(src.getName()));
        if (lblYear != null) lblYear.setText(src.getYear() == null ? "" : String.valueOf(src.getYear()));
        if (lblTime != null) lblTime.setText(safe(readMovieTime(src)));
        if (lblCountry != null) lblCountry.setText(extractCountryText(src));
        if (lblDesc != null) lblDesc.setText(stripHtml(readMovieContent(src)));
    }

    private void showFallbackInfo(Movie movie) {
        if (movie == null) return;
        if (lblTitle != null) lblTitle.setText(safe(movie.getName()));
        if (lblYear != null) lblYear.setText(movie.getYear() == null ? "" : String.valueOf(movie.getYear()));
        if (lblTime != null) lblTime.setText("");
        if (lblCountry != null) lblCountry.setText("Quốc tế");
        if (lblDesc != null) lblDesc.setText("Không tải được chi tiết phim. Vui lòng thử lại.");
    }

    @SuppressWarnings("unchecked")
    private List<Episode.ServerData> extractEpisodes(Movie detail) {
        if (detail == null) return new ArrayList<>();

        try {
            Object episodesObj = detail.getClass().getMethod("getEpisodes").invoke(detail);
            if (episodesObj instanceof List<?> groups) {
                for (Object groupObj : groups) {
                    if (groupObj instanceof Episode group && group.getServerData() != null && !group.getServerData().isEmpty()) {
                        return group.getServerData();
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return new ArrayList<>();
    }

    private void playEpisode(Episode.ServerData ep) {
        playEpisode(ep, 1, 0);
    }

    private void playEpisode(Episode.ServerData ep, int episodeIndex, int startSeconds) {
        if (ep == null) return;

        // update current ep
        this.currentEpisodeIndex = Math.max(1, episodeIndex);

        cleanup(); // sẽ persist trước khi stop (ở bước H)

        String m3u8 = safe(ep.getLinkM3u8());
        String embed = safe(ep.getLinkEmbed());

        // reset saved-second tracking for new episode
        lastSavedSecond = -1;

        if (!m3u8.isBlank()) {
            try {
                if (webView != null) {
                    webView.setVisible(false);
                    webView.setManaged(false);
                }
                if (mediaView != null) mediaView.setVisible(true);

                Media media = new Media(m3u8);
                mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);

                final int seekSeconds = Math.max(0, startSeconds);

                mediaPlayer.setOnReady(() -> {
                    // seek to saved progress (if any)
                    if (seekSeconds > 0) {
                        try {
                            mediaPlayer.seek(Duration.seconds(seekSeconds));
                        } catch (Exception ignored) {}
                    }

                    mediaPlayer.play();
                    startProgressSaver(); // <-- realtime save

                    if (centerPlayBtn != null) centerPlayBtn.setVisible(false);
                    if (iconPlayPause != null) iconPlayPause.setContent(PATH_PAUSE);

                    double v = volumeSlider != null ? volumeSlider.getValue() : 1.0;
                    mediaPlayer.setVolume(v);

                    if (hideControlsTimer != null) hideControlsTimer.playFromStart();
                });

                mediaPlayer.currentTimeProperty().addListener((o, old, now) -> {
                    if (mediaPlayer == null || isSliding) return;

                    Duration total = mediaPlayer.getTotalDuration();
                    if (total == null || total.isUnknown() || total.toMillis() <= 0) return;

                    if (progressSlider != null) {
                        progressSlider.setValue(now.toSeconds() / total.toSeconds() * 100.0);
                    }
                    if (lblDuration != null) {
                        lblDuration.setText(formatTime(now) + " / " + formatTime(total));
                    }
                });

                mediaPlayer.setOnEndOfMedia(() -> {
                    persistProgress(true); // save final
                    stopProgressSaver();

                    if (iconPlayPause != null) iconPlayPause.setContent(PATH_PLAY);
                    if (centerPlayBtn != null) centerPlayBtn.setVisible(true);
                });

                return;
            } catch (Exception e) {
                e.printStackTrace();
                // fallback qua embed
            }
        }

        // embed: không lấy được currentTime => chỉ lưu 0:00
        stopProgressSaver();

        if (!embed.isBlank() && webEngine != null) {
            if (mediaView != null) mediaView.setVisible(false);
            webView.setVisible(true);
            webView.setManaged(true);
            webEngine.load(embed);

            if (centerPlayBtn != null) centerPlayBtn.setVisible(true);
            if (iconPlayPause != null) iconPlayPause.setContent(PATH_PLAY);

            persistProgress(true);
        }
    }

    private void renderEpisodes(List<Episode.ServerData> episodes, int selectedIndex) {
        if (episodeContainer == null) return;

        episodeContainer.getChildren().clear();

        if (episodes == null || episodes.isEmpty()) {
            Label empty = new Label("Chưa có danh sách tập");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.7);");
            episodeContainer.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < episodes.size(); i++) {
            Episode.ServerData ep = episodes.get(i);
            String label = safe(ep.getName());
            if (label.isBlank()) label = "Tập " + (i + 1);

            final int epIndex = i + 1;

            Button btn = new Button(label);
            btn.getStyleClass().add("episode-square-btn");

            btn.setOnAction(e -> {
                playEpisode(ep, epIndex, 0);

                episodeContainer.getChildren().forEach(n -> n.getStyleClass().remove("episode-active"));
                btn.getStyleClass().add("episode-active");
            });

            if (i == selectedIndex) btn.getStyleClass().add("episode-active");
            episodeContainer.getChildren().add(btn);
        }
    }

    private void loadBottomPosters() {
        UiUtils.runAsync(movieService::getNewMovies, result -> {
            @SuppressWarnings("unchecked")
            List<Movie> movies = (List<Movie>) result;

            if (movies == null || bottomPosterContainer == null) return;

            bottomPosterContainer.getChildren().clear();

            List<Movie> copy = new ArrayList<>(movies);
            Collections.shuffle(copy);

            int limit = Math.min(10, copy.size());
            for (int i = 0; i < limit; i++) {
                bottomPosterContainer.getChildren().add(createPosterCard(copy.get(i)));
            }
        });
    }

    private StackPane createPosterCard(Movie movie) {
        StackPane card = new StackPane();
        card.getStyleClass().add("poster-item");
        card.setPrefSize(100, 150);

        ImageView img = new ImageView();
        img.setFitWidth(100);
        img.setFitHeight(150);
        img.setPreserveRatio(true);

        try {
            String url = movie != null ? safeThumb(movie) : "";
            if (!url.isBlank()) img.setImage(new Image(url, true));
        } catch (Exception ignored) {
        }

        Rectangle clip = new Rectangle(100, 150);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        img.setClip(clip);

        card.getChildren().add(img);

        card.setOnMouseClicked(e -> {
            cleanup();
            SceneNavigator.loadWatchScene(movie);
        });

        return card;
    }

    private void setupControls() {
        if (iconPlayPause != null) iconPlayPause.setContent(PATH_PLAY);
        if (iconVolume != null) iconVolume.setContent(PATH_VOL_ON);

        if (lblVolume != null && volumeSlider != null) {
            int percent = (int) (volumeSlider.getValue() * 100);
            lblVolume.setText(percent + "%");
        }

        hideControlsTimer = new PauseTransition(Duration.seconds(3));
        hideControlsTimer.setOnFinished(e -> {
            if (mediaPlayer != null
                    && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING
                    && (settingsMenu == null || !settingsMenu.isVisible())) {
                if (controlsPane != null) controlsPane.setVisible(false);
                if (videoContainer != null) videoContainer.setCursor(Cursor.NONE);
            }
        });

        if (volumeSlider != null) {
            volumeSlider.valueProperty().addListener((obs, old, val) -> {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(val.doubleValue());
                }
                if (iconVolume != null) {
                    iconVolume.setContent(val.doubleValue() == 0 ? PATH_VOL_OFF : PATH_VOL_ON);
                }
                if (lblVolume != null) {
                    lblVolume.setText((int) (val.doubleValue() * 100) + "%");
                }
            });
        }

        if (progressSlider != null) {
            progressSlider.setOnMousePressed(e -> isSliding = true);
            progressSlider.setOnMouseReleased(e -> {
                if (mediaPlayer != null && mediaPlayer.getTotalDuration() != null) {
                    mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(progressSlider.getValue() / 100.0));
                }
                isSliding = false;
                persistProgress(true);
            });
        }
    }

    @FXML
    void onUserInteraction(MouseEvent e) {
        if (controlsPane != null) {
            controlsPane.setVisible(true);
            controlsPane.setOpacity(1.0);
        }
        if (overlayGradient != null) overlayGradient.setVisible(true);
        if (videoContainer != null) videoContainer.setCursor(Cursor.DEFAULT);

        if (hideControlsTimer != null) hideControlsTimer.playFromStart();
    }

    @FXML
    void onVideoClick(MouseEvent e) {
        if (settingsMenu == null || !settingsMenu.isVisible()) {
            togglePlay();
        }
        onUserInteraction(null);
    }

    @FXML
    void togglePlay() {
        if (mediaPlayer == null) return;

        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            persistProgress(true);
            stopProgressSaver();
            if (iconPlayPause != null) iconPlayPause.setContent(PATH_PLAY);
            if (centerPlayBtn != null) centerPlayBtn.setVisible(true);
            onUserInteraction(null);
            if (hideControlsTimer != null) hideControlsTimer.stop();
        } else {
            mediaPlayer.play();
            startProgressSaver();
            if (iconPlayPause != null) iconPlayPause.setContent(PATH_PAUSE);
            if (centerPlayBtn != null) centerPlayBtn.setVisible(false);
            if (hideControlsTimer != null) hideControlsTimer.playFromStart();
        }
    }

    @FXML
    void toggleMute() {
        if (mediaPlayer == null || volumeSlider == null) return;

        if (mediaPlayer.getVolume() > 0) {
            mediaPlayer.setVolume(0);
            volumeSlider.setValue(0);
        } else {
            mediaPlayer.setVolume(1);
            volumeSlider.setValue(1);
        }
    }

    @FXML
    void toggleSettingsMenu() {
        if (settingsMenu == null) return;
        settingsMenu.setVisible(!settingsMenu.isVisible());
        onUserInteraction(null);
        if (hideControlsTimer != null) hideControlsTimer.stop();
    }

    @FXML
    void setSpeed05() {
        setSpeed(0.5);
    }

    @FXML
    void setSpeed10() {
        setSpeed(1.0);
    }

    @FXML
    void setSpeed125() {
        setSpeed(1.25);
    }

    @FXML
    void setSpeed15() {
        setSpeed(1.5);
    }

    @FXML
    void setSpeed20() {
        setSpeed(2.0);
    }

    private void setSpeed(double speed) {
        if (mediaPlayer != null) mediaPlayer.setRate(speed);
        if (settingsMenu != null) settingsMenu.setVisible(false);
        if (hideControlsTimer != null) hideControlsTimer.playFromStart();
    }

    @FXML
    void animateAndClose() {
        if (mediaPlayer != null) mediaPlayer.pause();

        TranslateTransition tt = new TranslateTransition(Duration.millis(300), phoneFrame);
        tt.setToY(1000);
        tt.setOnFinished(e -> {
            cleanup();
            SceneNavigator.loadHome();
        });
        tt.play();
    }

    private void cleanup() {
        persistProgress(true);
        stopProgressSaver();
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
        } catch (Exception ignored) {
        }

        try {
            if (webEngine != null) {
                webEngine.load(null);
            }
        } catch (Exception ignored) {
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String stripHtml(String s) {
        if (s == null) return "";
        return s.replaceAll("\\<.*?\\>", "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .trim();
    }

    private String formatTime(Duration d) {
        if (d == null || d.isUnknown()) return "00:00";
        int total = (int) Math.max(0, d.toSeconds());
        int m = total / 60;
        int s = total % 60;
        return String.format(Locale.ROOT, "%02d:%02d", m, s);
    }

    private String safeThumb(Movie movie) {
        if (movie == null) return "";
        try {
            return safe((String) movie.getClass().getMethod("getFullThumbUrl").invoke(movie));
        } catch (Exception ignored) {
        }

        try {
            String thumb = movie.getThumbUrl();
            if (thumb == null || thumb.isBlank()) return "";
            if (thumb.startsWith("http")) return thumb;
            return "https://img.ophim.cc/uploads/movies/" + thumb.replaceFirst("^/+", "");
        } catch (Exception ignored) {
            return "";
        }
    }

    private String readMovieTime(Movie movie) {
        try {
            Object v = movie.getClass().getMethod("getTime").invoke(movie);
            return v == null ? "" : String.valueOf(v);
        } catch (Exception ignored) {
            return "";
        }
    }

    private String readMovieContent(Movie movie) {
        try {
            Object v = movie.getClass().getMethod("getContent").invoke(movie);
            return v == null ? "" : String.valueOf(v);
        } catch (Exception ignored) {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    private String extractCountryText(Movie movie) {
        if (movie == null) return "Quốc tế";

        try {
            Object countriesObj = movie.getClass().getMethod("getCountry").invoke(movie);
            if (countriesObj instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                try {
                    Object name = first.getClass().getMethod("getName").invoke(first);
                    if (name != null && !String.valueOf(name).isBlank()) {
                        return String.valueOf(name);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        return "Quốc tế";
    }

    private void recordHistory(Movie movie) {
        if (movie == null) return;
        User user = SessionManager.getUser();

        if (user != null) {
            HistoryDAO historyDAO = new HistoryDAO();
            new Thread(() -> {
                try {
                    String slug = (movie.getSlug() != null) ? movie.getSlug() : "";
                    String name = (movie.getName() != null) ? movie.getName() : "Phim không tên";
                    String thumb = (movie.getThumbUrl() != null) ? movie.getThumbUrl() : "";

                    historyDAO.saveOrUpdate(user.getId(), slug, name, thumb);
                    System.out.println("✅ Đã lưu vào lịch sử: " + name);
                } catch (Exception e) {
                    System.err.println("❌ Lỗi khi lưu lịch sử: " + e.getMessage());
                }
            }).start();
        } else {
            System.out.println("ℹ️ Người dùng chưa đăng nhập, không lưu lịch sử.");
        }

    }

    private void loadResumeProgress(Movie movie) {
        User user = SessionManager.getUser();
        if (user == null || movie == null || movie.getSlug() == null || movie.getSlug().isBlank()) return;

        try {
            HistoryDAO.ProgressInfo p = new HistoryDAO().getProgress(user.getId(), movie.getSlug());
            if (p != null) {
                resumeEpisodeIndex = Math.max(1, p.episodeIndex);
                resumePositionSeconds = Math.max(0, p.positionSeconds);
            }
        } catch (Exception ignored) {}
    }

    private void startProgressSaver() {
        stopProgressSaver();
        if (mediaPlayer == null) return;

        progressSaver = new Timeline(new KeyFrame(Duration.seconds(3), e -> persistProgress(false)));
        progressSaver.setCycleCount(Timeline.INDEFINITE);
        progressSaver.play();
    }

    private void stopProgressSaver() {
        if (progressSaver != null) {
            try { progressSaver.stop(); } catch (Exception ignored) {}
            progressSaver = null;
        }
    }

    private void persistProgress(boolean force) {
        User user = SessionManager.getUser();
        if (user == null || movie == null || movie.getSlug() == null) return;

        int pos = 0, dur = 0;

        try {
            if (mediaPlayer != null) {
                Duration now = mediaPlayer.getCurrentTime();
                Duration total = mediaPlayer.getTotalDuration();

                pos = (now == null) ? 0 : (int) Math.floor(now.toSeconds());
                if (total != null && !total.isUnknown()) {
                    dur = (int) Math.floor(total.toSeconds());
                }
            }
        } catch (Exception ignored) {}

        if (!force && lastSavedSecond >= 0 && Math.abs(pos - lastSavedSecond) < 2) return;
        lastSavedSecond = pos;

        final int fPos = Math.max(0, pos);
        final int fDur = Math.max(0, dur);
        final int epIndex = Math.max(1, currentEpisodeIndex);

        new Thread(() -> {
            try {
                String slug = movie.getSlug();
                String name = (movie.getName() != null) ? movie.getName() : "Phim không tên";
                String thumb = (movie.getThumbUrl() != null) ? movie.getThumbUrl() : "";

                new HistoryDAO().saveOrUpdate(user.getId(), slug, name, thumb, epIndex, fPos, fDur);
            } catch (Exception ignored) {}
        }).start();
    }
}