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
    private void togglePassword(MouseEvent event) {

        // password
        if (pfPassword.isVisible()) {
            tfPassword.setText(pfPassword.getText());
            tfPassword.setVisible(true);
            tfPassword.setManaged(true);
            pfPassword.setVisible(false);
            pfPassword.setManaged(false);
        } else {
            pfPassword.setText(tfPassword.getText());
            pfPassword.setVisible(true);
            pfPassword.setManaged(true);
            tfPassword.setVisible(false);
            tfPassword.setManaged(false);
        }

        // confirm password
        if (pfConfirmPassword.isVisible()) {
            tfConfirmPassword.setText(pfConfirmPassword.getText());
            tfConfirmPassword.setVisible(true);
            tfConfirmPassword.setManaged(true);
            pfConfirmPassword.setVisible(false);
            pfConfirmPassword.setManaged(false);
        } else {
            pfConfirmPassword.setText(tfConfirmPassword.getText());
            pfConfirmPassword.setVisible(true);
            pfConfirmPassword.setManaged(true);
            tfConfirmPassword.setVisible(false);
            tfConfirmPassword.setManaged(false);
        }
    }


    @FXML
    private void goToLogin(MouseEvent event) {
        try {
            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource("/view/login.fxml"))
            );
            Stage stage;
            if (event != null) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) txtUsername.getScene().getWindow();
            }

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