package com.myapp.controller;

import com.myapp.exception.ForgotPasswordException;
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

    @FXML
    private void handleSendOtp(ActionEvent event) {
        lblError.setVisible(false);

        try {
            String email = txtEmail.getText().trim();
            otpService.sendOtp(email);
            goToOtpScreen(event);

        } catch (ForgotPasswordException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Có lỗi hệ thống, vui lòng thử lại sau");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource("/view/login.fxml"))
            );

            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToOtpScreen(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            Scene scene = new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/view/otp.fxml")
                    )
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
