package com.org.ddd.domain.validation.validators;

import com.org.ddd.domain.entities.Message;
import com.org.ddd.domain.validation.exceptions.ValidationException;

public class MessageValidator implements Validator<Message> {

    @Override
    public void validate(Message entity) throws ValidationException {
        StringBuilder errors = new StringBuilder();

        if (entity.getSenderId() == null) {
            errors.append("Sender ID cannot be null.\n");
        }

        if (entity.getReceiversIdList() == null || entity.getReceiversIdList().isEmpty()) {
            errors.append("Receivers list cannot be null or empty.\n");
        } else {
            if (entity.getReceiversIdList().contains(entity.getSenderId())) {
                errors.append("Sender cannot be in the receivers list.\n");
            }
        }

        if (entity.getContent() == null || entity.getContent().isBlank()) {
            errors.append("Message content cannot be null or empty.\n");
        }

        if (errors.length() > 0) {
            throw new ValidationException(errors.toString());
        }
    }
}
