package com.org.ddd.ui.user;

import com.org.ddd.domain.entities.Message;
import com.org.ddd.domain.entities.User;
import com.org.ddd.service.MessageService;
import com.org.ddd.service.UserService;
import com.org.ddd.utils.paging.Pageable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

public class MessagesController {

    private User loggedInUser;
    private User currentChatPartner;
    private UserService userService;
    private MessageService messageService;

    private final ObservableList<User> usersModel = FXCollections.observableArrayList();
    private final ObservableList<Message> messagesModel = FXCollections.observableArrayList();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private final Map<Long, Integer> unreadCounts = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    @FXML private ListView<User> usersListView;
    @FXML private ListView<Message> messagesListView;
    @FXML private Label chatPartnerLabel;
    @FXML private TextArea messageInputArea;
    @FXML private Button sendButton;

    public void initialize(User loggedInUser, UserService userService, MessageService messageService) {
        this.loggedInUser = loggedInUser;
        this.userService = userService;
        this.messageService = messageService;

        usersListView.setItems(usersModel);
        messagesListView.setItems(messagesModel);

        usersListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(user.getId() + ": " + user.getUsername());

                    int unread = unreadCounts.getOrDefault(user.getId(), 0);
                    
                    if (unread > 0) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
                        setText(getText() + " (" + unread + ")");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        messagesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                } else {
                    String timestamp = msg.getTimestamp().format(timeFormatter);
                    String senderName = msg.getSenderId().equals(loggedInUser.getId()) ? loggedInUser.getUsername() : currentChatPartner.getUsername();
                    setText(timestamp + ": " + senderName + ": " + msg.getContent());
                }
            }
        });

        loadUsersList();
        
        usersListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newSelection) -> {
            if (newSelection != null) {
                currentChatPartner = newSelection;
                chatPartnerLabel.setText("Chat with: " + currentChatPartner.getUsername());
                sendButton.setDisable(false);
                loadConversation();

                unreadCounts.remove(currentChatPartner.getId());
                usersListView.refresh();
            }
        });
        
        startPolling();
    }

    private void loadUsersList() {
        Iterable<User> allUsers = userService.getAllUsers();
        List<User> otherUsers = StreamSupport.stream(allUsers.spliterator(), false)
                .filter(user -> !user.getId().equals(loggedInUser.getId()))
                .toList();
        usersModel.setAll(otherUsers);
    }

    private void loadConversation() {
        if (currentChatPartner == null) return;

        Pageable pageable = new Pageable(0, 20);
        var conversationPage = messageService.getConversation(loggedInUser.getId(), currentChatPartner.getId(), pageable);
        
        List<Message> messages = new ArrayList<>();
        conversationPage.getElementsOnPage().forEach(msg -> {
            messages.add(msg);
            if (!msg.getSenderId().equals(loggedInUser.getId())) {
                messageService.markMessageAsRead(msg.getId(), loggedInUser.getId());
            }
        });
        Collections.reverse(messages);
        
        messagesModel.setAll(messages);
    }

    @FXML
    private void handleSendMessage() {
        String content = messageInputArea.getText();
        if (content.isBlank() || currentChatPartner == null) return;

        try {
            messageService.sendMessage(loggedInUser.getId(), List.of(currentChatPartner.getId()), content, null);
            messageInputArea.clear();
            loadConversation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startPolling() {
        Thread pollingThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(3000);

                    Map<Long, Integer> newUnreadCounts = messageService.getUnreadMessagesSummary(loggedInUser.getId());

                    if (!newUnreadCounts.equals(unreadCounts)) {
                        unreadCounts.clear();
                        unreadCounts.putAll(newUnreadCounts);

                        usersListView.refresh();

                        loadConversation();
                    }
                    
                } catch (Exception e) {
                    break;
                }
            }
        });
        pollingThread.setDaemon(true);

        pollingThread.start();
    }
    
    public void stopPolling() {
        running = false;
    }
}
