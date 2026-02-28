package com.myapp.util;

import com.myapp.controller.SearchResultsController;
import com.myapp.controller.WatchController;
import com.myapp.controller.CategoryController;
import com.myapp.model.Movie;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {

    private static Stage mainStage;

    private static final String LOGIN_FXML = "/view/login.fxml";
    private static final String REGISTER_FXML = "/view/register.fxml";
    private static final String FORGOT_FXML = "/view/forgotPassword.fxml";
    private static final String OTP_FXML = "/view/otp.fxml";
    private static final String RESET_FXML = "/view/resetPassword.fxml";
    private static final String CATEGORY_FXML = "/fxml/category.fxml";

    public static final String HOME_FXML = "/fxml/home.fxml";
    public static final String WATCH_FXML = "/fxml/watch.fxml";
    private static final String PROFILE_FXML = "/fxml/profile.fxml";
    private static final String SEARCH_RESULT_FXML = "/fxml/searchResults.fxml";

    private SceneNavigator() {}

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

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

    public static void goProfile() {
        switchScene(PROFILE_FXML);
    }

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

    public static void loadSearchScene(String keyword) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(SEARCH_RESULT_FXML));
            Parent root = loader.load();
            SearchResultsController controller = loader.getController();
            controller.searchMovies(keyword);

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
                    "Không thể mở trang tìm kiếm: " + e.getMessage()
            );
        }
    }

    public static void loadHome() {
        switchScene(HOME_FXML);
    }

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

    public static void loadCategoryScene(String title, String listSlug) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(CATEGORY_FXML));
            Parent root = loader.load();

            CategoryController controller = loader.getController();
            controller.initCategory(title, listSlug);

            Scene scene = new Scene(
                    root,
                    mainStage.getScene().getWidth(),
                    mainStage.getScene().getHeight()
            );

            mainStage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            UiUtils.showError("Lỗi hệ thống", "Không thể mở trang thể loại: " + e.getMessage());
        }
    }
}