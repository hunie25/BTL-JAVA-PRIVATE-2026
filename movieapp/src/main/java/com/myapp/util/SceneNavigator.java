package com.myapp.util;

import com.myapp.controller.HomeController;
import com.myapp.controller.WatchController;
import com.myapp.model.Movie;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {
    private static Stage primaryStage;

    // Lưu tham chiếu đến Controller hiện tại để tắt Timer khi cần
    private static Object currentController;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadHome() {
        try {
            // 1. Dọn dẹp Controller cũ (nếu có)
            cleanupCurrentController();

            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/fxml/home.fxml"));
            Parent root = loader.load();

            // 2. Lưu Controller mới
            currentController = loader.getController();

            Scene scene = new Scene(root);
            scene.setFill(Color.web("#020617"));
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadWatchScene(Movie movie) {
        try {
            // 1. Dọn dẹp Controller cũ (Dừng slide ảnh ở Home)
            cleanupCurrentController();

            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/fxml/watch.fxml"));
            Parent root = loader.load();

            WatchController controller = loader.getController();
            controller.initData(movie);

            // 2. Lưu Controller mới
            currentController = controller;

            Scene scene = new Scene(root);
            scene.setFill(Color.web("#020617"));
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadLogin() {
        try {
            cleanupCurrentController();

            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/view/login.fxml"));
            Parent root = loader.load();

            currentController = loader.getController();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void goResetPassword() {
        try {
            cleanupCurrentController();

            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/view/resetPassword.fxml")
            );
            Parent root = loader.load();

            currentController = loader.getController();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // Hàm dọn dẹp quan trọng
    private static void cleanupCurrentController() {
        if (currentController instanceof HomeController) {
            System.out.println("Stopping Home animations...");
            ((HomeController) currentController).stopTimerLogic();
        }
        else if (currentController instanceof WatchController) {
            // Nếu cần dừng video khi thoát màn hình Watch
            // ((WatchController) currentController).stopVideo();
        }
        currentController = null;
    }
}