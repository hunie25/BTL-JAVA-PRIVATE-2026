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

        // Ưu tiên kiểm tra email đăng ký, nếu không có thì kiểm tra email reset
        String email = (String) SessionManager.get("REGISTER_EMAIL");
        if (email == null) {
            email = (String) SessionManager.get("RESET_EMAIL");
        }

        if (email != null) {
            lblMessage.setText("Mã xác minh đã được gửi đến: " + email);
        }

        // Tự động focus vào ô nhập OTP
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
            // Kiểm tra xem đang thực hiện luồng nào
            String registerEmail = (String) SessionManager.get("REGISTER_EMAIL");
            String resetEmail = (String) SessionManager.get("RESET_EMAIL");

            if (registerEmail != null) {
                // Luồng ĐĂNG KÝ: Kích hoạt tài khoản đã lưu trong DB
                verifyRegistration(otp, registerEmail);
            } else if (resetEmail != null) {
                // Luồng QUÊN MẬT KHẨU: Xác nhận để cho phép đổi pass
                verifyReset(otp, resetEmail);
            } else {
                showError("Phiên làm việc hết hạn. Vui lòng thử lại từ đầu.");
            }

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void verifyRegistration(String otp, String email) throws Exception {
        // 1. Xác minh OTP qua Service
        otpService.verifyOtpForRegistration(email, otp);

        // 2. Kích hoạt trạng thái verified = true trong Database
        // Bạn cần đảm bảo đã viết hàm updateVerificationStatus trong UserDAO/AuthService
        authService.activateAccount(email);

        // 3. Xóa session đăng ký
        SessionManager.remove("REGISTER_EMAIL");

        // 4. Thông báo và chuyển về Login
        showSuccessAlert("Xác minh thành công! Tài khoản của bạn đã được kích hoạt.");
        SceneNavigator.goLogin();
    }

    private void verifyReset(String otp, String email) throws Exception {
        // Xác minh OTP để reset mật khẩu
        otpService.verifyOtp(email, otp);

        // Chuyển sang màn hình đặt lại mật khẩu mới
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