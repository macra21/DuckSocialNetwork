package com.org.ddd.ui.user;

import com.org.ddd.domain.entities.User;
import com.org.ddd.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorMessageLabel;

    private AuthService authService;
    private MainWindowController mainWindowController;

    public void setup(AuthService authService, MainWindowController mainWindowController){
        this.authService = authService;
        this.mainWindowController = mainWindowController;
    }

    @FXML
    public void initialize(){
        //
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            User user = authService.login(email, password);
            mainWindowController.onLoginSuccess(user);
            mainWindowController.showAppView();
        } catch (Exception e) {
            errorMessageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        mainWindowController.showRegisterView();
    }
}
