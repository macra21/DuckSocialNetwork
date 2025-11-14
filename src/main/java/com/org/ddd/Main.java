package com.org.ddd;

import com.org.ddd.domain.entities.*;
import com.org.ddd.domain.validation.validators.*;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.FileRepository;
import com.org.ddd.repository.dbRepositories.FriendshipDBRepository;
import com.org.ddd.repository.dbRepositories.UserDBRepository;
import com.org.ddd.repository.converters.*;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.service.*;
import com.org.ddd.service.event.EventService;
import com.org.ddd.ui.ConsoleUI;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import java.util.Properties;

public class Main {

    public static void main(String[] args) {

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
        // sneaky
        String dbUrl = props.getProperty("db.url");
        String dbUser = props.getProperty("db.user");
        String dbPass = props.getProperty("db.pass");

        String dataFolderPath = "src\\main\\java\\com\\org\\ddd\\data\\";

        try {
            Validator<User> userValidator = new UserValidator();
            Validator<Friendship> friendshipValidator = new FriendshipValidator();
            Validator<Flock> flockValidator = new FlockValidator();
            Validator<Message> messageValidator = new MessageValidator();
            //Validator<Event> eventValidator = new EventValidator(); //not used yet:((

            //EntityConverter<Friendship> friendshipConverter = new FriendshipConverter(); //used for the filerepo
            EntityConverter<Flock> flockConverter = new FlockConverter();
            EntityConverter<Event> eventConverter = new EventConverter();
            EntityConverter<Message> messageConverter = new MessageConverter();

            AbstractRepository<Long, User> userRepo = new UserDBRepository(
                    dbUrl, dbUser, dbPass
            );

            AbstractRepository<Long, Friendship> friendshipRepo = new FriendshipDBRepository(
                    dbUrl, dbUser, dbPass
            );

            AbstractRepository<Long, Flock> flockRepo = new FileRepository<>(
                    Paths.get(dataFolderPath + "flocks.txt"),
                    flockConverter
            );
            AbstractRepository<Long, Event> eventRepo = new FileRepository<>(
                    Paths.get(dataFolderPath + "events.txt"),
                    eventConverter
            );
            AbstractRepository<Long, Message> messageRepo = new FileRepository<>(
                    Paths.get(dataFolderPath + "messages.txt"),
                    messageConverter
            );

            MessageService messageService = new MessageService(
                    messageRepo,
                    userRepo,
                    messageValidator
            );

            FriendshipService friendshipService = new FriendshipService(
                    friendshipRepo,
                    userRepo,
                    friendshipValidator
            );

            FlockService flockService = new FlockService(
                    flockRepo,
                    userRepo,
                    flockValidator
            );

            AuthService authService = new AuthService(
                    userRepo,
                    userValidator
            );

            EventService eventService = new EventService(
                    eventRepo,
                    userRepo
            );

            UserService userService = new UserService(
                    userRepo,
                    friendshipService,
                    userValidator
            );

            ConsoleUI console = new ConsoleUI(
                    userService,
                    friendshipService,
                    authService,
                    flockService,
                    eventService,
                    messageService
            );

            console.run();

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
