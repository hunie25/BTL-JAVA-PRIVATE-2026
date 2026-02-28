package com.myapp.controller;

import com.myapp.dao.HistoryDAO;
import com.myapp.model.Movie;
import com.myapp.model.User;
import com.myapp.util.SceneNavigator;
import com.myapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProfileController {

    @FXML private Label lblUsername, lblEmail;
    @FXML private VBox historyContainer;

    private final HistoryDAO dao = new HistoryDAO();

    public void initialize() {
        User user = SessionManager.getUser();
        if (user != null) {
            lblUsername.setText(user.getUsername());
            lblEmail.setText(user.getEmail() != null ? user.getEmail() : "Chưa cập nhật email");
            loadHistory(user.getId());
        }
    }

    private void loadHistory(int userId) {
        List<Movie> history = dao.getWatchHistory(userId);
        historyContainer.getChildren().clear();

        for (Movie m : history) {
            historyContainer.getChildren().add(createHistoryRow(m, userId));
        }
    }

    private HBox createHistoryRow(Movie m, int userId) {
        HBox row = new HBox();
        row.setSpacing(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 14; -fx-padding: 12;");

        ImageView poster = new ImageView();
        poster.setFitWidth(86);
        poster.setFitHeight(110);
        poster.setPreserveRatio(true);

        String url = m.getFullPosterUrl();
        if (url == null || url.isBlank()) url = m.getFullThumbUrl();

        if (url != null && !url.isBlank()) {
            try { poster.setImage(new Image(url, true)); } catch (Exception ignored) {}
        }

        poster.setCursor(Cursor.HAND);
        poster.setOnMouseClicked(e -> SceneNavigator.loadWatchScene(m));

        VBox info = new VBox();
        info.setSpacing(6);
        info.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(m.getName() != null ? m.getName() : "");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 900;");
        title.setWrapText(true);
        title.setMaxWidth(190);

        int ep = m.getHistoryEpisodeIndex() != null ? m.getHistoryEpisodeIndex() : 1;
        int pos = m.getHistoryPositionSeconds() != null ? m.getHistoryPositionSeconds() : 0;

        Label meta = new Label("ĐANG XEM: " + ep + "  •  " + formatClock(pos));
        meta.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 12px; -fx-font-weight: 800;");

        Label time = new Label(formatViewedAt(m.getHistoryViewedAt()));
        time.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 12px; -fx-font-weight: 600;");

        info.getChildren().addAll(title, meta, time);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDel = new Button("🗑");
        btnDel.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.45); -fx-font-size: 16px;");
        btnDel.setCursor(Cursor.HAND);
        btnDel.setOnAction(e -> {
            dao.deleteHistory(userId, m.getSlug());
            loadHistory(userId);
        });

        row.getChildren().addAll(poster, info, spacer, btnDel);
        row.setOnMouseClicked(e -> SceneNavigator.loadWatchScene(m));
        row.setCursor(Cursor.HAND);

        return row;
    }

    private String formatClock(int seconds) {
        seconds = Math.max(0, seconds);
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%d:%02d", m, s);
    }

    private String formatViewedAt(String raw) {
        if (raw == null || raw.isBlank()) return "";

        String s = raw.trim().replace("T", " ");
        if (s.contains(".")) s = s.substring(0, s.indexOf('.')); // bỏ .0

        try {
            // Nếu DB đã lưu dạng có timezone (ví dụ 2026-02-28T09:36:36+07:00 hoặc ...Z)
            if (s.endsWith("Z") || s.matches(".*[+-]\\d{2}:\\d{2}$")) {
                java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(s.replace(" ", "T"));
                java.time.ZonedDateTime zdt = odt.atZoneSameInstant(java.time.ZoneId.systemDefault());
                return zdt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss d/M/yyyy"));
            }


            java.time.LocalDateTime utcLdt = java.time.LocalDateTime.parse(
                    s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );

            java.time.ZonedDateTime local = utcLdt
                    .atZone(java.time.ZoneOffset.UTC)
                    .withZoneSameInstant(java.time.ZoneId.systemDefault());

            return local.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss d/M/yyyy"));
        } catch (Exception e) {
            return s;
        }
    }

    @FXML void onClearAllHistory() {
        User user = SessionManager.getUser();
        if (user == null) return;

        dao.clearAllHistory(user.getId());
        loadHistory(user.getId());
    }

    @FXML void goBack() { SceneNavigator.loadHome(); }

    @FXML void handleLogout() {
        SessionManager.logout();
        SceneNavigator.loadHome();
    }
}