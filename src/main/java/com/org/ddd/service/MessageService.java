package com.org.ddd.service;

import com.org.ddd.domain.entities.Message;
import com.org.ddd.domain.entities.User;
import com.org.ddd.domain.validation.exceptions.ValidationException;
import com.org.ddd.domain.validation.validators.Validator;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.exceptions.RepositoryException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MessageService {

    private final AbstractRepository<Long, Message> messageRepo;
    private final AbstractRepository<Long, User> userRepo;
    private final Validator<Message> messageValidator;

    public MessageService(AbstractRepository<Long, Message> messageRepo, AbstractRepository<Long, User> userRepo, Validator<Message> messageValidator) {
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
        this.messageValidator = messageValidator;
    }

    public Message sendMessage(Long senderId, Long receiverId, String content) throws ValidationException, RepositoryException {
        Message newMessage = new Message(
                senderId,
                receiverId,
                content
        );

        messageValidator.validate(newMessage);

        businessRulesValidator(senderId, receiverId);

        messageRepo.add(newMessage);

        return newMessage;
    }

    public List<Message> getConversation(Long userId1, Long userId2) {
        List<Message> conversation = new ArrayList<>();

        for (Message msg : messageRepo.findAll()) {
            boolean user1ToUser2 = msg.getSenderId().equals(userId1) &&
                    msg.getReceiverId().equals(userId2);

            boolean user2ToUser1 = msg.getSenderId().equals(userId2) &&
                    msg.getReceiverId().equals(userId1);

            if (user1ToUser2 || user2ToUser1) {
                conversation.add(msg);
            }
        }

        conversation.sort(Comparator.comparing(Message::getTimestamp));
        return conversation;
    }

    public List<Message> getMessagesForUser(Long userId) {
        return ((List<Message>) messageRepo.findAll()).stream()
                .filter(msg -> msg.getReceiverId().equals(userId))
                .sorted(Comparator.comparing(Message::getTimestamp))
                .collect(Collectors.toList());
    }

    private void businessRulesValidator(Long senderId, Long receiverId) throws RepositoryException {
        if (!senderId.equals(0L)) {
            userRepo.findById(senderId);
        }

        userRepo.findById(receiverId);
    }
}