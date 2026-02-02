package com.myapp.controller;

import com.myapp.model.Episode;
import com.myapp.model.Movie;
import com.myapp.model.MovieResponse;
import com.myapp.service.MovieService;
import com.myapp.util.SceneNavigator;
import com.myapp.util.UiUtils;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.Collections;
import java.util.List;

public class WatchController {

    @FXML private StackPane rootStack;
    @FXML private BorderPane phoneFrame;
    @FXML private VBox playerWrapper;
    @FXML private StackPane videoContainer;
    @FXML private MediaView mediaView;
    @FXML private WebView webView;
    @FXML private BorderPane controlsPane;
    @FXML private VBox overlayGradient;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private ScrollPane mainScrollPane;

    // Controls
    @FXML private Button centerPlayBtn;
    @FXML private SVGPath iconPlayPause;
    @FXML private Slider progressSlider;
    @FXML private Slider volumeSlider;
    @FXML private SVGPath iconVolume;
    @FXML private Label lblVolume;
    @FXML private Label lblDuration;
    @FXML private VBox settingsMenu;

    // Info
    @FXML private Label lblTitle, lblYear, lblCountry, lblTime, lblDesc;
    @FXML private HBox episodeContainer, bottomPosterContainer;

    private MediaPlayer mediaPlayer;
    private WebEngine webEngine;
    private final MovieService movieService = new MovieService();
    private final ObjectMapper mapper = new ObjectMapper();
    private PauseTransition hideControlsTimer;
    private boolean isSliding = false;
    private Rectangle roundedClip;

    private final String PATH_PLAY = "M8 5v14l11-7z";
    private final String PATH_PAUSE = "M6 19h4V5H6v14zm8-14v14h4V5h-4z";
    private final String PATH_VOL_ON = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z";
    private final String PATH_VOL_OFF = "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z";

    public void initData(Movie movie) {
        // 1. SETUP MEDIA VIEW (Fix Zoom)
        if (mediaView != null) {
            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
            mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
        }

        // 2. SETUP CLIP (Bo góc điện thoại)
        roundedClip = new Rectangle();
        roundedClip.widthProperty().bind(phoneFrame.widthProperty());
        roundedClip.heightProperty().bind(phoneFrame.heightProperty());
        roundedClip.setArcWidth(35); roundedClip.setArcHeight(35);
        phoneFrame.setClip(roundedClip);

        // 3. RESET VỀ CHUẨN ĐIỆN THOẠI (KHÔNG DÙNG AUTO SCALE)
        resetToPhoneMode();
        phoneFrame.setTranslateY(0);

        controlsPane.setVisible(true);
        controlsPane.setOpacity(1);
        overlayGradient.setVisible(true);
        loadingSpinner.setVisible(true);
        settingsMenu.setVisible(false);

        lblTitle.setText(movie.getName());
        lblDesc.setText("Đang tải...");

        setupControls();
        loadMovieData(movie);
        loadBottomPosters();
    }

    // --- HÀM RESET VỀ CHẾ ĐỘ ĐIỆN THOẠI CHUẨN (375x812) ---
    private void resetToPhoneMode() {
        // 1. Đảm bảo Scale luôn là 1.0 (Kích thước thật)
        phoneFrame.setScaleX(1.0);
        phoneFrame.setScaleY(1.0);

        // 2. Ép cứng kích thước khung điện thoại
        // Set Min/Max/Pref giống hệt nhau để cố định
        phoneFrame.setMinWidth(375);  phoneFrame.setMaxWidth(375);  phoneFrame.setPrefWidth(375);
        phoneFrame.setMinHeight(812); phoneFrame.setMaxHeight(812); phoneFrame.setPrefHeight(812);

        // 3. Reset Video Container về 240px
        videoContainer.prefHeightProperty().unbind();
        videoContainer.minHeightProperty().unbind();
        videoContainer.maxHeightProperty().unbind();
        videoContainer.prefWidthProperty().unbind();
        videoContainer.maxWidthProperty().unbind();

        videoContainer.setMinHeight(240);
        videoContainer.setPrefHeight(240);
        videoContainer.setMaxHeight(240);

        // Fix lỗi mất nút full screen: Ép chiều ngang video về 375px
        videoContainer.setMinWidth(375);
        videoContainer.setPrefWidth(375);
        videoContainer.setMaxWidth(375);

        // 4. Ngăn VBox kéo giãn
        if (playerWrapper != null) {
            VBox.setVgrow(videoContainer, Priority.NEVER);
        }

        // 5. UI
        mainScrollPane.setVisible(true);
        mainScrollPane.setManaged(true);
        phoneFrame.setClip(roundedClip);
    }

    // --- TOGGLE FULLSCREEN ---
    @FXML
    void toggleFullscreen() {
        Stage stage = (Stage) videoContainer.getScene().getWindow();

        if (!stage.isFullScreen()) {
            // === VÀO CHẾ ĐỘ FULLSCREEN (Màn hình ngang) ===
            stage.setFullScreen(true);

            // 1. Reset Scale (Quan trọng nếu trước đó có scale)
            phoneFrame.setScaleX(1.0);
            phoneFrame.setScaleY(1.0);

            // 2. Tháo giới hạn khung điện thoại
            phoneFrame.setMaxWidth(Double.MAX_VALUE);
            phoneFrame.setMaxHeight(Double.MAX_VALUE);
            phoneFrame.setPrefWidth(Region.USE_COMPUTED_SIZE);
            phoneFrame.setPrefHeight(Region.USE_COMPUTED_SIZE);
            phoneFrame.setClip(null); // Bỏ bo góc

            // 3. Ẩn nội dung bên dưới
            mainScrollPane.setVisible(false);
            mainScrollPane.setManaged(false);

            // 4. Cho phép Video giãn ra tối đa
            videoContainer.setMaxWidth(Double.MAX_VALUE);
            videoContainer.setMaxHeight(Double.MAX_VALUE);

            // Bind vào kích thước màn hình
            videoContainer.prefHeightProperty().bind(rootStack.heightProperty());
            videoContainer.minHeightProperty().bind(rootStack.heightProperty());

            if (playerWrapper != null) {
                VBox.setVgrow(videoContainer, Priority.ALWAYS);
            }

        } else {
            // === THOÁT CHẾ ĐỘ FULLSCREEN (Về điện thoại dọc) ===
            stage.setFullScreen(false);
            resetToPhoneMode(); // Quay về kích thước chuẩn
        }
    }

    // --- CÁC HÀM XỬ LÝ KHÁC (GIỮ NGUYÊN) ---
    private StackPane createPosterCard(Movie movie) {
        StackPane card = new StackPane();
        card.getStyleClass().add("poster-item");
        card.setPrefSize(100, 150);
        ImageView img = new ImageView();
        img.setFitWidth(100); img.setFitHeight(150);
        try { img.setImage(new Image(movie.getFullThumbUrl())); } catch(Exception e){}
        Rectangle clip = new Rectangle(100, 150); clip.setArcWidth(10); clip.setArcHeight(10); img.setClip(clip);
        card.getChildren().add(img);

        card.setOnMouseClicked(e -> {
            cleanup();
            SceneNavigator.loadWatchScene(movie);
        });

        return card;
    }

    private void cleanup() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        if (webEngine != null) {
            webEngine.load(null);
        }
    }

    private void setupControls() {
        hideControlsTimer = new PauseTransition(Duration.seconds(3));
        hideControlsTimer.setOnFinished(e -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING && !settingsMenu.isVisible()) {
                controlsPane.setVisible(false);
                videoContainer.setCursor(javafx.scene.Cursor.NONE);
            }
        });
        volumeSlider.valueProperty().addListener((obs, old, val) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(val.doubleValue());
                iconVolume.setContent(val.doubleValue() == 0 ? PATH_VOL_OFF : PATH_VOL_ON);
                lblVolume.setText((int)(val.doubleValue() * 100) + "%");
            }
        });
        progressSlider.setOnMousePressed(e -> isSliding = true);
        progressSlider.setOnMouseReleased(e -> {
            if(mediaPlayer!=null) mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(progressSlider.getValue()/100.0));
            isSliding = false;
        });
    }

    @FXML void onUserInteraction(MouseEvent e) {
        controlsPane.setVisible(true);
        controlsPane.setOpacity(1.0);
        overlayGradient.setVisible(true);
        videoContainer.setCursor(javafx.scene.Cursor.DEFAULT);
        hideControlsTimer.playFromStart();
    }

    private void loadMovieData(Movie movie) {
        UiUtils.runAsync(() -> {
            try {
                String json = new OkHttpClient().newCall(
                        new Request.Builder().url("https://ophim1.com/phim/" + movie.getSlug()).build()
                ).execute().body().string();
                return mapper.readValue(json, MovieResponse.class);
            } catch (Exception e) { return null; }
        }, response -> {
            loadingSpinner.setVisible(false);
            if (response != null && response.getMovie() != null) {
                lblTitle.setText(response.getMovie().getName());
                lblYear.setText(String.valueOf(response.getMovie().getYear()));
                lblTime.setText(response.getMovie().getTime());
                String country = (response.getMovie().getCountry() != null && !response.getMovie().getCountry().isEmpty())
                        ? response.getMovie().getCountry().get(0).getName() : "Quốc tế";
                lblCountry.setText(country);
                lblDesc.setText(response.getMovie().getContent().replaceAll("\\<.*?\\>", ""));

                if (response.getEpisodes() != null && !response.getEpisodes().isEmpty()) {
                    List<Episode.ServerData> eps = response.getEpisodes().get(0).getServerData();
                    renderEpisodes(eps);
                    if (!eps.isEmpty()) playEpisode(eps.get(0));
                }
            }
        });
    }

    private void playEpisode(Episode.ServerData ep) {
        cleanup();
        try {
            Media media = new Media(ep.getLinkM3u8());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setOnReady(() -> {
                mediaPlayer.play();
                centerPlayBtn.setVisible(false);
                iconPlayPause.setContent(PATH_PAUSE);
                mediaPlayer.setVolume(volumeSlider.getValue());
                hideControlsTimer.playFromStart();
            });
            mediaPlayer.currentTimeProperty().addListener((o,old,now) -> {
                if(!isSliding) {
                    progressSlider.setValue(now.toSeconds() / mediaPlayer.getTotalDuration().toSeconds() * 100);
                    lblDuration.setText(formatTime(now) + " / " + formatTime(mediaPlayer.getTotalDuration()));
                }
            });
        } catch(Exception e) {}
    }

    private void renderEpisodes(List<Episode.ServerData> episodes) {
        episodeContainer.getChildren().clear();
        for(int i=0; i<episodes.size(); i++) {
            Episode.ServerData ep = episodes.get(i);
            Button btn = new Button(ep.getName());
            btn.getStyleClass().add("episode-square-btn");
            btn.setOnAction(e -> {
                playEpisode(ep);
                episodeContainer.getChildren().forEach(n -> n.getStyleClass().remove("episode-active"));
                btn.getStyleClass().add("episode-active");
            });
            if(i==0) btn.getStyleClass().add("episode-active");
            episodeContainer.getChildren().add(btn);
        }
    }

    private void loadBottomPosters() {
        UiUtils.runAsync(movieService::getNewMovies, result -> {
            List<Movie> movies = (List<Movie>) result;
            if(movies!=null) {
                bottomPosterContainer.getChildren().clear();
                Collections.shuffle(movies);
                for(int i=0; i<Math.min(10, movies.size()); i++) {
                    bottomPosterContainer.getChildren().add(createPosterCard(movies.get(i)));
                }
            }
        });
    }

    @FXML void onVideoClick(MouseEvent e) { if(!settingsMenu.isVisible()) togglePlay(); onUserInteraction(null); }
    @FXML void togglePlay() {
        if(mediaPlayer==null) return;
        if(mediaPlayer.getStatus()==MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause(); iconPlayPause.setContent(PATH_PLAY); centerPlayBtn.setVisible(true); onUserInteraction(null); hideControlsTimer.stop();
        } else {
            mediaPlayer.play(); iconPlayPause.setContent(PATH_PAUSE); centerPlayBtn.setVisible(false); hideControlsTimer.playFromStart();
        }
    }
    @FXML void toggleMute() {
        if(mediaPlayer != null) {
            if(mediaPlayer.getVolume() > 0) { mediaPlayer.setVolume(0); volumeSlider.setValue(0); }
            else { mediaPlayer.setVolume(1); volumeSlider.setValue(1); }
        }
    }
    @FXML void toggleSettingsMenu() { settingsMenu.setVisible(!settingsMenu.isVisible()); onUserInteraction(null); hideControlsTimer.stop(); }
    @FXML void setSpeed05() { setSpeed(0.5); } @FXML void setSpeed10() { setSpeed(1.0); } @FXML void setSpeed125() { setSpeed(1.25); }
    @FXML void setSpeed15() { setSpeed(1.5); } @FXML void setSpeed20() { setSpeed(2.0); }
    private void setSpeed(double s) { if(mediaPlayer!=null) mediaPlayer.setRate(s); settingsMenu.setVisible(false); hideControlsTimer.playFromStart(); }
    private String formatTime(Duration d) { if(d==null) return "00:00"; int s=(int)d.toSeconds(); return String.format("%02d:%02d", s/60, s%60); }
    @FXML void animateAndClose() {
        if(mediaPlayer != null) mediaPlayer.pause();
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), phoneFrame);
        tt.setToY(1000);
        tt.setOnFinished(e -> { cleanup(); SceneNavigator.loadHome(); });
        tt.play();
    }
}