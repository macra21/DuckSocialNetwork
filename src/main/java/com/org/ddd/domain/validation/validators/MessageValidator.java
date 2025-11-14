package com.org.ddd.domain.validation.validators;

import com.org.ddd.domain.entities.Message;
import com.org.ddd.domain.validation.exceptions.ValidationException;

import java.util.Objects;

public class MessageValidator implements Validator<Message> {

    @Override
    public void validate(Message entity) throws ValidationException {
        StringBuilder errors = new StringBuilder();

        if (entity.getSenderId() == null) {
            errors.append("Sender ID cannot be null.\n");
        }

        if (entity.getReceiverId() == null) {
            errors.append("Receiver ID cannot be null.\n");
        }

        if (entity.getContent() == null || entity.getContent().isBlank()) {
            errors.append("Message content cannot be null or empty.\n");
        }

        if (Objects.equals(entity.getSenderId(), entity.getReceiverId())) {
            errors.append("Sender and Receiver cannot be the same user.\n");
        }

        if (errors.length() > 0) {
            throw new ValidationException(errors.toString());
        }
    }
}