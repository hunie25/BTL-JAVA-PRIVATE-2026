package com.myapp;

import com.myapp.util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Cấu hình Stage
        primaryStage.setTitle("HUB Films");

        // --- THIẾT LẬP KÍCH THƯỚC CHUẨN ĐIỆN THOẠI ---
        primaryStage.setMinWidth(375);
        primaryStage.setMinHeight(700);

        // Kích thước mặc định khi mở lên (Dáng điện thoại)
        primaryStage.setWidth(390); // iPhone 12/13/14 width
        primaryStage.setHeight(844);

        SceneNavigator.setPrimaryStage(primaryStage);
        SceneNavigator.loadLogin();
        //moi sua
     //   SceneNavigator.loadHome();
    }

    public static void main(String[] args) {
        launch(args);
    }
}