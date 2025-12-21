package com.org.ddd.dto;

import com.org.ddd.domain.entities.FriendshipStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public class FriendshipFilterDTO {
    private Optional<Long> userId1 = Optional.empty();
    private Optional<Long> userId2 = Optional.empty();
    private Optional<LocalDateTime> friendsFrom = Optional.empty();
    private Optional<FriendshipStatus> status = Optional.empty();
    private Optional<Long> involvedUser = Optional.empty();

    public FriendshipFilterDTO() {
    }

    public Optional<Long> getUserId1() {
        return userId1;
    }

    public void setUserId1(Long userId1) {
        this.userId1 = Optional.ofNullable(userId1);
    }

    public Optional<Long> getUserId2() {
        return userId2;
    }

    public void setUserId2(Long userId2) {
        this.userId2 = Optional.ofNullable(userId2);
    }

    public Optional<LocalDateTime> getFriendsFrom() {
        return friendsFrom;
    }

    public void setFriendsFrom(LocalDateTime friendsFrom) {
        this.friendsFrom = Optional.ofNullable(friendsFrom);
    }

    public Optional<FriendshipStatus> getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = Optional.ofNullable(status);
    }

    public Optional<Long> getInvolvedUser() {
        return involvedUser;
    }

    public void setInvolvedUser(Long involvedUser) {
        this.involvedUser = Optional.ofNullable(involvedUser);
    }
}
