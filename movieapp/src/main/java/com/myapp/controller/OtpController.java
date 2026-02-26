package com.myapp.controller;

import com.myapp.service.AuthService;
import com.myapp.service.OtpService;
import com.myapp.util.SceneNavigator;
import com.myapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class OtpController {

    @FXML private TextField txtOtp;
    @FXML private Label lblError, lblMessage;
    @FXML private Button btnVerify;

    private final OtpService otpService = new OtpService();
    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        lblError.setVisible(false);
        String email = (String) SessionManager.get("REGISTER_EMAIL");
        if (email == null) {
            email = (String) SessionManager.get("RESET_EMAIL");
        }

        if (email != null) {
            lblMessage.setText("Mã xác minh đã được gửi đến: " + email);
        }
        txtOtp.requestFocus();
    }

    @FXML
    private void handleVerifyOtp() {
        lblError.setVisible(false);
        String otp = txtOtp.getText().trim();

        if (otp.isEmpty()) {
            showError("Vui lòng nhập mã OTP!");
            return;
        }

        try {
            String registerEmail = (String) SessionManager.get("REGISTER_EMAIL");
            String resetEmail = (String) SessionManager.get("RESET_EMAIL");

            if (registerEmail != null) {
                verifyRegistration(otp, registerEmail);
            } else if (resetEmail != null) {
                verifyReset(otp, resetEmail);
            } else {
                showError("Phiên làm việc hết hạn. Vui lòng thử lại từ đầu.");
            }

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void verifyRegistration(String otp, String email) throws Exception {
        otpService.verifyOtpForRegistration(email, otp);
        authService.activateAccount(email);
        SessionManager.remove("REGISTER_EMAIL");
        showSuccessAlert("Xác minh thành công! Tài khoản của bạn đã được kích hoạt.");
        SceneNavigator.goLogin();
    }

    private void verifyReset(String otp, String email) throws Exception {
        otpService.verifyOtp(email, otp);
        SceneNavigator.goResetPassword();
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        SceneNavigator.goLogin();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}