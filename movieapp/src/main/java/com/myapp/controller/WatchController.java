package com.myapp.controller;

import com.myapp.model.Episode;
import com.myapp.model.Movie;
import com.myapp.model.MovieResponse;
import com.myapp.service.MovieService;
import com.myapp.util.SceneNavigator;
import com.myapp.util.UiUtils;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

public class WatchController {

    @FXML private MediaView mediaView;
    @FXML private StackPane videoContainer;
    @FXML private ScrollPane mainScrollPane;
    @FXML private FlowPane episodeContainer;

    @FXML private BorderPane controlsPane;
    @FXML private Button btnPlay;
    @FXML private Button centerPlayBtn;
    @FXML private Slider progressSlider;

    // Popup Volume
    @FXML private VBox volumePopup;
    @FXML private Slider volumeSlider;
    @FXML private Label lblVolumeValue;

    @FXML private Label lblDuration;
    @FXML private SVGPath iconPlayPause;
    @FXML private Button btnSettings;
    @FXML private Button btnFullscreen;

    @FXML private VBox speedMenu;
    @FXML private Button btnSpeed10;

    @FXML private Label lblTitle;
    @FXML private Label lblOriginName;
    @FXML private Label lblYear;
    @FXML private Label lblTime;
    @FXML private Label lblCountry;
    @FXML private Label lblCategory;
    @FXML private Label lblDesc;

    private MediaPlayer mediaPlayer;
    private final MovieService movieService = new MovieService();
    private final ObjectMapper mapper = new ObjectMapper();

    private PauseTransition idleTimer;
    private boolean isSliding = false;

    private final String PATH_PLAY = "M8 5v14l11-7z";
    private final String PATH_PAUSE = "M6 19h4V5H6v14zm8-14v14h4V5h-4z";

    public void initData(Movie movie) {
        lblTitle.setText(movie.getName());
        lblYear.setText(String.valueOf(movie.getYear()));
        lblDesc.setText("Đang tải...");

        UiUtils.runAsync(() -> {
            try {
                String json = new okhttp3.OkHttpClient().newCall(
                        new okhttp3.Request.Builder().url("https://ophim1.com/phim/" + movie.getSlug()).build()
                ).execute().body().string();
                return mapper.readValue(json, MovieResponse.class);
            } catch (Exception e) { return null; }
        }, response -> {
            if (response != null) {
                Movie details = response.getMovie();
                lblTitle.setText(details.getName());
                lblOriginName.setText(details.getOriginName());
                lblYear.setText(String.valueOf(details.getYear()));
                lblTime.setText(details.getTime());
                if (details.getCountry() != null) lblCountry.setText(details.getCountry().stream().map(Movie.Category::getName).collect(Collectors.joining(", ")));
                if (details.getCategory() != null) lblCategory.setText(details.getCategory().stream().map(Movie.Category::getName).collect(Collectors.joining(", ")));
                if (details.getContent() != null) lblDesc.setText(details.getContent().replaceAll("\\<.*?\\>", ""));

                if (response.getEpisodes() != null && !response.getEpisodes().isEmpty()) {
                    List<Episode> eps = response.getEpisodes().get(0).getServer_data();
                    renderEpisodes(eps);
                    if (!eps.isEmpty()) playEpisode(eps.get(0));
                }
            }
        });

        if (mainScrollPane != null) {
            mediaView.fitWidthProperty().bind(mainScrollPane.widthProperty());
            mediaView.setPreserveRatio(true);
            mainScrollPane.setVvalue(0);
        }

        setupAutoHidingControls();

        lblVolumeValue.setText("100");
        volumeSlider.valueProperty().addListener((obs, old, val) -> {
            if (mediaPlayer != null) mediaPlayer.setVolume(val.doubleValue());
            lblVolumeValue.setText(String.format("%.0f", val.doubleValue() * 100));
        });

        progressSlider.setOnMousePressed(e -> isSliding = true);
        progressSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds((progressSlider.getValue() / 100) * mediaPlayer.getTotalDuration().toSeconds()));
            }
            isSliding = false;
        });

        if (btnSpeed10 != null) btnSpeed10.getStyleClass().add("speed-selected");
        if (btnSettings != null) btnSettings.setTooltip(new Tooltip("Tốc độ phát"));
        if (btnFullscreen != null) btnFullscreen.setTooltip(new Tooltip("Toàn màn hình"));
    }

    @FXML
    void toggleVolumePopup() {
        boolean isVisible = volumePopup.isVisible();
        volumePopup.setVisible(!isVisible);
        if (!isVisible) {
            idleTimer.playFromStart();
            speedMenu.setVisible(false);
        }
    }

    private void setupAutoHidingControls() {
        idleTimer = new PauseTransition(Duration.seconds(3));
        idleTimer.setOnFinished(e -> hideControls());
        centerPlayBtn.setVisible(false);
        controlsPane.setOpacity(1);
    }

    @FXML void onUserInteraction(MouseEvent event) {
        showControls();
        idleTimer.playFromStart();
    }
    @FXML void hideControlsImmediately() {}

    private void showControls() {
        if (controlsPane.getOpacity() < 1) {
            controlsPane.setVisible(true);
            FadeTransition fade = new FadeTransition(Duration.millis(200), controlsPane);
            fade.setToValue(1);
            fade.play();
            videoContainer.setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }

    private void hideControls() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            FadeTransition fade = new FadeTransition(Duration.millis(500), controlsPane);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                controlsPane.setVisible(false);
                speedMenu.setVisible(false);
                volumePopup.setVisible(false);
                videoContainer.setCursor(javafx.scene.Cursor.NONE);
            });
            fade.play();
        }
    }

    @FXML void togglePlay() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            if(iconPlayPause != null) iconPlayPause.setContent(PATH_PLAY);
            centerPlayBtn.setVisible(true);
            showControls();
            idleTimer.stop();
        } else {
            mediaPlayer.play();
            if(iconPlayPause != null) iconPlayPause.setContent(PATH_PAUSE);
            centerPlayBtn.setVisible(false);
            idleTimer.playFromStart();
        }
    }

    @FXML void toggleFullscreen() {
        Stage stage = (Stage) videoContainer.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML void toggleSpeedMenu() {
        boolean isVisible = speedMenu.isVisible();
        speedMenu.setVisible(!isVisible);
        if (!isVisible) {
            idleTimer.playFromStart();
            volumePopup.setVisible(false);
        }
    }

    @FXML void setSpeed05() { changeSpeed(0.5); }
    @FXML void setSpeed10() { changeSpeed(1.0); }
    @FXML void setSpeed125() { changeSpeed(1.25); }
    @FXML void setSpeed15() { changeSpeed(1.5); }
    @FXML void setSpeed20() { changeSpeed(2.0); }

    private void changeSpeed(double speed) {
        if (mediaPlayer != null) mediaPlayer.setRate(speed);
        speedMenu.setVisible(false);
        for (javafx.scene.Node node : speedMenu.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.getStyleClass().remove("speed-selected");
                String txt = btn.getText();
                if (speed == 1.0 && txt.contains("1.0x")) btn.getStyleClass().add("speed-selected");
                else if (speed == 0.5 && txt.contains("0.5x")) btn.getStyleClass().add("speed-selected");
                else if (speed == 1.25 && txt.contains("1.25x")) btn.getStyleClass().add("speed-selected");
                else if (speed == 1.5 && txt.contains("1.5x")) btn.getStyleClass().add("speed-selected");
                else if (speed == 2.0 && txt.contains("2.0x")) btn.getStyleClass().add("speed-selected");
            }
        }
        idleTimer.playFromStart();
    }

    private void playEpisode(Episode ep) {
        if (mediaPlayer != null) { mediaPlayer.stop(); mediaPlayer.dispose(); }
        try {
            Media media = new Media(ep.getLinkM3u8());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            mediaPlayer.currentTimeProperty().addListener((o, old, now) -> updateProgress());
            mediaPlayer.setOnReady(() -> {
                mediaPlayer.play();
                if(iconPlayPause != null) iconPlayPause.setContent(PATH_PAUSE);
                centerPlayBtn.setVisible(false);
                idleTimer.playFromStart();
                mediaPlayer.setVolume(volumeSlider.getValue());
                lblVolumeValue.setText(String.format("%.0f", volumeSlider.getValue() * 100));
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateProgress() {
        if (mediaPlayer == null || isSliding) return;
        Duration cur = mediaPlayer.getCurrentTime();
        Duration tot = mediaPlayer.getTotalDuration();
        if (tot.greaterThan(Duration.ZERO)) progressSlider.setValue((cur.toMillis() / tot.toMillis()) * 100);
        lblDuration.setText(formatTime(cur) + " / " + formatTime(tot));
    }

    private String formatTime(Duration d) {
        if (d == null) return "00:00";
        int s = (int) d.toSeconds();
        return String.format("%02d:%02d", s/60, s%60);
    }

    private void renderEpisodes(List<Episode> episodes) {
        episodeContainer.getChildren().clear();
        for (Episode ep : episodes) {
            Button btn = new Button(ep.getName());
            btn.getStyleClass().add("episode-grid-btn");
            btn.setOnAction(e -> {
                playEpisode(ep);
                episodeContainer.getChildren().forEach(n -> n.getStyleClass().remove("episode-active"));
                btn.getStyleClass().add("episode-active");
            });
            episodeContainer.getChildren().add(btn);
        }
        if(!episodeContainer.getChildren().isEmpty()) episodeContainer.getChildren().get(0).getStyleClass().add("episode-active");
    }

    @FXML void goHome() {
        if(mediaPlayer!=null) {mediaPlayer.stop(); mediaPlayer.dispose();}
        SceneNavigator.loadHome();
    }
}