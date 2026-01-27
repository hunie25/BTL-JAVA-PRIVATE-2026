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

    // FXML paths
    private static final String LOGIN_FXML = "/view/login.fxml";
    private static final String REGISTER_FXML = "/view/register.fxml";
    private static final String FORGOT_FXML = "/view/forgotPassword.fxml";
    private static final String OTP_FXML = "/view/otp.fxml";
    private static final String RESET_FXML = "/view/resetPassword.fxml";

    public static final String HOME_FXML = "/fxml/home.fxml";
    public static final String WATCH_FXML = "/fxml/watch.fxml";

    private SceneNavigator() {}

    /**
     * Gọi ở MainApp.start()
     */
    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    /* ===================== AUTH SCENES ===================== */

    public static void goLogin() {
        switchScene(LOGIN_FXML);
    }

    public static void goRegister() {
        switchScene(REGISTER_FXML);
    }

    public static void goForgotPassword() {
        switchScene(FORGOT_FXML);
    }

    public static void goOtp() {
        switchScene(OTP_FXML);
    }

    public static void goResetPassword() {
        switchScene(RESET_FXML);
    }

    /* ===================== COMMON SWITCH ===================== */

    private static void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene;
            if (mainStage.getScene() != null) {
                scene = new Scene(
                        root,
                        mainStage.getScene().getWidth(),
                        mainStage.getScene().getHeight()
                );
            } else {
                scene = new Scene(root);
            }

            mainStage.setScene(scene);
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            UiUtils.showError("Lỗi hệ thống", "Không thể tải màn hình: " + e.getMessage());
        }
    }

    /* ===================== HOME ===================== */

    public static void loadHome() {
        switchScene(HOME_FXML);
    }

    /* ===================== WATCH ===================== */

    public static void loadWatchScene(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(WATCH_FXML));
            Parent root = loader.load();

            WatchController controller = loader.getController();
            controller.initData(movie);

            Scene scene = new Scene(
                    root,
                    mainStage.getScene().getWidth(),
                    mainStage.getScene().getHeight()
            );

            mainStage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            UiUtils.showError(
                    "Lỗi hệ thống",
                    "Không thể mở trang xem phim: " + e.getMessage()
            );
        }
    }
}
