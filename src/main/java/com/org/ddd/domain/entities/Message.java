package com.org.ddd.domain.entities;

import com.org.ddd.utils.Identifiable;

import java.time.LocalDateTime;
import java.util.List;

public class Message extends Identifiable<Long> {

    private Long senderId;
    private List<Long> receiversIdList;
    private String content;
    private LocalDateTime timestamp;
    private Long replyId;
    
    public Message(Long senderId, List<Long> receiversIdList, String content) {
        this.senderId = senderId;
        this.receiversIdList = receiversIdList;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.replyId = null;
    }

    public Message(Long senderId, List<Long> receiversIdList, String content, Long replyId) {
        this.senderId = senderId;
        this.receiversIdList = receiversIdList;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.replyId = replyId;
    }

    public Message(Long id, Long senderId, List<Long> receiversIdList, String content, LocalDateTime timestamp, Long replyId) {
        setId(id);
        this.senderId = senderId;
        this.receiversIdList = receiversIdList;
        this.content = content;
        this.timestamp = timestamp;
        this.replyId = replyId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public List<Long> getReceiversIdList() {
        return receiversIdList;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Long getReplyId() {
        return replyId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public void setReceiversIdList(List<Long> receiversIdList) {
        this.receiversIdList = receiversIdList;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setReplyId(Long replyId) {
        this.replyId = replyId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + getId() +
                ", senderId=" + senderId +
                ", receiversIdList=" + receiversIdList +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", replyId=" + replyId +
                '}';
    }
}
