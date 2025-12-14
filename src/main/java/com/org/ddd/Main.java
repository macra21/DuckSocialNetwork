package com.org.ddd;

import com.org.ddd.domain.entities.Friendship;
import com.org.ddd.domain.entities.Message;
import com.org.ddd.domain.entities.User;
import com.org.ddd.domain.validation.validators.FriendshipValidator;
import com.org.ddd.domain.validation.validators.MessageValidator;
import com.org.ddd.domain.validation.validators.UserValidator;
import com.org.ddd.domain.validation.validators.Validator;
import com.org.ddd.repository.dbRepositories.FriendshipDBRepository;
import com.org.ddd.repository.dbRepositories.MessageDBRepository;
import com.org.ddd.repository.dbRepositories.UserDBRepository;
import com.org.ddd.service.AuthService;
import com.org.ddd.service.FriendshipService;
import com.org.ddd.service.MessageService;
import com.org.ddd.service.UserService;
import com.org.ddd.ui.user.LoginController;
import com.org.ddd.ui.user.MainController;
import com.org.ddd.ui.user.RegisterController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main extends Application {

    private Stage primaryStage;
    
    // Services
    private AuthService authService;
    private UserService userService;
    private FriendshipService friendshipService;
    private MessageService messageService;

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 600;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        Properties props = loadProperties();
        if (props == null) return;

        initializeServices(props);
        
        showLoginScene();
    }

    private void initializeServices(Properties props) {
        String dbUrl = props.getProperty("db.url");
        String dbUser = props.getProperty("db.user");
        String dbPass = props.getProperty("db.pass");

        // Repositories
        UserDBRepository userRepo = new UserDBRepository(dbUrl, dbUser, dbPass);
        FriendshipDBRepository friendshipRepo = new FriendshipDBRepository(dbUrl, dbUser, dbPass);
        MessageDBRepository messageRepo = new MessageDBRepository(dbUrl, dbUser, dbPass);

        // Validators
        Validator<User> userValidator = new UserValidator();
        Validator<Friendship> friendshipValidator = new FriendshipValidator();
        Validator<Message> messageValidator = new MessageValidator();

        // Services
        this.authService = new AuthService(userRepo, userValidator);
        this.friendshipService = new FriendshipService(friendshipRepo, userRepo, friendshipValidator);
        this.userService = new UserService(userRepo, friendshipService, userValidator);
        this.messageService = new MessageService(messageRepo, userRepo, messageValidator);
    }

    public void showLoginScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/LoginView.fxml"));
            Parent layout = loader.load();

            LoginController controller = loader.getController();
            controller.setAuthService(authService);
            controller.setMainApp(this); 

            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(layout, WINDOW_WIDTH, WINDOW_HEIGHT));
            } else {
                primaryStage.getScene().setRoot(layout);
            }
            
            primaryStage.setTitle("Login");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRegisterScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/RegisterView.fxml"));
            Parent layout = loader.load();

            RegisterController controller = loader.getController();
            controller.setAuthService(authService);
            controller.setMainApp(this);

            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(layout, WINDOW_WIDTH, WINDOW_HEIGHT));
            } else {
                primaryStage.getScene().setRoot(layout);
            }

            primaryStage.setTitle("Register");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void showMainAppScene(User loggedInUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/MainView.fxml"));
            Parent layout = loader.load();

            MainController controller = loader.getController();
            controller.initialize(this, loggedInUser, userService, messageService);

            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(layout, WINDOW_WIDTH, WINDOW_HEIGHT));
            } else {
                primaryStage.getScene().setRoot(layout);
            }

            primaryStage.setTitle("Duck Social Network");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("CRITICAL ERROR: 'config.properties' not found.");
                return null;
            }
            props.load(input);
            return props;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
