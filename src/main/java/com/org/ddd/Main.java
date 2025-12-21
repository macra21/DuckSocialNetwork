package com.org.ddd;

import com.org.ddd.domain.entities.Flock;
import com.org.ddd.domain.entities.Friendship;
import com.org.ddd.domain.entities.Message;
import com.org.ddd.domain.entities.User;
import com.org.ddd.domain.validation.validators.*;
import com.org.ddd.repository.dbRepositories.FlockDBRepository;
import com.org.ddd.repository.dbRepositories.FriendshipDBRepository;
import com.org.ddd.repository.dbRepositories.MessageDBRepository;
import com.org.ddd.repository.dbRepositories.UserDBRepository;
import com.org.ddd.service.*;
import com.org.ddd.ui.user.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 600;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Repositories
        UserDBRepository userRepo = new UserDBRepository();
        FriendshipDBRepository friendshipRepo = new FriendshipDBRepository();
        MessageDBRepository messageRepo = new MessageDBRepository();
        FlockDBRepository flockRepo = new FlockDBRepository();

        // Validators
        Validator<User> userValidator = new UserValidator();
        Validator<Friendship> friendshipValidator = new FriendshipValidator();
        Validator<Message> messageValidator = new MessageValidator();
        Validator<Flock> flockValidator = new FlockValidator();

        // Services
        AuthService authService = new AuthService(userRepo, userValidator);
        FriendshipService friendshipService = new FriendshipService(friendshipRepo, userRepo, friendshipValidator);
        UserService userService = new UserService(userRepo, friendshipService, userValidator);
        MessageService messageService = new MessageService(messageRepo, userRepo, messageValidator);
        FlockService flockService = new FlockService(flockRepo, userRepo, flockValidator);

        showMainScene(primaryStage, authService, userService, friendshipService, messageService, flockService);
    }

    private void showMainScene(Stage primaryStage, AuthService authService, UserService userService, FriendshipService friendshipService, MessageService messageService, FlockService flockService) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/MainWindow.fxml"));
            Parent root = loader.load();

            MainWindowController controller = loader.getController();
            controller.setup(authService, userService, friendshipService, messageService, flockService);

            primaryStage.setTitle("Duck Social Network");
            primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
            primaryStage.setOnCloseRequest(event -> controller.cleanup());
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
