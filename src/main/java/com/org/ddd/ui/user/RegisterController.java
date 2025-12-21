package com.org.ddd.ui.user;

import com.org.ddd.domain.entities.Duck;
import com.org.ddd.domain.entities.DuckType;
import com.org.ddd.domain.entities.Person;
import com.org.ddd.domain.entities.User;
import com.org.ddd.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class RegisterController {
    // User Fields
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> userChangeComboBox;

    // Person Fields
    @FXML
    private VBox PersonVBox;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private DatePicker birthDateField;
    @FXML
    private TextField occupationField;
    @FXML
    private TextField empathyLevelField;

    // Duck Fields
    @FXML
    private VBox DuckVBox;
    @FXML
    private TextField speedField;
    @FXML
    private TextField resistanceField;
    @FXML
    private ComboBox<DuckType> duckTypeComboBox;

    @FXML
    private Label errorMessageLabel;

    @FXML
    private Button goToLoginButton;
    @FXML
    private Button registerButton;

    private MainWindowController mainWindowController;
    private AuthService authService;



    public void setup(AuthService authService, MainWindowController mainWindowController){
        this.authService = authService;
        this.mainWindowController = mainWindowController;

    }

    public void initialize(){
        userChangeComboBox.getItems().addAll("Person", "Duck");
        duckTypeComboBox.getItems().addAll(DuckType.values());

        userChangeComboBox.setValue("Person");
        duckTypeComboBox.setValue(DuckType.values()[0]);
    }

    @FXML
    private void handleUserChange(){
        if (userChangeComboBox.getValue().equals("Person")){
            PersonVBox.setVisible(true);
            PersonVBox.setManaged(true);
            DuckVBox.setVisible(false);
            DuckVBox.setManaged(false);
        }
        else if (userChangeComboBox.getValue().equals("Duck")){
            DuckVBox.setVisible(true);
            DuckVBox.setManaged(true);
            PersonVBox.setVisible(false);
            PersonVBox.setManaged(false);
        }
    }

    @FXML
    private void handleGoToLoginButton(){
        mainWindowController.showLoginView();
    }

    @FXML
    private void handleRegisterButton(){
        try {
            User user = null;
            if (userChangeComboBox.getValue().equals("Person")){
                Person person = new Person(
                        usernameField.getText(),
                        emailField.getText(),
                        passwordField.getText(),
                        firstNameField.getText(),
                        lastNameField.getText(),
                        birthDateField.getValue(),
                        occupationField.getText(),
                        Integer.parseInt(empathyLevelField.getText())
                );
                user = authService.register(person);
            } else if (userChangeComboBox.getValue().equals("Duck")){
                Duck duck = new Duck(
                        usernameField.getText(),
                        emailField.getText(),
                        passwordField.getText(),
                        Integer.parseInt(speedField.getText()),
                        Integer.parseInt(resistanceField.getText()),
                        (DuckType) duckTypeComboBox.getValue()
                );
                user = authService.register(duck);
            }
            mainWindowController.onLoginSuccess(user);
            mainWindowController.showAppView();
        } catch (Exception e) {
            if (e.getMessage().equals("For input string: \"\"")){
                errorMessageLabel.setText("Fill all fields!");
            }
            else{
                errorMessageLabel.setText(e.getMessage());
            }

            System.out.println(e.getMessage());
        }
    }
}
