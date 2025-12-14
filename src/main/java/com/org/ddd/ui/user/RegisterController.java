package com.org.ddd.ui.user;

import com.org.ddd.Main;
import com.org.ddd.domain.entities.Duck;
import com.org.ddd.domain.entities.DuckType;
import com.org.ddd.domain.entities.Person;
import com.org.ddd.service.AuthService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class RegisterController {

    private AuthService authService;
    private Main mainApp;

    // Common fields
    @FXML private ComboBox<String> userTypeComboBox;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    // Person fields
    @FXML private VBox personFieldsVBox;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField occupationField;
    @FXML private TextField empathyLevelField;

    // Duck fields
    @FXML private VBox duckFieldsVBox;
    @FXML private ComboBox<DuckType> duckTypeComboBox;
    @FXML private TextField speedField;
    @FXML private TextField resistanceField;

    @FXML
    public void initialize() {
        userTypeComboBox.setItems(FXCollections.observableArrayList("Person", "Duck"));
        duckTypeComboBox.setItems(FXCollections.observableArrayList(DuckType.values()));

        userTypeComboBox.getSelectionModel().select("Person");
        handleUserTypeChange();
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleUserTypeChange() {
        boolean isPerson = "Person".equals(userTypeComboBox.getValue());
        personFieldsVBox.setVisible(isPerson);
        personFieldsVBox.setManaged(isPerson);
        duckFieldsVBox.setVisible(!isPerson);
        duckFieldsVBox.setManaged(!isPerson);
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        try {
            String userType = userTypeComboBox.getValue();
            if ("Person".equals(userType)) {
                registerPerson(username, email, password);
            } else {
                registerDuck(username, email, password);
            }
            
            mainApp.showLoginScene();

        } catch (Exception e) {
            errorLabel.setText("Registration failed: " + e.getMessage());
        }
    }

    private void registerPerson(String username, String email, String password) throws Exception {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        LocalDate birthDate = birthDatePicker.getValue();
        String occupation = occupationField.getText();
        int empathyLevel = Integer.parseInt(empathyLevelField.getText());

        Person newUser = new Person(username, email, password, firstName, lastName, birthDate, occupation, empathyLevel);
        authService.register(newUser);
    }

    private void registerDuck(String username, String email, String password) throws Exception {
        DuckType duckType = duckTypeComboBox.getValue();
        double speed = Double.parseDouble(speedField.getText());
        double resistance = Double.parseDouble(resistanceField.getText());

        Duck newDuck = new Duck(username, email, password, speed, duckType, resistance);
        authService.register(newDuck);
    }

    @FXML
    private void handleGoToLogin() {
        mainApp.showLoginScene();
    }
}
