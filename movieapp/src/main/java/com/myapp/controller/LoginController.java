package com.myapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;


public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private TextField tfPassword;

    @FXML
    private Label lblError;

    private boolean isShowPassword = false;

    @FXML
    private void togglePassword() {

        if (isShowPassword) {
            pfPassword.setText(tfPassword.getText());
            pfPassword.setVisible(true);
            pfPassword.setManaged(true);

            tfPassword.setVisible(false);
            tfPassword.setManaged(false);
        } else {
            tfPassword.setText(pfPassword.getText());
            tfPassword.setVisible(true);
            tfPassword.setManaged(true);

            pfPassword.setVisible(false);
            pfPassword.setManaged(false);
        }

        isShowPassword = !isShowPassword;
    }

    @FXML
    private void handleLogin(javafx.event.ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            Scene scene = new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/view/home.fxml")
                    )
            );

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword(javafx.event.ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            Scene scene = new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/view/forgotPassword.fxml")
                    )
            );

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void goToRegister(javafx.event.ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource("/view/register.fxml"))
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
