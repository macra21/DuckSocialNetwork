package com.org.ddd;

import com.org.ddd.domain.entities.*;
import com.org.ddd.domain.validation.validators.*;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.PagingRepository;
import com.org.ddd.repository.dbRepositories.*;
import com.org.ddd.repository.converters.*;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.service.*;
import com.org.ddd.ui.DuckController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Properties props = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {

            if (input == null) {
                System.err.println("CRITICAL ERROR: 'config.properties' not found in src/main/resources");
                return;
            }

            props.load(input);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String dbUrl = props.getProperty("db.url");
        String dbUser = props.getProperty("db.user");
        String dbPass = props.getProperty("db.pass");

        try {
            Validator<User> userValidator = new UserValidator();
            Validator<Friendship> friendshipValidator = new FriendshipValidator();

            UserDBRepository userRepo = new UserDBRepository(dbUrl, dbUser, dbPass);

            AbstractRepository<Long, Friendship> friendshipRepo = new FriendshipDBRepository(
                    dbUrl, dbUser, dbPass
            );

            FriendshipService friendshipService = new FriendshipService(
                    friendshipRepo,
                    userRepo,
                    friendshipValidator
            );

            UserService userService = new UserService(
                    userRepo,
                    friendshipService,
                    userValidator
            );

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DuckView.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            primaryStage.setScene(scene);
            
            DuckController controller = loader.getController();
            controller.setUserService(userService);
            
            primaryStage.setTitle("Duck Social Network");
            primaryStage.show();

        } catch (RepositoryException e) {
            System.err.println("[CRITICAL BOOT ERROR]");
            System.err.println("Application failed to start. A required file or database might be missing/corrupt.");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[UNHANDLED CRITICAL ERROR]");
            e.printStackTrace();
        }
    }
}
