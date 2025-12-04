package com.org.ddd.ui;

import com.org.ddd.domain.entities.Duck;
import com.org.ddd.domain.entities.DuckType;
import com.org.ddd.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DuckController {

    @FXML
    private ComboBox<DuckType> duckTypeComboBox;

    @FXML
    private TableView<Duck> duckTableView;

    @FXML
    private TableColumn<Duck, Long> idColumn;

    @FXML
    private TableColumn<Duck, String> usernameColumn;

    @FXML
    private TableColumn<Duck, String> duckTypeColumn;

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
        loadDuckTypes();
        loadDucks();
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        duckTypeColumn.setCellValueFactory(new PropertyValueFactory<>("duckType"));
    }

    private void loadDuckTypes() {
        ObservableList<DuckType> types = FXCollections.observableArrayList(DuckType.values());
        duckTypeComboBox.setItems(types);
    }

    private void loadDucks() {
        ObservableList<Duck> ducks = FXCollections.observableArrayList(
                StreamSupport.stream(userService.getAllUsers().spliterator(), false)
                        .filter(user -> user instanceof Duck)
                        .map(user -> (Duck) user)
                        .collect(Collectors.toList())
        );
        duckTableView.setItems(ducks);
    }

    @FXML
    private void handleDuckTypeChange() {
        DuckType selectedType = duckTypeComboBox.getValue();
        if (selectedType == null) {
            loadDucks();
            return;
        }
        ObservableList<Duck> ducks = FXCollections.observableArrayList(
                StreamSupport.stream(userService.getAllUsers().spliterator(), false)
                        .filter(user -> user instanceof Duck)
                        .map(user -> (Duck) user)
                        .filter(duck -> duck.getDuckType() == selectedType || selectedType == DuckType.FLYING_AND_SWIMMING)
                        .collect(Collectors.toList())
        );
        duckTableView.setItems(ducks);
    }
}
