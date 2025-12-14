package com.org.ddd.service;

import com.org.ddd.domain.entities.Message;
import com.org.ddd.domain.entities.User;
import com.org.ddd.domain.validation.exceptions.ValidationException;
import com.org.ddd.domain.validation.validators.Validator;
import com.org.ddd.dto.MessageFilterDTO;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.dbRepositories.MessageDBRepository;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.utils.paging.Page;
import com.org.ddd.utils.paging.Pageable;

import java.util.List;
import java.util.Map;

public class MessageService {

    private final MessageDBRepository messageRepo;
    private final AbstractRepository<Long, User> userRepo;
    private final Validator<Message> messageValidator;

    public MessageService(MessageDBRepository messageRepo, AbstractRepository<Long, User> userRepo, Validator<Message> messageValidator) {
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
        this.messageValidator = messageValidator;
    }

    public Message sendMessage(Long senderId, List<Long> receiversIdList, String content, Long replyToMessageId) throws ValidationException, RepositoryException {
        Message newMessage;
        if (replyToMessageId != null) {
            newMessage = new Message(senderId, receiversIdList, content, replyToMessageId);
        } else {
            newMessage = new Message(senderId, receiversIdList, content);
        }

        messageValidator.validate(newMessage);
        businessRulesValidator(senderId, receiversIdList);

        messageRepo.add(newMessage);
        return newMessage;
    }

    public Page<Message> getConversation(Long userId1, Long userId2, Pageable pageable) {
        MessageFilterDTO filter = new MessageFilterDTO(userId1, userId2);
        return messageRepo.findAllOnPage(pageable, filter);
    }

    public void markMessageAsRead(Long messageId, Long userId) {
        messageRepo.markAsRead(messageId, userId);
    }

    public Map<Long, Integer> getUnreadMessagesSummary(Long forUser) {
        return messageRepo.getUnreadMessagesSummary(forUser);
    }

    private void businessRulesValidator(Long senderId, List<Long> receiversIdList) throws RepositoryException {
        if (senderId != null && senderId != 0L) {
            userRepo.findById(senderId);
        }
        for (Long receiverId : receiversIdList) {
            userRepo.findById(receiverId);
        }
    }
}
