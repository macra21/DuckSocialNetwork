package com.org.ddd.service;

import com.org.ddd.domain.entities.User;
import com.org.ddd.domain.validation.exceptions.ValidationException;
import com.org.ddd.domain.validation.validators.Validator;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.exceptions.RepositoryException;

public class UserService {
    private final AbstractRepository<Long, User> userRepository;
    private final FriendshipService friendshipService; // not using the friendship repo to avoid circular dependencies
    private final Validator<User> userValidator;

    public UserService(AbstractRepository<Long, User> userRepository, FriendshipService friendshipService, Validator<User> userValidator) {
        this.userRepository = userRepository;
        this.friendshipService = friendshipService;
        this.userValidator = userValidator;
    }

    public void addUser(User user) throws ValidationException, RepositoryException {
        userValidator.validate(user);
        // TODO maybe add service validation(unique username, etc...)
        userRepository.add(user);
    }

    public void updateUser(User user) throws ValidationException, RepositoryException{
        userValidator.validate(user);
        userRepository.update(user);
    }

    public User deleteUser(Long id) throws RepositoryException{
        friendshipService.deleteAllFriendshipsForUser(id);
        //TODO delete user from all groups it is in...
        return userRepository.delete(id);
    }

    public User findUserById(Long id) throws RepositoryException{
        return userRepository.findById(id);
    }

    public Iterable<User> getAllUsers(){
        return userRepository.findAll();
    }
}
