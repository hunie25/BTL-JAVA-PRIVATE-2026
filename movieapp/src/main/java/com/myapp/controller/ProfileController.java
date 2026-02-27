package com.myapp.controller;

import com.myapp.dao.HistoryDAO;
import com.myapp.model.Movie;
import com.myapp.model.User;
import com.myapp.util.SceneNavigator;
import com.myapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import java.util.List;

public class ProfileController {
    @FXML
    private Label lblUsername, lblEmail;
    @FXML private HBox historyContainer;

    public void initialize() {
        User user = SessionManager.getUser();
        if (user != null) {
            lblUsername.setText(user.getUsername());
            lblEmail.setText(user.getEmail() != null ? user.getEmail() : "Chưa cập nhật email");
            loadHistory(user.getId());
        }
    }

    private void loadHistory(int userId) {
        HistoryDAO dao = new HistoryDAO();
        List<Movie> history = dao.getWatchHistory(userId);

        historyContainer.getChildren().clear();
        for (Movie m : history) {
            VBox card = createHistoryCard(m, userId);
            historyContainer.getChildren().add(card);
        }
    }

    private VBox createHistoryCard(Movie m, int userId) {
        VBox card = new VBox();
        card.setSpacing(5);
        card.setAlignment(Pos.TOP_CENTER);

        StackPane imagePane = new StackPane();
        ImageView img = new ImageView();
        img.setFitWidth(100);
        img.setFitHeight(150);

        String fullUrl = m.getFullThumbUrl();
        if (fullUrl != null && fullUrl.contains("-poster.jpg")) {
            fullUrl = fullUrl.replace("-poster.jpg", "-thumb.jpg");
        }

        if (fullUrl != null && !fullUrl.isBlank()) {
            try {
                img.setImage(new Image(fullUrl, true));
            } catch (Exception e) {
                System.err.println("Lỗi nạp ảnh: " + fullUrl);
            }
        }

        img.setOnMouseClicked(e -> SceneNavigator.loadWatchScene(m));
        img.setCursor(javafx.scene.Cursor.HAND);

        Button btnDelete = new Button("✕");
        btnDelete.setStyle("-fx-background-color: rgba(255,0,0,0.7); -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-font-size: 10px; -fx-padding: 2 5;");
        btnDelete.setTranslateX(35);
        btnDelete.setTranslateY(-60);

        btnDelete.setOnAction(e -> {
            HistoryDAO dao = new HistoryDAO();
            dao.deleteHistory(userId, m.getSlug());
            loadHistory(userId);
        });

        imagePane.getChildren().addAll(img, btnDelete);

        Label title = new Label(m.getName());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        title.setWrapText(true);
        title.setMaxWidth(100);
        title.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imagePane, title);
        return card;
    }

    @FXML void goBack() { SceneNavigator.loadHome(); }

    @FXML void handleLogout() {
        SessionManager.logout();
        SceneNavigator.loadHome();
    }
}