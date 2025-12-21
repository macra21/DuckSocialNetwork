package com.org.ddd.ui.user;

import com.org.ddd.domain.entities.Friendship;
import com.org.ddd.domain.entities.User;
import com.org.ddd.dto.UserFilterDTO;
import com.org.ddd.service.FriendshipService;
import com.org.ddd.service.UserService;
import com.org.ddd.utils.paging.Page;
import com.org.ddd.utils.paging.Pageable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ExploreController {

    @FXML private ListView<User> allUsersListView;
    @FXML private Button sendFriendRequestButton;

    private User loggedInUser;
    private FriendshipService friendshipService;
    private UserService userService;
    private final ObservableList<User> allUsersModel = FXCollections.observableArrayList();

    public void setup(User loggedInUser, FriendshipService friendshipService, UserService userService) {
        this.loggedInUser = loggedInUser;
        this.friendshipService = friendshipService;
        this.userService = userService;

        allUsersListView.setItems(allUsersModel);
        allUsersListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getUsername());
            }
        });

        allUsersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                Friendship existingFriendship = friendshipService.findExistingFriendship(loggedInUser.getId(), newSelection.getId());
                sendFriendRequestButton.setDisable(existingFriendship != null);
            } else {
                sendFriendRequestButton.setDisable(true);
            }
        });

        loadAllUsers();
    }

    private void loadAllUsers() {
        new Thread(() -> {
            Page<User> usersPage = userService.findAllOnPage(new Pageable(0, 1000), new UserFilterDTO());

            List<User> users = StreamSupport.stream(usersPage.getElementsOnPage().spliterator(), false)
                    .filter(user -> !user.getId().equals(loggedInUser.getId()))
                    .collect(Collectors.toList());

            Platform.runLater(() -> allUsersModel.setAll(users));
        }).start();
    }

    @FXML
    private void handleSendFriendRequest() {
        User selectedUser = allUsersListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            new Thread(() -> {
                friendshipService.sendRequest(loggedInUser.getId(), selectedUser.getId());
                Platform.runLater(this::loadAllUsers);
            }).start();
        }
    }
}
