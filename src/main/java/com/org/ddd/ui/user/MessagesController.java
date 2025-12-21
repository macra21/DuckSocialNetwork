package com.org.ddd.ui.user;

import com.org.ddd.domain.entities.Friendship;
import com.org.ddd.domain.entities.FriendshipStatus;
import com.org.ddd.domain.entities.Message;
import com.org.ddd.domain.entities.User;
import com.org.ddd.dto.FriendshipFilterDTO;
import com.org.ddd.service.FriendshipService;
import com.org.ddd.service.MessageService;
import com.org.ddd.service.UserService;
import com.org.ddd.utils.paging.Page;
import com.org.ddd.utils.paging.Pageable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessagesController {

    private User loggedInUser;
    private User currentFriend;
    private MessageService messageService;
    private FriendshipService friendshipService;
    private UserService userService;
    private Message repliedToMessage = null;

    private final ObservableList<User> friendsModel = FXCollections.observableArrayList();
    private final ObservableList<Message> messagesModel = FXCollections.observableArrayList();
    private final Map<Long, User> userCache = new HashMap<>();
    private final Map<Long, Message> messageCache = new HashMap<>();

    private boolean isLoading = false;
    private Thread pollingThread;
    private volatile boolean pollingRunning = true;

    @FXML private ListView<User> friendsListView;
    @FXML private ListView<Message> messagesListView;
    @FXML private TextField messageTextField;
    @FXML private VBox replyContainer;
    @FXML private Label replyToLabel;

    public void setup(User loggedInUser, MessageService messageService, FriendshipService friendshipService, UserService userService) {
        this.loggedInUser = loggedInUser;
        this.messageService = messageService;
        this.friendshipService = friendshipService;
        this.userService = userService;

        userCache.put(loggedInUser.getId(), loggedInUser);
        loadFriends();
        startPolling();
    }

    private void startPolling() {
        pollingThread = new Thread(() -> {
            while (pollingRunning) {
                try {
                    if (currentFriend != null) {
                        Map<Long, Integer> unreadSummary = messageService.getUnreadMessagesSummary(loggedInUser.getId());
                        if (unreadSummary.containsKey(currentFriend.getId())) {
                            Platform.runLater(this::loadMessages);
                        }
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    pollingRunning = false;
                }
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    @FXML
    public void initialize() {
        friendsListView.setItems(friendsModel);
        messagesListView.setItems(messagesModel);

        friendsListView.setCellFactory(_ -> new TextFieldListCell<>(new StringConverter<>() {
            @Override public String toString(User u) { return u.getUsername(); }
            @Override public User fromString(String s) { return null; }
        }));

        friendsListView.getSelectionModel().selectedItemProperty().addListener((_, _, newVal) -> {
            if (newVal == null) return;
            currentFriend = newVal;
            userCache.put(currentFriend.getId(), currentFriend);
            loadMessages();
            handleCancelReply();
        });

        messagesListView.setCellFactory(lv -> {
            ListCell<Message> cell = new ListCell<>() {
                @Override
                protected void updateItem(Message message, boolean empty) {
                    super.updateItem(message, empty);
                    if (empty || message == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        User sender = userCache.computeIfAbsent(message.getSenderId(), id -> userService.findUserById(id));
                        String timestamp = message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm"));

                        String replyText = "";
                        if (message.getReplyId() != null) {
                            Message repliedMsg = messageCache.get(message.getReplyId());
                            if (repliedMsg != null) {
                                User repliedSender = userCache.computeIfAbsent(repliedMsg.getSenderId(), id -> userService.findUserById(id));
                                replyText = "[reply to " + repliedSender.getUsername() + ": " + repliedMsg.getContent() + "] ";
                            }
                        }

                        setText(replyText + sender.getUsername() + " (" + timestamp + "): " + message.getContent());
                        setWrapText(true);
                    }
                }
            };

            ContextMenu contextMenu = new ContextMenu();
            MenuItem replyMenuItem = new MenuItem("Reply");
            replyMenuItem.setOnAction(event -> {
                Message message = cell.getItem();
                setReplyTo(message);
            });
            contextMenu.getItems().add(replyMenuItem);

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
    }

    private void loadFriends() {
        new Thread(() -> {
            FriendshipFilterDTO filter = new FriendshipFilterDTO();
            filter.setInvolvedUser(loggedInUser.getId());
            filter.setStatus(FriendshipStatus.APPROVED);
            Page<Friendship> friendshipsPage = friendshipService.findFriendshipsForUser(loggedInUser.getId(), new Pageable(0, 1000), filter);

            List<User> friends = StreamSupport.stream(friendshipsPage.getElementsOnPage().spliterator(), false)
                    .map(f -> {
                        Long friendId = f.getUserId1().equals(loggedInUser.getId()) ? f.getUserId2() : f.getUserId1();
                        return userService.findUserById(friendId);
                    })
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                friendsModel.setAll(friends);
                if (!friendsModel.isEmpty()) {
                    friendsListView.getSelectionModel().selectFirst();
                }
            });
        }).start();
    }

    private void loadMessages() {
        if (isLoading || currentFriend == null) return;
        isLoading = true;

        new Thread(() -> {
            var messages = messageService.getConversation(loggedInUser.getId(), currentFriend.getId(), new Pageable(0, 1000)).getElementsOnPage();
            StreamSupport.stream(messages.spliterator(), false).forEach(m -> messageCache.put(m.getId(), m));

            List<Message> messageList = StreamSupport.stream(messages.spliterator(), false)
                    .sorted(Comparator.comparing(Message::getTimestamp))
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                messagesModel.setAll(messageList);
                if (!messagesModel.isEmpty())
                    messagesListView.scrollTo(messagesModel.size() - 1);

                if (!messageList.isEmpty()) {
                    new Thread(() -> {
                        for (Message m : messageList) {
                            if (!m.getSenderId().equals(loggedInUser.getId())) {
                                messageService.markMessageAsRead(m.getId(), loggedInUser.getId());
                            }
                        }
                    }).start();
                }
                
                isLoading = false;
            });
        }).start();
    }

    @FXML
    private void handleSendMessage() {
        String text = messageTextField.getText();
        if (text.isEmpty() || currentFriend == null) return;

        Long replyId = (repliedToMessage != null) ? repliedToMessage.getId() : null;

        new Thread(() -> {
            Message message = new Message(loggedInUser.getId(), List.of(currentFriend.getId()), text, replyId);
            messageService.sendMessage(message);
            Platform.runLater(this::loadMessages);
        }).start();
        
        messageTextField.clear();
        handleCancelReply();
    }

    @FXML
    private void handleCancelReply() {
        repliedToMessage = null;
        replyContainer.setVisible(false);
        replyContainer.setManaged(false);
        messagesListView.getSelectionModel().clearSelection();
    }

    private void setReplyTo(Message message) {
        repliedToMessage = message;
        replyToLabel.setText(formatForReply(repliedToMessage));
        replyContainer.setVisible(true);
        replyContainer.setManaged(true);
    }

    private String formatForReply(Message m) {
        User sender = userCache.computeIfAbsent(m.getSenderId(), id -> userService.findUserById(id));
        String content = m.getContent().length() > 30 ? m.getContent().substring(0, 30) + "..." : m.getContent();
        return sender.getUsername() + ": " + content;
    }

    public void cleanup() {
        pollingRunning = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }
}
