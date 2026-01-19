package com.myapp.controller;

import com.myapp.model.Movie;
import com.myapp.service.MovieService;
import com.myapp.util.SceneNavigator;
import com.myapp.util.UiUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.prefs.Preferences;

public class HomeController {
    @FXML private ListView<Movie> lvMovies;
    @FXML private Button btnReload;
    @FXML private Label lbStatus;

    private final MovieService movieService = new MovieService();
    private final Preferences prefs = Preferences.userRoot().node("movieapp");

    @FXML
    public void initialize() {
        lvMovies.setCellFactory(list -> new MovieCardCell(prefs));

        // 1 click mở (mobile)
        lvMovies.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) openSelectedMovie();
        });

        // Enter mở
        lvMovies.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER -> openSelectedMovie();
            }
        });

        btnReload.setOnAction(e -> loadLatest());

        // quay lại home thì progress đã đổi -> refresh list
        Platform.runLater(() -> {
            if (lvMovies.getScene() != null) {
                lvMovies.getScene().getWindow().focusedProperty().addListener((obs, oldV, focused) -> {
                    if (focused) lvMovies.refresh();
                });
            }
        });

        loadLatest();
    }

    private void openSelectedMovie() {
        Movie m = lvMovies.getSelectionModel().getSelectedItem();
        if (m == null) return;

        String slug = m.getSlug();
        if (slug == null || slug.isBlank()) {
            UiUtils.error("Phim này thiếu slug nên không mở được.");
            return;
        }

        double sec = prefs.getDouble(key(slug, "sec"), 0);
        double total = prefs.getDouble(key(slug, "total"), 0);

        // hỏi xem tiếp nếu đã xem dở
        if (sec > 10 && total > 0 && sec < total - 3) {
            int percent = (int) Math.round((sec / total) * 100.0);

            ButtonType btnContinue = new ButtonType("Xem tiếp (" + percent + "%)", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnRestart  = new ButtonType("Xem lại từ đầu", ButtonBar.ButtonData.NO);
            ButtonType btnCancel   = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Tiếp tục xem?");
            a.setHeaderText(m.getName());
            a.setContentText("Bạn đang xem dở. Muốn xem tiếp hay xem lại từ đầu?");
            a.getButtonTypes().setAll(btnContinue, btnRestart, btnCancel);

            // áp CSS cho dialog (để không bị trắng “desktop”)
            var css = getClass().getResource("/css/app.css");
            if (css != null) a.getDialogPane().getStylesheets().add(css.toExternalForm());
            a.getDialogPane().getStyleClass().add("app-root");

            ButtonType choice = a.showAndWait().orElse(btnCancel);
            if (choice == btnCancel) return;

            if (choice == btnRestart) clearProgress(slug);
        }

        SceneNavigator.goWatch(slug);
    }

    private void loadLatest() {
        setStatus("Đang tải danh sách phim...");
        btnReload.setDisable(true);
        lvMovies.getItems().clear();

        new Thread(() -> {
            try {
                List<Movie> movies = movieService.getLatest(1);
                Platform.runLater(() -> {
                    lvMovies.getItems().setAll(movies);
                    setStatus("OK: " + movies.size() + " phim");
                    btnReload.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    btnReload.setDisable(false);
                    setStatus("Lỗi khi tải phim.");
                    UiUtils.error("Lỗi load phim: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void setStatus(String s) {
        Platform.runLater(() -> lbStatus.setText(s));
    }

    private String key(String slug, String suffix) {
        return slug + ":" + suffix;
    }

    private void clearProgress(String slug) {
        prefs.remove(key(slug, "sec"));
        prefs.remove(key(slug, "total"));
        prefs.remove(key(slug, "epIndex"));
        prefs.remove(key(slug, "updated"));
        prefs.remove(key(slug, "epUrl"));
    }

    // ====== Cell tối ưu: tạo UI 1 lần, update dữ liệu thôi ======
    private static class MovieCardCell extends ListCell<Movie> {
        private final Preferences prefs;

        private final Label lbName = new Label();
        private final Label lbYear = new Label();
        private final ProgressBar pb = new ProgressBar(0);
        private final Label lbProg = new Label();

        private final VBox card;

        MovieCardCell(Preferences prefs) {
            this.prefs = prefs;

            lbName.getStyleClass().add("movie-title");
            lbYear.getStyleClass().add("movie-year");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox top = new HBox(10, lbName, spacer, lbYear);
            top.setAlignment(Pos.CENTER_LEFT);

            pb.setMaxWidth(Double.MAX_VALUE);
            pb.getStyleClass().add("movie-progress");

            lbProg.getStyleClass().add("movie-progress-text");

            HBox progRow = new HBox(10, pb, lbProg);
            progRow.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(pb, Priority.ALWAYS);

            card = new VBox(8, top, progRow);
            card.getStyleClass().add("movie-card");
            card.setFillWidth(true);
        }

        @Override
        protected void updateItem(Movie item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            String name = item.getName() == null ? "Phim" : item.getName();
            lbName.setText(name);

            lbYear.setText(item.getYear() > 0 ? String.valueOf(item.getYear()) : "");

            String slug = item.getSlug() == null ? "" : item.getSlug();
            double sec = prefs.getDouble(slug + ":sec", 0);
            double total = prefs.getDouble(slug + ":total", 0);

            if (sec > 5 && total > 0) {
                double p = Math.max(0, Math.min(1, sec / total));
                int percent = (int) Math.round(p * 100);
                pb.setProgress(p);
                lbProg.setText("Đã xem " + percent + "%");
            } else {
                pb.setProgress(0);
                lbProg.setText("Chưa xem");
            }

            setText(null);
            setGraphic(card);
        }
    }
}
