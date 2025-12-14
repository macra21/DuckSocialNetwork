package com.org.ddd.dto;

import java.util.List;
import java.util.Optional;

public class MessageFilterDTO {
    // For conversations between two users
    private Optional<Long> user1;
    private Optional<Long> user2;

    // For more filtering
    private Optional<Long> senderId;
    private Optional<List<Long>> receiversIdList;
    private Optional<Boolean> seen;

    public MessageFilterDTO(Long user1, Long user2) {
        this.user1 = Optional.of(user1);
        this.user2 = Optional.of(user2);
        this.senderId = Optional.empty();
        this.receiversIdList = Optional.empty();
        this.seen = Optional.empty();
    }

    public MessageFilterDTO() {
        this.user1 = Optional.empty();
        this.user2 = Optional.empty();
        this.senderId = Optional.empty();
        this.receiversIdList = Optional.empty();
        this.seen = Optional.empty();
    }

    public Optional<Long> getUser1() {
        return user1;
    }

    public void setUser1(Long user1) {
        this.user1 = Optional.ofNullable(user1);
    }

    public Optional<Long> getUser2() {
        return user2;
    }

    public void setUser2(Long user2) {
        this.user2 = Optional.ofNullable(user2);
    }

    public Optional<Long> getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = Optional.ofNullable(senderId);
    }

    public Optional<List<Long>> getReceiversIdList() {
        return receiversIdList;
    }

    public void setReceiversIdList(List<Long> receiversIdList) {
        this.receiversIdList = Optional.ofNullable(receiversIdList);
    }

    public Optional<Boolean> getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = Optional.ofNullable(seen);
    }
}
