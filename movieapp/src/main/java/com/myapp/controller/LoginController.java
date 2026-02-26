package com.myapp.controller;

import com.myapp.model.User;
import com.myapp.service.AuthService;
import com.myapp.util.SceneNavigator;
import com.myapp.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.myapp.dao.UserDAO;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField pfPassword;
    @FXML private TextField tfPassword;
    @FXML private Label lblError;

    private final AuthService authService = new AuthService();
    private final UserDAO userDAO = new UserDAO();
    private boolean isShowPassword = false;

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        tfPassword.textProperty().bindBidirectional(pfPassword.textProperty());//đồng bộ ẩn hiện mật khẩu
        txtUsername.setOnAction(e -> handleLogin(null));
        pfPassword.setOnAction(e -> handleLogin(null));
        tfPassword.setOnAction(e -> handleLogin(null));
    }

    @FXML
    private void togglePassword() {
        if (isShowPassword) {
            showField(pfPassword, tfPassword);
        } else {
            showField(tfPassword, pfPassword);
        }
        isShowPassword = !isShowPassword;
    }

    private void showField(Control show, Control hide) {
        show.setVisible(true);
        show.setManaged(true);
        hide.setVisible(false);
        hide.setManaged(false);
        show.requestFocus();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        lblError.setVisible(false);
        String username = txtUsername.getText().trim();
        String password = pfPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            User user = authService.login(username, password);

            if (user != null) {
                SessionManager.setUser(user);
                SceneNavigator.loadHome();
            } else {
                showError("Sai tài khoản, mật khẩu hoặc chưa xác minh Email!");
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        SceneNavigator.goForgotPassword();
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        SceneNavigator.goRegister();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}