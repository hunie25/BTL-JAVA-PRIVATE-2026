package com.myapp.controller;

import com.myapp.exception.ForgotPasswordException;
import com.myapp.service.OtpService;
import com.myapp.util.SceneNavigator;
import com.myapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class OtpController {

    @FXML
    private TextField txtOtp;

    @FXML
    private Label lblError;

    private final OtpService otpService = new OtpService();

    @FXML
    private void handleVerifyOtp() {
        lblError.setVisible(false);

        try {
            String email = (String) SessionManager.get("RESET_EMAIL");
            otpService.verifyOtp(email, txtOtp.getText());

            SceneNavigator.goResetPassword();

        } catch (ForgotPasswordException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        SceneNavigator.goLogin();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
