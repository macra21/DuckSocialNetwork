package com.org.ddd.domain.entities;

import com.org.ddd.utils.Identifiable;

import java.time.LocalDateTime;

public class Friendship extends Identifiable<Long> {
    private Long userId1;
    private Long userId2;
    private LocalDateTime friendsFrom;
    private FriendshipStatus status;

    public Friendship(Long userId1, Long userId2) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.friendsFrom = LocalDateTime.now();
        this.status = FriendshipStatus.PENDING;
    }

    public Friendship(Long userId1, Long userId2, LocalDateTime friendsFrom) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.friendsFrom = friendsFrom;
        this.status = FriendshipStatus.PENDING;
    }

    public Long getUserId1() {
        return userId1;
    }

    public void setUserId1(Long userId1) {
        this.userId1 = userId1;
    }

    public Long getUserId2() {
        return userId2;
    }

    public void setUserId2(Long userId2) {
        this.userId2 = userId2;
    }

    public LocalDateTime getFriendsFrom() {
        return friendsFrom;
    }

    public void setFriendsFrom(LocalDateTime friendsFrom) {
        this.friendsFrom = friendsFrom;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public boolean isBetween(Long userId1, Long userId2){
        if (this.userId1.equals(userId1) && this.userId2.equals(userId2) ||
                this.userId1.equals(userId2) && this.userId2.equals(userId1))
            return true;
        return false;
    }

    @Override
    public String toString() {
        return "Friendship{" +
                "id=" + getId() +
                "userId1=" + userId1 +
                ", userId2=" + userId2 +
                ", friendsFrom=" + friendsFrom +
                ", status=" + status +
                '}';
    }
}
