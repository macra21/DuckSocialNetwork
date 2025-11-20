package com.org.ddd.service;

import com.org.ddd.domain.entities.User;
import com.org.ddd.domain.validation.exceptions.ValidationException;
import com.org.ddd.domain.validation.validators.Validator;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.utils.PasswordHasher;

import java.security.NoSuchAlgorithmException;

public class AuthService {

    private final AbstractRepository<Long, User> userRepository;
    private final Validator<User> userValidator;

    public AuthService(AbstractRepository<Long, User> userRepository, Validator<User> userValidator) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
    }

    public User login(String email, String password) throws ValidationException, RepositoryException, NoSuchAlgorithmException {

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new ValidationException("Email and password are required.\n");
        }

        User userToLogin = findUserByEmail(email);

        if (userToLogin == null) {
            throw new ValidationException("Invalid credentials. User not found.\n");
        }

        String hashedPassword = PasswordHasher.hash(password);

        if (!userToLogin.getPassword().equals(hashedPassword)) {
            throw new ValidationException("Invalid credentials. Wrong password.\n");
        }

        return userToLogin;
    }

    public void register(User user) throws ValidationException, RepositoryException, NoSuchAlgorithmException {
        userValidator.validate(user);

        validateUsernameUniqueness(user.getUsername());
        validateEmailUniqueness(user.getEmail());

        String hashedPassword = PasswordHasher.hash(user.getPassword());
        user.setPassword(hashedPassword);

        userRepository.add(user);
    }

    private User findUserByEmail(String email) {
        Iterable<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    private void validateUsernameUniqueness(String username) throws ValidationException {
        Iterable<User> allUsers = this.userRepository.findAll();
        for (User existingUser : allUsers) {
            if (existingUser.getUsername().equalsIgnoreCase(username)) {
                throw new ValidationException("Username '" + username + "' is already taken.\n");
            }
        }
    }

    private void validateEmailUniqueness(String email) throws ValidationException {
        Iterable<User> allUsers = this.userRepository.findAll();
        for (User existingUser : allUsers) {
            if (existingUser.getEmail().equalsIgnoreCase(email)) {
                throw new ValidationException("Email '" + email + "' is already taken.\n");
            }
        }
    }
}