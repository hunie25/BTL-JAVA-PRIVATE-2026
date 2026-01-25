package com.myapp.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneNavigator {
    private static Stage stage;

    // Kích thước giả lập điện thoại (portrait)
    private static final int PHONE_W = 335;
    private static final int PHONE_H = 620;

    private SceneNavigator() {}

    public static void init(Stage primaryStage) {
        stage = primaryStage;
        stage.setResizable(false); // giả lập điện thoại
    }

    public static void goLogin() {
        setScene("/view/login.fxml", PHONE_W, PHONE_H, null);
    }

    public static void goRegister() {
        setScene("/view/register.fxml", PHONE_W, PHONE_H, null);
    }

    public static void goForgotPassword() {
        setScene("/view/forgotPassword.fxml", PHONE_W, PHONE_H, null);
    }

    public static void goOtp() {
        setScene("/view/otp.fxml", PHONE_W, PHONE_H, null);
    }

    public static void goResetPassword() {
        setScene("/view/resetPassword.fxml", PHONE_W, PHONE_H, null);
    }

    public static void goHome() {
        setScene("/view/home.fxml", PHONE_W, PHONE_H, null);
    }

    public static void goWatch(String slug) {
        setScene("/view/watch.fxml", PHONE_W, PHONE_H, slug);
    }

    // Optional: test xoay ngang “giống điện thoại”
    public static void setLandscape(boolean landscape) {
        if (stage == null) return;
        if (landscape) {
            stage.setWidth(PHONE_H);
            stage.setHeight(PHONE_W);
        } else {
            stage.setWidth(PHONE_W);
            stage.setHeight(PHONE_H);
        }
        stage.centerOnScreen();
    }

    private static void setScene(String fxmlPath, int w, int h, String slug) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            // truyền slug nếu controller có setSlug(String)
            if (slug != null) {
                Object controller = loader.getController();
                try {
                    controller.getClass().getMethod("setSlug", String.class).invoke(controller, slug);
                } catch (NoSuchMethodException ignore) {}
            }

            Scene scene = new Scene(root, w, h);
            var css = SceneNavigator.class.getResource("/css/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.setMinWidth(w);
            stage.setMaxWidth(w);
            stage.setMinHeight(h);
            stage.setMaxHeight(h);

            /* để scene tự quyết định */
            stage.sizeToScene();

            stage.centerOnScreen();
        } catch (Exception e) {
            throw new RuntimeException("Cannot load " + fxmlPath, e);
        }
    }
}
