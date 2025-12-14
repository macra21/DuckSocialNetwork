package com.org.ddd.service;

import com.org.ddd.domain.entities.User;
import com.org.ddd.domain.validation.exceptions.ValidationException;
import com.org.ddd.domain.validation.validators.Validator;
import com.org.ddd.dto.UserFilterDTO;
import com.org.ddd.repository.dbRepositories.UserDBRepository;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.utils.PasswordHasher;
import com.org.ddd.utils.paging.Page;
import com.org.ddd.utils.paging.Pageable;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class AuthService {

    private final UserDBRepository userRepository;
    private final Validator<User> userValidator;

    public AuthService(UserDBRepository userRepository, Validator<User> userValidator) {
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
        UserFilterDTO filter = new UserFilterDTO();
        filter.setEmail(Optional.of(email));
        Page<User> result = userRepository.findAllOnPage(new Pageable(0, 1), filter);
        
        return StreamSupport.stream(result.getElementsOnPage().spliterator(), false)
                .findFirst()
                .orElse(null);
    }

    private void validateUsernameUniqueness(String username) throws ValidationException {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setUsername(Optional.of(username));
        Page<User> result = userRepository.findAllOnPage(new Pageable(0, 1), filter);

        if (result.getTotalNumberOfElements() > 0) {
            throw new ValidationException("Username '" + username + "' is already taken.\n");
        }
    }

    private void validateEmailUniqueness(String email) throws ValidationException {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setEmail(Optional.of(email));
        Page<User> result = userRepository.findAllOnPage(new Pageable(0, 1), filter);

        if (result.getTotalNumberOfElements() > 0) {
            throw new ValidationException("Email '" + email + "' is already taken.\n");
        }
    }
}
