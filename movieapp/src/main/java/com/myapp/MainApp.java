package com.myapp;

import com.myapp.util.SceneNavigator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneNavigator.setMainStage(primaryStage);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        primaryStage.setTitle("OPhim Hub");
        primaryStage.setWidth(375);
        primaryStage.setHeight(812);
        primaryStage.setResizable(false); // QUAN TRỌNG
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Không cần dòng System.setProperty nữa
    }
}