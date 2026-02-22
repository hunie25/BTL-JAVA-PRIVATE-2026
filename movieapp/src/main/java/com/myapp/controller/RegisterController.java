package com.myapp.controller;

import com.myapp.exception.RegisterException;
import com.myapp.service.AuthService;
import com.myapp.util.SceneNavigator;
import com.myapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class RegisterController {
    @FXML private TextField txtUsername, txtEmail;
    @FXML private PasswordField pfPassword, pfConfirmPassword;
    @FXML private TextField tfPassword, tfConfirmPassword;
    @FXML private Label lblError;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        lblError.setVisible(false);

        // Ràng buộc dữ liệu 2 chiều giữa trường ẩn và hiện mật khẩu
        tfPassword.textProperty().bindBidirectional(pfPassword.textProperty());
        tfConfirmPassword.textProperty().bindBidirectional(pfConfirmPassword.textProperty());

        // Hỗ trợ nhấn Enter để đăng ký nhanh
        txtUsername.setOnAction(e -> handleRegister());
        txtEmail.setOnAction(e -> handleRegister());
        pfPassword.setOnAction(e -> handleRegister());
    }

        @FXML
        private void handleRegister() {
            lblError.setVisible(false);

            String username = txtUsername.getText().trim();
            String email = txtEmail.getText().trim();
            String password = pfPassword.getText();
            String confirmPassword = pfConfirmPassword.getText();

            try {
                authService.register(username, email, password, confirmPassword);
                SessionManager.set("REGISTER_EMAIL", email);
                SceneNavigator.goOtp();

            } catch (RegisterException e) {
                showError(e.getMessage());
            } catch (Exception e) {
                showError("Lỗi hệ thống khi đăng ký, vui lòng thử lại!");
                e.printStackTrace();
            }
        }

    @FXML
    private void togglePassword() {
        toggleVisibility(pfPassword, tfPassword);
    }

    @FXML
    private void toggleConfirmPassword() {
        toggleVisibility(pfConfirmPassword, tfConfirmPassword);
    }

    /**
     * Hàm dùng chung để ẩn/hiện mật khẩu
     */
    private void toggleVisibility(PasswordField pf, TextField tf) {
        boolean isVisible = pf.isVisible();
        pf.setVisible(!isVisible);
        pf.setManaged(!isVisible);
        tf.setVisible(isVisible);
        tf.setManaged(isVisible);

        if (isVisible) tf.requestFocus(); else pf.requestFocus();
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        SceneNavigator.goLogin();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}