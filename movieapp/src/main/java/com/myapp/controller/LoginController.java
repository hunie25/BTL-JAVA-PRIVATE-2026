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
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = isShowPassword
                ? tfPassword.getText()
                : pfPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        if (username.equals("admin") && password.equals("123456")) {
            lblError.setVisible(false);
            System.out.println("Đăng nhập thành công!");
        } else {
            showError("Sai tài khoản hoặc mật khẩu");
        }
    }

    @FXML
    private void handleForgotPassword() {
        System.out.println("Đi tới màn hình quên mật khẩu");
    }

    @FXML
    private void goToRegister(javafx.event.ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource("/view/register.fxml"))
            );

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
