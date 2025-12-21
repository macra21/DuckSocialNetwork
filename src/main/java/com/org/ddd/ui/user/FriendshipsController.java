package com.org.ddd.ui.user;

import com.org.ddd.domain.entities.*;
import com.org.ddd.dto.FriendshipFilterDTO;
import com.org.ddd.service.FriendshipService;
import com.org.ddd.service.UserService;
import com.org.ddd.utils.paging.Page;
import com.org.ddd.utils.paging.Pageable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FriendshipsController {

    @FXML private ToggleButton pendingRequestsToggle;
    @FXML private ListView<Friendship> friendsListView;
    @FXML private VBox detailsContainer;
    @FXML private VBox userDetailsContainer;
    @FXML private VBox pendingActionsContainer;
    @FXML private VBox sentRequestActionsContainer;
    @FXML private Label usernameLabel;
    @FXML private Label userTypeLabel;
    @FXML private VBox personDetailsBox;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label occupationLabel;
    @FXML private Label birthDateLabel;
    @FXML private Label empathyLevelLabel;
    @FXML private VBox duckDetailsBox;
    @FXML private Label duckTypeLabel;
    @FXML private Label speedLabel;
    @FXML private Label resistanceLabel;
    @FXML private Label flockIdLabel;

    private User loggedInUser;
    private FriendshipService friendshipService;
    private UserService userService;
    private final ObservableList<Friendship> friendshipsModel = FXCollections.observableArrayList();
    private Thread pollingThread;
    private volatile boolean pollingRunning = true;

    public void setup(User loggedInUser, FriendshipService friendshipService, UserService userService) {
        this.loggedInUser = loggedInUser;
        this.friendshipService = friendshipService;
        this.userService = userService;

        friendsListView.setItems(friendshipsModel);
        friendsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Friendship item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTextFill(Color.BLACK);
                } else {
                    long friendId = item.getUserId1().equals(loggedInUser.getId()) ? item.getUserId2() : item.getUserId1();
                    User friend = userService.findUserById(friendId);
                    
                    String text = friend.getUsername();
                    if (item.getStatus() == FriendshipStatus.PENDING) {
                        if (item.getUserId2().equals(loggedInUser.getId())) {
                            text += " (Received)";
                            setTextFill(Color.GREEN);
                        } else {
                            text += " (Sent)";
                            setTextFill(Color.BLUE);
                        }
                    } else {
                        setTextFill(Color.BLACK);
                    }
                    setText(text);
                }
            }
        });

        friendsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                handleFriendshipSelected(newSelection);
            } else {
                clearDetails();
            }
        });

        loadFriendships();
        startPolling();
    }

    private void startPolling() {
        pollingThread = new Thread(() -> {
            while (pollingRunning) {
                try {
                    Platform.runLater(this::loadFriendships);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    pollingRunning = false;
                }
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    @FXML
    private void handleToggleChange(ActionEvent event) {
        loadFriendships();
    }

    private void loadFriendships() {
        FriendshipStatus selectedStatus = pendingRequestsToggle.isSelected() ? FriendshipStatus.PENDING : FriendshipStatus.APPROVED;

        new Thread(() -> {
            FriendshipFilterDTO filter = new FriendshipFilterDTO();
            filter.setInvolvedUser(loggedInUser.getId());
            filter.setStatus(selectedStatus);

            Page<Friendship> friendshipsPage = friendshipService.getFriendshipsOnPage(new Pageable(0, 1000), filter);
            List<Friendship> newFriendships = StreamSupport.stream(friendshipsPage.getElementsOnPage().spliterator(), false).collect(Collectors.toList());

            Platform.runLater(() -> {
                Friendship selectedItem = friendsListView.getSelectionModel().getSelectedItem();
                Long selectedId = (selectedItem != null) ? selectedItem.getId() : null;

                friendshipsModel.setAll(newFriendships);

                if (selectedId != null) {
                    for (Friendship f : friendshipsModel) {
                        if (f.getId().equals(selectedId)) {
                            friendsListView.getSelectionModel().select(f);
                            break;
                        }
                    }
                }
            });
        }).start();
    }

    private void handleFriendshipSelected(Friendship friendship) {
        clearDetails();
        FriendshipStatus status = friendship.getStatus();

        if (status == FriendshipStatus.PENDING) {
            if (friendship.getUserId2().equals(loggedInUser.getId())) {
                pendingActionsContainer.setVisible(true);
                pendingActionsContainer.setManaged(true);
            } else {
                sentRequestActionsContainer.setVisible(true);
                sentRequestActionsContainer.setManaged(true);
            }
        } else if (status == FriendshipStatus.APPROVED) {
            userDetailsContainer.setVisible(true);
            userDetailsContainer.setManaged(true);

            long friendId = friendship.getUserId1().equals(loggedInUser.getId()) ? friendship.getUserId2() : friendship.getUserId1();
            User friend = userService.findUserById(friendId);

            usernameLabel.setText(friend.getUsername());
            userTypeLabel.setText(friend.getClass().getSimpleName());

            if (friend instanceof Person) {
                personDetailsBox.setVisible(true);
                personDetailsBox.setManaged(true);
                duckDetailsBox.setVisible(false);
                duckDetailsBox.setManaged(false);

                Person person = (Person) friend;
                firstNameLabel.setText(person.getFirstName());
                lastNameLabel.setText(person.getLastName());
                occupationLabel.setText(person.getOccupation());
                birthDateLabel.setText(person.getBirthDate().toString());
                empathyLevelLabel.setText(String.valueOf(person.getEmpathyLevel()));
            } else if (friend instanceof Duck) {
                duckDetailsBox.setVisible(true);
                duckDetailsBox.setManaged(true);
                personDetailsBox.setVisible(false);
                personDetailsBox.setManaged(false);

                Duck duck = (Duck) friend;
                duckTypeLabel.setText(duck.getDuckType() != null ? duck.getDuckType().name() : "N/A");
                speedLabel.setText(String.valueOf(duck.getSpeed()));
                resistanceLabel.setText(String.valueOf(duck.getResistance()));
                flockIdLabel.setText(duck.getFlockId() != null ? duck.getFlockId().toString() : "None");
            }
        }
    }

    @FXML
    private void handleAcceptRequest() {
        Friendship selectedFriendship = friendsListView.getSelectionModel().getSelectedItem();
        if (selectedFriendship != null) {
            new Thread(() -> {
                friendshipService.acceptRequest(selectedFriendship.getId());
                Platform.runLater(this::loadFriendships);
            }).start();
        }
    }

    @FXML
    private void handleRejectRequest() {
        Friendship selectedFriendship = friendsListView.getSelectionModel().getSelectedItem();
        if (selectedFriendship != null) {
            new Thread(() -> {
                friendshipService.rejectRequest(selectedFriendship.getId());
                Platform.runLater(this::loadFriendships);
            }).start();
        }
    }

    @FXML
    private void handleCancelRequest() {
        Friendship selectedFriendship = friendsListView.getSelectionModel().getSelectedItem();
        if (selectedFriendship != null) {
            new Thread(() -> {
                friendshipService.deleteFriendship(selectedFriendship.getId());
                Platform.runLater(this::loadFriendships);
            }).start();
        }
    }

    private void clearDetails() {
        userDetailsContainer.setVisible(false);
        userDetailsContainer.setManaged(false);
        pendingActionsContainer.setVisible(false);
        pendingActionsContainer.setManaged(false);
        sentRequestActionsContainer.setVisible(false);
        sentRequestActionsContainer.setManaged(false);
    }

    public void cleanup() {
        pollingRunning = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }
}
