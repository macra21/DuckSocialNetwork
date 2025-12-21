package com.org.ddd.ui.user;

import com.org.ddd.domain.entities.User;
import com.org.ddd.service.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainWindowController {

    @FXML
    private BorderPane mainLayout;

    private User loggedInUser;
    private AuthService authService;
    private UserService userService;
    private FriendshipService friendshipService;
    private MessageService messageService;
    private FlockService flockService;
    private MessagesController messagesController;
    private FriendshipsController friendshipsController;
    private ExploreController exploreController;

    public void setup(AuthService authService, UserService userService, FriendshipService friendshipService, MessageService messageService, FlockService flockService) {
        this.authService = authService;
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.flockService = flockService;
        showLoginView();
    }

    @FXML
    public void initialize(){
        //
    }

    public void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/LoginView.fxml"));
            Parent loginView = loader.load();

            LoginController loginController = loader.getController();
            loginController.setup(authService, this);

            mainLayout.setCenter(loginView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRegisterView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/RegisterView.fxml"));
            Parent registerView = loader.load();

            RegisterController registerController = loader.getController();
            registerController.setup(authService, this);

            mainLayout.setCenter(registerView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAppView() {
        try {
            FXMLLoader appLoader = new FXMLLoader(getClass().getResource("/fxml/user/AppView.fxml"));
            appLoader.setController(this);
            Parent appView = appLoader.load();
            mainLayout.setLeft(appView);

            handleExploreView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onLoginSuccess(User user) {
        this.loggedInUser = user;
        showAppView();
    }

    @FXML
    public void handleMessagesView() {
        try {
            FXMLLoader messagesLoader = new FXMLLoader(getClass().getResource("/fxml/user/MessagesView.fxml"));
            Parent messagesView = messagesLoader.load();
            messagesController = messagesLoader.getController();
            messagesController.setup(loggedInUser, messageService, friendshipService, userService);
            mainLayout.setCenter(messagesView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleFriendshipsView() {
        try {
            FXMLLoader friendshipsLoader = new FXMLLoader(getClass().getResource("/fxml/user/FriendshipsView.fxml"));
            Parent friendshipsView = friendshipsLoader.load();
            friendshipsController = friendshipsLoader.getController();
            friendshipsController.setup(loggedInUser, friendshipService, userService);
            mainLayout.setCenter(friendshipsView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleExploreView() {
        try {
            FXMLLoader exploreLoader = new FXMLLoader(getClass().getResource("/fxml/user/ExploreView.fxml"));
            Parent exploreView = exploreLoader.load();
            exploreController = exploreLoader.getController();
            exploreController.setup(loggedInUser, friendshipService, userService);
            mainLayout.setCenter(exploreView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cleanup() {
        if (messagesController != null) {
            messagesController.cleanup();
        }
        if (friendshipsController != null) {
            friendshipsController.cleanup();
        }
    }
}
