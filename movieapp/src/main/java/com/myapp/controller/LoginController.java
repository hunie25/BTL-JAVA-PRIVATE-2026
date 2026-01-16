package com.myapp.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private TextField tfPassword;

    @FXML
    private ImageView iconEye;

    private boolean isPasswordVisible = false;

    // ================== TOGGLE PASSWORD ==================
    @FXML
    private void togglePassword() {
        if (isPasswordVisible) {
            // Ẩn mật khẩu
            pfPassword.setText(tfPassword.getText());
            pfPassword.setVisible(true);
            pfPassword.setManaged(true);

            tfPassword.setVisible(false);
            tfPassword.setManaged(false);

            iconEye.setImage(new Image(
                    getClass().getResourceAsStream("/images/eye.png")
            ));
        } else {
            // Hiện mật khẩu
            tfPassword.setText(pfPassword.getText());
            tfPassword.setVisible(true);
            tfPassword.setManaged(true);

            pfPassword.setVisible(false);
            pfPassword.setManaged(false);

            iconEye.setImage(new Image(
                    getClass().getResourceAsStream("/images/eyeOff.png")
            ));
        }

        isPasswordVisible = !isPasswordVisible;
    }
    @FXML
    private ImageView eye;

    @FXML
    public void initialize() {
        iconEye.setCursor(Cursor.HAND);
    }

    @FXML
    private Button btnLogin;

    @FXML
    private void hoverLogin() {
        btnLogin.setStyle("-fx-background-color: #ffa733;");
    }

    @FXML
    private void exitLogin() {
        btnLogin.setStyle("-fx-background-color: #ff8c00;");
    }


    // ================== LOGIN ==================
    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = isPasswordVisible
                ? tfPassword.getText()
                : pfPassword.getText();

        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        // TODO: xử lý đăng nhập DB
    }


    @FXML
    private void handleRegister() {
        System.out.println("Register clicked");
    }

    @FXML
    private void handleForgotPassword() {
        System.out.println("Forgot password clicked");
    }
}
