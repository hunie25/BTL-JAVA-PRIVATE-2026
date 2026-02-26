package com.myapp.controller;

import com.myapp.exception.ForgotPasswordException;
import com.myapp.service.AuthService;
import com.myapp.service.OtpService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ForgotPasswordController {

    @FXML
    private TextField txtEmail;

    @FXML
    private Label lblError;

    private final OtpService otpService = new OtpService();
    private final AuthService authService = new AuthService();

    @FXML
    private void handleSendOtp(ActionEvent event) {
        resetError();
        String email = txtEmail.getText().trim();

        if (email.isEmpty()) {
            showError("Vui lòng nhập Email!");
            return;
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!java.util.regex.Pattern.matches(emailRegex, email)) {
            showError("Email không hợp lệ (Ví dụ: abc@gmail.com)!");
            txtEmail.getStyleClass().add("input-field-error");
            return;
        }

        try {
            if (!authService.isEmailExists(email)) {
                showError("Email này chưa được đăng ký trong hệ thống!");
                return;
            }
            com.myapp.util.SessionManager.set("RESET_EMAIL", email);
            otpService.sendOtp(email);
            goToOtpScreen(event);

        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setManaged(true);
        txtEmail.getStyleClass().add("input-field-error");
    }

    private void resetError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
        txtEmail.getStyleClass().remove("input-field-error");
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        switchScene(event, "/view/login.fxml");
    }

    private void goToOtpScreen(ActionEvent event) {
        switchScene(event, "/view/otp.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxmlPath)));
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể chuyển trang, vui lòng kiểm tra file FXML!");
        }
    }
}