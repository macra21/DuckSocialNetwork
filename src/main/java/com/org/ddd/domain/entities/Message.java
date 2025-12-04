package com.org.ddd.domain.entities;

import com.org.ddd.utils.Identifiable;

import java.time.LocalDateTime;

public class Message extends Identifiable<Long> {

    private final Long senderId;
    private final Long receiverId;
    private final String content;
    private LocalDateTime timestamp;

    public Message(
            Long senderId,
            Long receiverId,
            String content
    ) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public Long getSenderId() {
        return senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + getId() +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", timestamp=" + timestamp +
                ", content='" + content + '\'' +
                '}';
    }
}