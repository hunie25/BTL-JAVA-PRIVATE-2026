package com.myapp.util;

import com.myapp.controller.WatchController;
import com.myapp.model.Movie;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneNavigator {

    private static Stage mainStage;

    // Các đường dẫn đến file FXML
    public static final String HOME_FXML = "/fxml/home.fxml";
    public static final String WATCH_FXML = "/fxml/watch.fxml";

    /**
     * Gọi hàm này ở MainApp để set Stage chính
     */
    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    /**
     * Chuyển về trang chủ
     */
    public static void loadHome() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(HOME_FXML));
            Parent root = loader.load();

            // Giữ nguyên kích thước cửa sổ hiện tại hoặc set lại nếu muốn
            Scene scene = new Scene(root, mainStage.getScene().getWidth(), mainStage.getScene().getHeight());
            mainStage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            UiUtils.showError("Lỗi hệ thống", "Không thể tải trang chủ: " + e.getMessage());
        }
    }

    /**
     * Chuyển sang trang xem phim và truyền dữ liệu phim vào
     */
    public static void loadWatchScene(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(WATCH_FXML));
            Parent root = loader.load();

            // Lấy controller của màn hình Watch để truyền dữ liệu phim
            WatchController controller = loader.getController();
            controller.initData(movie); // Truyền object Movie vào để load tập phim

            Scene scene = new Scene(root, mainStage.getScene().getWidth(), mainStage.getScene().getHeight());
            mainStage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            UiUtils.showError("Lỗi hệ thống", "Không thể mở trang xem phim: " + e.getMessage());
        }
    }
}