// This class was used for the FileRepository and now it is not compatible with the current project
/*
package com.org.ddd.repository.converters;

import com.org.ddd.domain.entities.Message;
import com.org.ddd.repository.exceptions.RepositoryException;

import java.time.LocalDateTime;

public class MessageConverter implements EntityConverter<Message> {

    private static final String SEPARATOR = ":::";
    private static final String NEWLINE_REPLACEMENT = "|NL|";

    @Override
    public String toLine(Message entity) {
        String safeContent = entity.getContent()
                .replace("\n", NEWLINE_REPLACEMENT)
                .replace("\r", "");

        return entity.getId() + SEPARATOR +
                entity.getSenderId() + SEPARATOR +
                entity.getReceiverId() + SEPARATOR +
                entity.getTimestamp().toString() + SEPARATOR +
                safeContent;
    }

    @Override
    public Message fromLine(String line) {
        String[] parts = line.split(SEPARATOR);
        if (parts.length != 5) {
            throw new RepositoryException("Corrupt message line: " + line);
        }

        try {
            Long id = Long.parseLong(parts[0]);
            Long senderId = Long.parseLong(parts[1]);
            Long receiverId = Long.parseLong(parts[2]);
            LocalDateTime timestamp = LocalDateTime.parse(parts[3]);
            String content = parts[4].replace(NEWLINE_REPLACEMENT, "\n");

            Message msg = new Message(
                    senderId,
                    receiverId,
                    content
            );

            msg.setId(id);
            msg.setTimestamp(timestamp);

            return msg;

        } catch (Exception e) {
            throw new RepositoryException("Failed to parse message line: " + line, e);
        }
    }
}*/
