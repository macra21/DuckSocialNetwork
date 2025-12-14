// this console interface is no longer compatible with the app
/*

package com.org.ddd.ui;

import com.org.ddd.domain.entities.*;
import com.org.ddd.domain.validation.exceptions.ValidationException;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.service.*;
import com.org.ddd.service.event.EventService;
import com.org.ddd.service.exceptions.ServiceException;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private final UserService userService;
    private final FriendshipService friendshipService;
    private final AuthService authService;
    private final FlockService flockService;
    private final EventService eventService;
    private final MessageService messageService;

    private final Scanner scanner;
    private User currentUser = null;

    public ConsoleUI(UserService userService, FriendshipService friendshipService, AuthService authService,
                     FlockService flockService, EventService eventService, MessageService messageService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.authService = authService;
        this.flockService = flockService;
        this.eventService = eventService;
        this.messageService = messageService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        while (true) {
            if (this.currentUser == null) {
                runLoggedOutMenu();
            } else {
                runLoggedInMenu();
            }
        }
    }

    private void runLoggedOutMenu() {
        System.out.println("\n=== DUCK SOCIAL NETWORK ===");
        System.out.println("--- You are logged out ---");
        System.out.println("1. Login (by Email)");
        System.out.println("2. Register new Account");
        System.out.println("3. View Network Statistics");
        System.out.println("0. Exit");

        String command = readString("Command: ");
        switch (command) {
            case "1":
                handleLogin();
                break;
            case "2":
                handleRegister();
                break;
            case "3":
                handleNetworkStatistics();
                break;
            case "0":
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid command.");
        }
    }

    private void runLoggedInMenu() {
        System.out.println("\n=== DUCK SOCIAL NETWORK ===");
        System.out.println("--- Logged in as: " + currentUser.getUsername() + " (" + currentUser.getClass().getSimpleName() + ") ---");
        System.out.println("1. Logout");
        System.out.println("2. User Management");
        System.out.println("3. Friendship Management");
        System.out.println("4. Messages");
        System.out.println("5. Flocks (Carduri)");
        System.out.println("6. Events & Races");
        System.out.println("7. Network Analysis");

        String command = readString("Command: ");
        switch (command) {
            case "1":
                handleLogout();
                break;
            case "2":
                runUserManagementMenu();
                break;
            case "3":
                runFriendshipManagementMenu();
                break;
            case "4":
                runMessagesMenu();
                break;
            case "5":
                runFlocksMenu();
                break;
            case "6":
                runEventsMenu();
                break;
            case "7":
                runNetworkAnalysisMenu();
                break;
            default:
                System.out.println("Invalid command.");
        }
    }

    // === AUTH METHODS ===
    private void handleLogin() {
        try {
            String email = readString("Email: ");
            String password = readString("Password: ");

            this.currentUser = authService.login(email, password);
            System.out.println("Welcome, " + this.currentUser.getUsername() + "!");

        } catch (ValidationException | RepositoryException e) {
            System.out.println("Login failed: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Login failed: %s" + e.getMessage());
        }
    }

    private void handleLogout() {
        System.out.println("Goodbye, " + this.currentUser.getUsername() + "!");
        this.currentUser = null;
    }

    private void handleRegister() {
        try {
            String type = "";
            while (!type.equals("1") && !type.equals("2")) {
                type = readString("Are you a (1) Person or (2) Duck? ");
            }

            String username = readString("Username: ");
            String email = readString("Email: ");
            String password = readString("Password: ");

            User newUser;
            if (type.equals("1")) {
                newUser = handleRegisterPerson(username, email, password);
            } else {
                newUser = handleRegisterDuck(username, email, password);
            }

            authService.register(newUser);
            System.out.println("Account created successfully for " + newUser.getUsername() + "! You can now log in.");

        } catch (ValidationException | RepositoryException e) {
            System.out.println("Failed to create user: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    // === USER MANAGEMENT MENU ===
    private void runUserManagementMenu() {
        System.out.println("\n--- User Management ---");
        System.out.println("1. List all Users");
        System.out.println("2. Search Users");
        System.out.println("3. View User Profile");
        System.out.println("4. Remove my Account");
        System.out.println("0. Back");

        String command = readString("Command: ");
        switch (command) {
            case "1":
                handleListAllUsers();
                break;
            case "2":
                handleSearchUsers();
                break;
            case "3":
                handleViewUserProfile();
                break;
            case "4":
                handleRemoveMyAccount();
                break;
            case "0":
                return;
            default:
                System.out.println("Invalid command.");
        }
    }

    // === FRIENDSHIP MANAGEMENT MENU ===
    private void runFriendshipManagementMenu() {
        System.out.println("\n--- Friendship Management ---");
        System.out.println("1. Add Friend");
        System.out.println("2. Remove Friend");
        System.out.println("3. List My Friends");
        System.out.println("0. Back");

        String command = readString("Command: ");
        switch (command) {
            case "1":
                handleAddFriendship();
                break;
            case "2":
                handleRemoveFriendship();
                break;
            case "3":
                handleListMyFriends();
                break;
            case "0":
                return;
            default:
                System.out.println("Invalid command.");
        }
    }

    // === MESSAGES MENU ===
    private void runMessagesMenu() {
        System.out.println("\n--- Messages ---");
        System.out.println("1. Send Message");
        System.out.println("2. View Conversations");
        System.out.println("3. View All My Messages");
        System.out.println("0. Back");

        String command = readString("Command: ");
        switch (command) {
            case "1":
                handleSendMessage();
                break;
            case "2":
                handleViewConversations();
                break;
            case "3":
                handleViewAllMessages();
                break;
            case "0":
                return;
            default:
                System.out.println("Invalid command.");
        }
    }

    // === FLOCKS MENU ===
    private void runFlocksMenu() {
        System.out.println("\n--- Flocks (Carduri) ---");
        System.out.println("1. Create Flock");
        System.out.println("2. Join Flock");
        System.out.println("3. Leave Flock");
        System.out.println("4. List All Flocks");
        if (currentUser instanceof Duck) {
            System.out.println("5. My Flock Info");
        }
        System.out.println("0. Back");

        String command = readString("Command: ");
        switch (command) {
            case "1":
                handleCreateFlock();
                break;
            case "2":
                handleJoinFlock();
                break;
            case "3":
                handleLeaveFlock();
                break;
            case "4":
                handleListAllFlocks();
                break;
            case "5":
                if (currentUser instanceof Duck) handleMyFlockInfo();
                else System.out.println("Only ducks can view flock info.");
                break;
            case "0":
                return;
            default:
                System.out.println("Invalid command.");
        }
    }

    // === EVENTS MENU ===
    private void runEventsMenu() {
        System.out.println("\n--- Events & Races ---");
        if (currentUser instanceof Person) {
            System.out.println("1. Create Race Event");
        }
        System.out.println("2. List All Events");
        System.out.println("3. Subscribe to Event");
        System.out.println("4. Unsubscribe from Event");
        System.out.println("5. Execute Race Event");
        System.out.println("0. Back");

        String command = readString("Command: ");
        switch (command) {
            case "1":
                if (currentUser instanceof Person) handleCreateRaceEvent();
                else System.out.println("Only persons can create events.");
                break;
            case "2":
                handleListAllEvents();
                break;
            case "3":
                handleSubscribeToEvent();
                break;
            case "4":
                handleUnsubscribeFromEvent();
                break;
            case "5":
                handleExecuteRaceEvent();
                break;
            case "0":
                return;
            default:
                System.out.println("Invalid command.");
        }
    }

    // === NETWORK ANALYSIS MENU ===
    private void runNetworkAnalysisMenu() {
        System.out.println("\n--- Network Analysis ---");
        System.out.println("1. Number of Communities");
        System.out.println("2. Most Sociable Community");
        System.out.println("0. Back");

        String command = readString("Command: ");
        switch (command) {
            case "1":
                handleShowCommunitiesCount();
                break;
            case "2":
                handleShowMostSociableCommunity();
                break;
            case "0":
                return;
            default:
                System.out.println("Invalid command.");
        }
    }

    private void handleNetworkStatistics() {
        System.out.println("\n--- Network Statistics (Public) ---");
        handleShowCommunitiesCount();
        handleShowMostSociableCommunity();
    }

    // === IMPLEMENTATION METHODS ===

    // User Management
    private Person handleRegisterPerson(String user, String email, String pass) {
        String firstName = readString("First Name: ");
        String lastName = readString("Last Name: ");
        LocalDate birthDate = readLocalDate("Birth Date (YYYY-MM-DD): ");
        String occupation = readString("Occupation: ");
        int empathy = readInt("Empathy Level (0-10): ");

        return new Person(user, email, pass, firstName, lastName, birthDate, occupation, empathy);
    }

    private Duck handleRegisterDuck(String user, String email, String pass) {
        DuckType type = readDuckType("Duck Type (FLYING, SWIMMING, FLYING_AND_SWIMMING): ");
        double speed = readDouble("Speed (e.g., 5.5): ");
        double resistance = readDouble("Resistance (e.g., 10.0): ");

        // Create the specific duck type based on DuckType
        switch (type) {
            case FLYING:
                return new FlyingDuck(user, email, pass, speed, type, resistance);
            case SWIMMING:
                return new SwimmingDuck(user, email, pass, speed, type, resistance);
            case FLYING_AND_SWIMMING:
                return new FlyingAndSwimmingDuck(user, email, pass, speed, type, resistance);
            default:
                throw new IllegalArgumentException("Unknown duck type: " + type);
        }
    }

    private void handleListAllUsers() {
        try {
            System.out.println("\n--- All Users ---");
            System.out.println(userService.getAllUsers());
        } catch (RepositoryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleSearchUsers() {
        String query = readString("Search by username: ");
        try {
            System.out.println("\n--- Search Results ---");
            // Implementare simplă - poți extinde cu logică de search în UserService
            System.out.println("Search functionality not fully implemented yet.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleViewUserProfile() {
        Long userId = readLong("Enter user ID: ");
        try {
            User user = userService.findUserById(userId);
            System.out.println("\n--- User Profile ---");
            System.out.println(user);
            if (user instanceof Duck) {
                Duck duck = (Duck) user;
                if (duck.getFlockId() != null) {
                    System.out.println("Flock ID: " + duck.getFlockId());
                }
            }
        } catch (RepositoryException e) {
            System.out.println("User not found: " + e.getMessage());
        }
    }

    private void handleRemoveMyAccount() {
        try {
            String confirm = readString("Are you sure you want to delete your account? (yes/no): ");
            if (confirm.equalsIgnoreCase("yes")) {
                userService.deleteUser(this.currentUser.getId());
                System.out.println("Account deleted successfully.");
                this.currentUser = null;
            } else {
                System.out.println("Account deletion cancelled.");
            }
        } catch (RepositoryException e) {
            System.out.println("Failed to delete account: " + e.getMessage());
        }
    }

    // Friendship Management
    private void handleAddFriendship() {
        try {
            Long id1 = this.currentUser.getId();
            Long id2 = readLong("Enter ID of user to add as friend: ");

            if (id2 != null) {
                friendshipService.addFriendship(id1, id2);
                System.out.println("Friendship added successfully!");
            }

        } catch (ValidationException | RepositoryException e) {
            System.out.println("Failed to add friend: " + e.getMessage());
        }
    }

    private void handleRemoveFriendship() {
        try {
            Long id1 = this.currentUser.getId();
            Long id2 = readLong("Enter ID of friend to remove: ");

            if (id2 != null) {
                friendshipService.deleteFriendship(id1, id2);
                System.out.println("Friendship removed successfully!");
            }
        } catch (ValidationException | RepositoryException e) {
            System.out.println("Failed to remove friend: " + e.getMessage());
        }
    }

    private void handleListMyFriends() {
        try {
            System.out.println("\n--- My Friends ---");
            System.out.println(friendshipService.findFriendshipsForUser(currentUser.getId()));
        } catch (RepositoryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Messages
    private void handleSendMessage() {
        try {
            Long receiverId = readLong("Enter receiver ID: ");
            String content = readString("Message content: ");

            messageService.sendMessage(currentUser.getId(), receiverId, content);
            System.out.println("Message sent successfully!");

        } catch (ValidationException | RepositoryException e) {
            System.out.println("Failed to send message: " + e.getMessage());
        }
    }

    private void handleViewConversations() {
        try {
            Long otherUserId = readLong("Enter user ID to view conversation: ");
            System.out.println("\n--- Conversation ---");
            System.out.println(messageService.getConversation(currentUser.getId(), otherUserId));
        } catch (RepositoryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleViewAllMessages() {
        try {
            System.out.println("\n--- All My Messages ---");
            System.out.println(messageService.getMessagesForUser(currentUser.getId()));
        } catch (RepositoryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Flocks
    private void handleCreateFlock() {
        try {
            String name = readString("Flock name: ");
            FlockPurpose purpose = readFlockPurpose("Flock purpose (SwimMasters/SkyFlyers): ");

            Flock flock = flockService.createFlock(name, purpose);
            System.out.println("Flock created successfully with ID: " + flock.getId());

        } catch (ValidationException | RepositoryException e) {
            System.out.println("Failed to create flock: " + e.getMessage());
        }
    }

    private void handleJoinFlock() {
        if (!(currentUser instanceof Duck)) {
            System.out.println("Only ducks can join flocks!");
            return;
        }

        try {
            Long flockId = readLong("Enter flock ID to join: ");
            flockService.addDuckToFlock(currentUser.getId(), flockId);
            System.out.println("Successfully joined the flock!");

        } catch (ValidationException | RepositoryException | ServiceException e) {
            System.out.println("Failed to join flock: " + e.getMessage());
        }
    }

    private void handleLeaveFlock() {
        if (!(currentUser instanceof Duck)) {
            System.out.println("Only ducks can leave flocks!");
            return;
        }

        try {
            Duck duck = (Duck) currentUser;
            if (duck.getFlockId() == null) {
                System.out.println("You are not in any flock!");
                return;
            }

            flockService.removeDuckFromFlock(currentUser.getId(), duck.getFlockId());
            System.out.println("Successfully left the flock!");

        } catch (RepositoryException | ServiceException e) {
            System.out.println("Failed to leave flock: " + e.getMessage());
        }
    }


    private void handleListAllFlocks() {
        try {
            System.out.println("\n--- All Flocks ---");

            Iterable<Flock> flocks = flockService.findAll();
            boolean hasFlocks = false;

            for (Flock flock : flocks) {
                hasFlocks = true;
                displayFlockInfo(flock);
            }

            if (!hasFlocks) {
                System.out.println("No flocks found.");
            }

        } catch (RepositoryException e) {
            System.err.println("Failed to retrieve flocks: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
        }
    }

    private void displayFlockInfo(Flock flock) {
        System.out.println("Flock ID: " + flock.getId());
        System.out.println("Name: " + flock.getName());
        System.out.println("Purpose: " + flock.getFlockPurpose());
        System.out.println("Created: " + flock.getCreationTime());
        System.out.println("Total Members: " + flock.getMemberIds().size());

        if (!flock.getMemberIds().isEmpty()) {
            System.out.print("Member IDs: ");
            System.out.println(flock.getMemberIds());

            // Show member details if available
            List<String> memberNames = new ArrayList<>();
            for (Long memberId : flock.getMemberIds()) {
                try {
                    User user = userService.findUserById(memberId);
                    if (user instanceof Duck) {
                        Duck duck = (Duck) user;
                        memberNames.add(duck.getUsername() + " (" + duck.getDuckType() + ")");
                    }
                } catch (RepositoryException e) {
                    memberNames.add("ID:" + memberId + " (not found)");
                }
            }

            if (!memberNames.isEmpty()) {
                System.out.println("Members: " + String.join(", ", memberNames));
            }
        } else {
            System.out.println("No members yet");
        }

        System.out.println("---");
    }

    private void handleMyFlockInfo() {
        Duck duck = (Duck) currentUser;
        if (duck.getFlockId() != null) {
            System.out.println("You are in flock ID: " + duck.getFlockId());
        } else {
            System.out.println("You are not in any flock.");
        }
    }

    // Events
    private void handleCreateRaceEvent() {
        try {
            String name = readString("Event name: ");
            String description = readString("Event description: ");
            LocalDateTime eventTime = readDateTime("Event time (YYYY-MM-DDTHH:MM:SS): ");

            // Create lanes
            int numLanes = readInt("Number of lanes: ");
            List<Lane> lanes = new ArrayList<>();
            for (int i = 1; i <= numLanes; i++) {
                double distance = readDouble("Distance for lane " + i + ": ");
                lanes.add(new Lane(i, distance));
            }

            Event event = eventService.createRaceEvent(currentUser.getId(), name, description, eventTime, lanes);
            System.out.println("Race event created successfully with ID: " + event.getId());

        } catch (ValidationException | RepositoryException e) {
            System.out.println("Failed to create event: " + e.getMessage());
        }
    }

    private void handleListAllEvents() {
        try {
            System.out.println("\n--- All Events ---");

            Iterable<Event> events = eventService.findAll();
            boolean hasEvents = false;

            for (Event event : events) {
                hasEvents = true;
                displayEventInfo(event);
            }

            if (!hasEvents) {
                System.out.println("No events found.");
            }

        } catch (RepositoryException e) {
            System.err.println("Failed to retrieve events: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
        }
    }

    private void displayEventInfo(Event event) {
        System.out.println("Event ID: " + event.getId());
        System.out.println("Name: " + event.getName());
        System.out.println("Description: " + event.getDescription());
        System.out.println("Organizer ID: " + event.getOrganizerId());
        System.out.println("Subscribers: " + event.getSubscriberIds().size());

        if (event instanceof RaceEvent) {
            RaceEvent raceEvent = (RaceEvent) event;
            System.out.println("Type: Race Event");
            System.out.println("Event Time: " + raceEvent.getEventTime());
            System.out.println("Number of Lanes: " + raceEvent.getLanes().size());

            if (raceEvent.getOptimalTime() != null) {
                System.out.println("Optimal Time: " + raceEvent.getOptimalTime());
                System.out.println("Status: Executed");
            } else {
                System.out.println("Status: Not yet executed");
            }
        }

        System.out.println("---");
    }


    private void handleSubscribeToEvent() {
        try {
            Long eventId = readLong("Enter event ID to subscribe: ");
            eventService.subscribe(currentUser.getId(), eventId);
            System.out.println("Successfully subscribed to event!");

        } catch (RepositoryException e) {
            System.out.println("Failed to subscribe: " + e.getMessage());
        }
    }

    private void handleUnsubscribeFromEvent() {
        try {
            Long eventId = readLong("Enter event ID to unsubscribe: ");
            eventService.unsubscribe(currentUser.getId(), eventId);
            System.out.println("Successfully unsubscribed from event!");

        } catch (RepositoryException e) {
            System.out.println("Failed to unsubscribe: " + e.getMessage());
        }
    }

    private void handleExecuteRaceEvent() {
        try {
            Long eventId = readLong("Enter event ID to execute: ");
            eventService.executeEvent(eventId);
        } catch (RepositoryException e) {
            System.out.println("Failed to execute event: " + e.getMessage());
        }
    }

    // Network Analysis
    private void handleShowCommunitiesCount() {
        try {
            int count = friendshipService.findNumberOfCommunities();
            System.out.println("Number of communities: " + count);
        } catch (RepositoryException e) {
            System.out.println("Error calculating communities: " + e.getMessage());
        }
    }

    private void handleShowMostSociableCommunity() {
        try {
            System.out.println("Most sociable community:");
            System.out.println(friendshipService.getMostSociableCommunity());
        } catch (RepositoryException e) {
            System.out.println("Error finding most sociable community: " + e.getMessage());
        }
    }

    // === INPUT HELPER METHODS ===
    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private Long readLong(String prompt) {
        while (true) {
            try {
                return Long.parseLong(readString(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readString(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readString(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (e.g., 5.5).");
            }
        }
    }

    private LocalDate readLocalDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readString(prompt));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    private LocalDateTime readDateTime(String prompt) {
        while (true) {
            try {
                return LocalDateTime.parse(readString(prompt));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid datetime format. Please use YYYY-MM-DDTHH:MM:SS.");
            }
        }
    }

    private DuckType readDuckType(String prompt) {
        while (true) {
            try {
                String input = readString(prompt).toUpperCase();
                return DuckType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid type. Must be FLYING, SWIMMING, or FLYING_AND_SWIMMING.");
            }
        }
    }

    private FlockPurpose readFlockPurpose(String prompt) {
        while (true) {
            try {
                String input = readString(prompt);
                return FlockPurpose.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid purpose. Must be SwimMasters or SkyFlyers.");
            }
        }
    }
}*/
