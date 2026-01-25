package com.myapp.controller;

import com.myapp.exception.RegisterException;
import com.myapp.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

public class RegisterController {
    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField pfPassword;
    @FXML
    private TextField tfPassword;

    @FXML
    private PasswordField pfConfirmPassword;
    @FXML
    private TextField tfConfirmPassword;

    @FXML
    private Label lblError;
    @FXML
    private Button btnRegister;
    @FXML
    private Hyperlink linkLogin;

    private final AuthService authService = new AuthService();
    @FXML
    private void handleRegister() {
        lblError.setVisible(false);

        String username = txtUsername.getText().trim();
        String password = pfPassword.isVisible()
                ? pfPassword.getText()
                : tfPassword.getText();

        String confirmPassword = pfConfirmPassword.isVisible()
                ? pfConfirmPassword.getText()
                : tfConfirmPassword.getText();

        try {
            authService.register(username, password, confirmPassword);
            goToLogin(null);
        } catch (RegisterException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Có lỗi xảy ra, vui lòng thử lại!");
            e.printStackTrace();
        }
    }

    @FXML
    private void togglePassword() {
        toggle(pfPassword, tfPassword);
    }

    @FXML
    private void toggleConfirmPassword() {
        toggle(pfConfirmPassword, tfConfirmPassword);
    }

    private void toggle(PasswordField pf, TextField tf) {
        if (pf.isVisible()) {
            tf.setText(pf.getText());
            tf.setVisible(true);
            tf.setManaged(true);
            pf.setVisible(false);
            pf.setManaged(false);
        } else {
            pf.setText(tf.getText());
            pf.setVisible(true);
            pf.setManaged(true);
            tf.setVisible(false);
            tf.setManaged(false);
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(getClass().getResource("/view/login.fxml"))
            ));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}