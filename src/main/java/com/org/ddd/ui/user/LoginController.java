package com.org.ddd.ui.user;

import com.org.ddd.Main;
import com.org.ddd.domain.entities.User;
import com.org.ddd.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    private AuthService authService;
    private Main mainApp;

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            User loggedInUser = authService.login(email, password);
            mainApp.showMainAppScene(loggedInUser);
        } catch (Exception e) {
            errorLabel.setText("Login failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToRegister() {
        mainApp.showRegisterScene();
    }
}
