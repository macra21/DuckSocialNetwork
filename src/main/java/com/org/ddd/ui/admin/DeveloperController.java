package com.org.ddd.ui.admin;

import com.org.ddd.domain.entities.*;
import com.org.ddd.dto.UserFilterDTO;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.service.FriendshipService;
import com.org.ddd.service.UserService;
import com.org.ddd.utils.paging.Page;
import com.org.ddd.utils.paging.Pageable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DeveloperController {

    private UserService userService;
    private FriendshipService friendshipService;
    private final ObservableList<User> userModel = FXCollections.observableArrayList();
    private final ObservableList<Friendship> friendshipModel = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 20;
    private String currentView = "USERS";
    private String currentAction = "";

    @FXML private BorderPane mainBorderPane;

    // User
    @FXML private ComboBox<String> userTypeComboBox;
    @FXML private ComboBox<DuckType> duckTypeComboBox;
    @FXML private TableView<User> userTableView;
    @FXML private TableColumn<User, String> column1;
    @FXML private TableColumn<User, String> column2;
    @FXML private TableColumn<User, String> column3;
    @FXML private TableColumn<User, String> column4;
    @FXML private TableColumn<User, String> column5;
    @FXML private TableColumn<User, String> column6;
    @FXML private Pagination pagination;
    @FXML private VBox addUserFormVBox;
    @FXML private VBox deleteUserFormVBox;
    @FXML private VBox personFieldsVBox;
    @FXML private VBox duckFieldsVBox;
    @FXML private ComboBox<String> addUserTypeComboBox;
    @FXML private ComboBox<DuckType> addDuckTypeComboBox;
    @FXML private TextField addUsernameField;
    @FXML private TextField addEmailField;
    @FXML private TextField addPasswordField;
    @FXML private TextField addFirstNameField;
    @FXML private TextField addLastNameField;
    @FXML private TextField addBirthDateField;
    @FXML private TextField addOccupationField;
    @FXML private TextField addEmpathyLevelField;
    @FXML private TextField addSpeedField;
    @FXML private TextField addResistanceField;
    @FXML private TextField deleteUserIdField;

    // Friendship
    @FXML private TableView<Friendship> friendshipTableView;
    @FXML private TableColumn<Friendship, Long> friendshipIdColumn;
    @FXML private TableColumn<Friendship, Long> friendshipUser1Column;
    @FXML private TableColumn<Friendship, Long> friendshipUser2Column;
    @FXML private TableColumn<Friendship, String> friendshipDateColumn;
    @FXML private VBox addFriendshipFormVBox;
    @FXML private VBox deleteFriendshipFormVBox;
    @FXML private TextField addFriendshipUser1IdField;
    @FXML private TextField addFriendshipUser2IdField;
    @FXML private TextField deleteFriendshipUser1IdField;
    @FXML private TextField deleteFriendshipUser2IdField;
    @FXML private Label communityResultLabel;


    // Common
    @FXML private Button commitButton;
    @FXML private Label errorLabel;



    public void initialize(UserService userService, FriendshipService friendshipService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        handleShowUsers();
    }

    private void initializeUsersView() {
        column1.setCellValueFactory(new PropertyValueFactory<>("id"));
        column2.setCellValueFactory(new PropertyValueFactory<>("username"));
        column3.setCellValueFactory(new PropertyValueFactory<>("email"));
        userTableView.setItems(userModel);

        userTypeComboBox.setItems(FXCollections.observableArrayList("ALL", "PERSON", "DUCK"));
        userTypeComboBox.getSelectionModel().selectFirst();
        duckTypeComboBox.setItems(FXCollections.observableArrayList(DuckType.values()));
        pagination.setMaxPageIndicatorCount(5);
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> loadUsersPage(newIndex.intValue()));

        addUserTypeComboBox.setItems(FXCollections.observableArrayList("Person", "Duck"));
        addDuckTypeComboBox.setItems(FXCollections.observableArrayList(DuckType.values()));
        addUserTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isPerson = newVal.equals("Person");
            personFieldsVBox.setVisible(isPerson);
            personFieldsVBox.setManaged(isPerson);
            duckFieldsVBox.setVisible(!isPerson);
            duckFieldsVBox.setManaged(!isPerson);
        });

        loadUsersPage(0);
    }

    private void initializeFriendshipsView() {
        friendshipIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        friendshipUser1Column.setCellValueFactory(new PropertyValueFactory<>("userId1"));
        friendshipUser2Column.setCellValueFactory(new PropertyValueFactory<>("userId2"));
        friendshipDateColumn.setCellValueFactory(new PropertyValueFactory<>("friendsFrom"));
        friendshipTableView.setItems(friendshipModel);
        loadFriendships();
    }


    @FXML
    public void handleShowUsers() {
        try {
            currentView = "USERS";
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UsersView.fxml"));
            loader.setController(this);
            BorderPane usersView = loader.load();

            mainBorderPane.setCenter(usersView.getCenter());
            mainBorderPane.setRight(usersView.getRight());

            initializeUsersView();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleShowFriendships() {
        try {
            currentView = "FRIENDSHIPS";
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/FriendshipsView.fxml"));
            loader.setController(this);
            BorderPane friendshipsView = loader.load();

            mainBorderPane.setCenter(friendshipsView.getCenter());
            mainBorderPane.setRight(friendshipsView.getRight());

            initializeFriendshipsView();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUsersPage(int pageIndex) {
        UserFilterDTO filter = new UserFilterDTO();
        String userType = userTypeComboBox.getValue();

        if (userType.equals("PERSON")) {
            filter.setUserClass(Optional.of(Person.class));
        } else if (userType.equals("DUCK")) {
            filter.setUserClass(Optional.of(Duck.class));
            filter.setDuckType(Optional.ofNullable(duckTypeComboBox.getValue()));
        }

        Pageable pageable = new Pageable(pageIndex, PAGE_SIZE);
        Page<User> page = userService.findAllOnPage(pageable, filter);

        userModel.setAll(StreamSupport.stream(page.getElementsOnPage().spliterator(), false)
                .collect(Collectors.toList()));

        int totalElements = page.getTotalNumberOfElements();
        int pageCount = (int) Math.ceil((double) totalElements / PAGE_SIZE);
        if (pageCount == 0 && totalElements > 0) {
            pageCount = 1;
        }
        if (pagination.getPageCount() != pageCount) { pagination.setPageCount(pageCount); }

        updateTableColumns(userType);
    }

    private void loadFriendships() {
        friendshipModel.setAll(
                StreamSupport.stream(friendshipService.getAllFriendships().spliterator(), false)
                        .collect(Collectors.toList())
        );
    }


    private void updateTableColumns(String userType) {
        if (userType.equals("PERSON")) {
            column4.setVisible(true);
            column5.setVisible(true);
            column6.setVisible(true);

            column4.setText("First Name");
            column4.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            column5.setText("Last Name");
            column5.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            column6.setText("Birth Date");
            column6.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        } else if (userType.equals("DUCK")) {
            column4.setVisible(true);
            column5.setVisible(true);
            column6.setVisible(true);

            column4.setText("Duck Type");
            column4.setCellValueFactory(new PropertyValueFactory<>("duckType"));
            column5.setText("Speed");
            column5.setCellValueFactory(new PropertyValueFactory<>("speed"));
            column6.setText("Resistance");
            column6.setCellValueFactory(new PropertyValueFactory<>("resistance"));
        } else {
            column4.setVisible(false);
            column5.setVisible(false);
            column6.setVisible(false);
        }
    }

    @FXML
    private void handleAdd() {
        currentAction = "ADD";
        if (currentView.equals("USERS")) {
            addUserFormVBox.setVisible(true);
            addUserFormVBox.setManaged(true);
            deleteUserFormVBox.setVisible(false);
            deleteUserFormVBox.setManaged(false);
            addUserTypeComboBox.getSelectionModel().select("Person");
        } else if (currentView.equals("FRIENDSHIPS")) {
            addFriendshipFormVBox.setVisible(true);
            addFriendshipFormVBox.setManaged(true);
            deleteFriendshipFormVBox.setVisible(false);
            deleteFriendshipFormVBox.setManaged(false);
        }
        commitButton.setVisible(true);
        commitButton.setManaged(true);
        errorLabel.setText("");
    }

    @FXML
    private void handleDelete() {
        currentAction = "DELETE";
        if (currentView.equals("USERS")) {
            deleteUserFormVBox.setVisible(true);
            deleteUserFormVBox.setManaged(true);
            addUserFormVBox.setVisible(false);
            addUserFormVBox.setManaged(false);
        } else if (currentView.equals("FRIENDSHIPS")) {
            deleteFriendshipFormVBox.setVisible(true);
            deleteFriendshipFormVBox.setManaged(true);
            addFriendshipFormVBox.setVisible(false);
            addFriendshipFormVBox.setManaged(false);
        }
        commitButton.setVisible(true);
        commitButton.setManaged(true);
        errorLabel.setText("");
    }

    @FXML
    private void handleCommitAction() {
        errorLabel.setText("");
        //System.out.println(currentAction);
        //System.out.println(currentView);
        try {
            if (currentAction.equals("ADD")) {
                if (currentView.equals("USERS")) {
                    executeAddUser();
                    errorLabel.setText("User added successfully!");
                } else if (currentView.equals("FRIENDSHIPS")) {
                    executeAddFriendship();
                    errorLabel.setText("Friendship added successfully!");
                }
            } else if (currentAction.equals("DELETE")) {
                if (currentView.equals("USERS")) {
                    //System.out.println("Sterg");
                    executeDeleteUser();
                    errorLabel.setText("User deleted successfully!");
                } else if (currentView.equals("FRIENDSHIPS")) {
                    executeDeleteFriendship();
                    errorLabel.setText("Friendship deleted successfully!");
                }
            }
        } catch (RepositoryException | DateTimeParseException | NumberFormatException e) {
            errorLabel.setText("Error: " + e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("An unexpected error occurred: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private void executeAddUser() throws RepositoryException {
        String type = addUserTypeComboBox.getValue();
        String username = addUsernameField.getText();
        String email = addEmailField.getText();
        String password = addPasswordField.getText();

        if (type.equals("Person")) {
            String firstName = addFirstNameField.getText();
            String lastName = addLastNameField.getText();
            LocalDate birthDate = LocalDate.parse(addBirthDateField.getText());
            String occupation = addOccupationField.getText();
            int empathyLevel = Integer.parseInt(addEmpathyLevelField.getText());
            Person person = new Person(username, email, password, firstName, lastName, birthDate, occupation, empathyLevel);
            userService.addUser(person);
        } else if (type.equals("Duck")) {
            DuckType duckType = addDuckTypeComboBox.getValue();
            double speed = Double.parseDouble(addSpeedField.getText());
            double resistance = Double.parseDouble(addResistanceField.getText());
            Duck duck = new Duck(username, email, password, speed, duckType, resistance);
            userService.addUser(duck);
        }
        loadUsersPage(pagination.getCurrentPageIndex());
    }

    private void executeDeleteUser() throws RepositoryException {
        Long id = Long.parseLong(deleteUserIdField.getText());
        System.out.println(deleteUserIdField.getText());
        userService.deleteUser(id);
        //System.out.printf("sters");
        loadUsersPage(pagination.getCurrentPageIndex());
    }

    private void executeAddFriendship() throws RepositoryException {
        Long userId1 = Long.parseLong(addFriendshipUser1IdField.getText());
        Long userId2 = Long.parseLong(addFriendshipUser2IdField.getText());
        friendshipService.addFriendship(userId1, userId2);
        loadFriendships();
    }

    private void executeDeleteFriendship() throws RepositoryException {
        Long userId1 = Long.parseLong(deleteFriendshipUser1IdField.getText());
        Long userId2 = Long.parseLong(deleteFriendshipUser2IdField.getText());
        friendshipService.deleteFriendship(userId1, userId2);
        loadFriendships();
    }


    @FXML
    private void handleUserTypeChange() {
        String selectedType = userTypeComboBox.getValue();
        boolean isDuck = selectedType.equals("DUCK");
        duckTypeComboBox.setVisible(isDuck);
        duckTypeComboBox.setManaged(isDuck);
        if (!isDuck) {
            duckTypeComboBox.getSelectionModel().clearSelection();
        }
        pagination.setCurrentPageIndex(0);
        loadUsersPage(0);
    }

    @FXML
    private void handleDuckTypeChange() {
        pagination.setCurrentPageIndex(0);
        loadUsersPage(0);
    }

    @FXML
    private void handleShowNumberOfCommunities() {
        int numberOfCommunities = friendshipService.findNumberOfCommunities();
        communityResultLabel.setText("Number of communities: " + numberOfCommunities);
    }

    @FXML
    private void handleShowMostSociableCommunity() {
        Iterable<User> mostSociableCommunity = friendshipService.getMostSociableCommunity();
        String communityUsernames = StreamSupport.stream(mostSociableCommunity.spliterator(), false)
                .map(User::getUsername)
                .collect(Collectors.joining(", "));
        communityResultLabel.setText("Most sociable community: " + communityUsernames);
    }
}
