package com.myapp;

import com.myapp.util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        SceneNavigator.init(stage);
        SceneNavigator.goLogin();
//        SceneNavigator.goHome();
        stage.setTitle("Movie App - Week 1 (Xem phim)");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


