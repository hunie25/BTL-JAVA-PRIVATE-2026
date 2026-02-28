package com.myapp.controller;

import com.myapp.util.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import java.util.regex.Pattern;

public class ResetPasswordController {

    @FXML private PasswordField pfNewPassword;
    @FXML private TextField tfNewPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private TextField tfConfirmPassword;
    @FXML private Label lblError;

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
    private final com.myapp.service.AuthService authService = new com.myapp.service.AuthService();

    @FXML
    private void toggleNewPassword(MouseEvent event) {
        if (pfNewPassword.isVisible()) {
            tfNewPassword.setText(pfNewPassword.getText());
            updateVisibility(tfNewPassword, pfNewPassword);
        } else {
            pfNewPassword.setText(tfNewPassword.getText());
            updateVisibility(pfNewPassword, tfNewPassword);
        }
    }
    private void updateVisibility(Node toShow, Node toHide) {
        toShow.setVisible(true);
        toShow.setManaged(true);
        toHide.setVisible(false);
        toHide.setManaged(false);
    }

    @FXML
    private void handleResetPassword(ActionEvent event) {
        String newPass = pfNewPassword.isVisible() ? pfNewPassword.getText() : tfNewPassword.getText();
        String confirmPass = pfConfirmPassword.isVisible() ? pfConfirmPassword.getText() : tfConfirmPassword.getText();

        resetUIState();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            showError("Vui lòng không để trống mật khẩu!");
        }
        else if (!PASSWORD_PATTERN.matcher(newPass).matches()) {
            showError("Mật khẩu yếu! Cần ít nhất 8 ký tự, 1 chữ hoa, 1 chữ thường và 1 số.");
            addErrorStyle(pfNewPassword, tfNewPassword);
        }
        else if (!newPass.equals(confirmPass)) {
            showError("Mật khẩu xác nhận không khớp!");
            addErrorStyle(pfConfirmPassword, tfConfirmPassword);
        }
        else {
            String email = (String) com.myapp.util.SessionManager.get("RESET_EMAIL");
            System.out.println("DEBUG: Đang tiến hành đổi mật khẩu cho email: " + email);

            if (email == null || email.isEmpty()) {
                showError("Lỗi: Không tìm thấy phiên làm việc. Vui lòng thử lại!");
                return;
            }

            try {
                authService.resetPassword(email, newPass);
                System.out.println(">>> Cập nhật Database thành công!");

                goToLogin(event);
            } catch (Exception e) {
                showError("Lỗi hệ thống: " + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void addErrorStyle(Node... nodes) {
        for (Node node : nodes) {
            node.getStyleClass().add("input-field-error");
        }
    }

    private void resetUIState() {
        lblError.setVisible(false);
        lblError.setManaged(false);
        pfNewPassword.getStyleClass().remove("input-field-error");
        tfNewPassword.getStyleClass().remove("input-field-error");
        pfConfirmPassword.getStyleClass().remove("input-field-error");
        tfConfirmPassword.getStyleClass().remove("input-field-error");
    }

    @FXML
    private void goToLogin(ActionEvent event) {
            SceneNavigator.goLogin();
    }



}