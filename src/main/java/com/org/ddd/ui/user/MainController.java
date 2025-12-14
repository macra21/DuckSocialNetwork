package com.org.ddd.ui.user;

import com.org.ddd.Main;
import com.org.ddd.domain.entities.User;
import com.org.ddd.service.MessageService;
import com.org.ddd.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MainController {

    private Main mainApp;
    private User loggedInUser;
    private UserService userService;
    private MessageService messageService;

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label usernameLabel;

    public void initialize(Main mainApp, User loggedInUser, UserService userService, MessageService messageService) {
        this.mainApp = mainApp;
        this.loggedInUser = loggedInUser;
        this.userService = userService;
        this.messageService = messageService;
        this.usernameLabel.setText("Logged in as: " + loggedInUser.getUsername());

        handleShowMessages();
    }

    @FXML
    private void handleShowMessages() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/MessagesView.fxml"));
            Pane messagesLayout = loader.load();

            MessagesController controller = loader.getController();
            controller.initialize(loggedInUser, userService, messageService);

            mainBorderPane.setCenter(messagesLayout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowFriendships() {
        // TODO: Load FriendshipsView.fxml into the center
        System.out.println("Showing Friendships...");
        mainBorderPane.setCenter(new Label("Friendships View - Not Implemented Yet"));
    }

    @FXML
    private void handleShowGroups() {
        // TODO: Load GroupsView.fxml into the center
        System.out.println("Showing Groups...");
        mainBorderPane.setCenter(new Label("Groups View - Not Implemented Yet"));
    }

    @FXML
    private void handleShowEvents() {
        // TODO: Load EventsView.fxml into the center
        System.out.println("Showing Events...");
        mainBorderPane.setCenter(new Label("Events View - Not Implemented Yet"));
    }

    @FXML
    private void handleLogout() {
        mainApp.showLoginScene();
    }
}
