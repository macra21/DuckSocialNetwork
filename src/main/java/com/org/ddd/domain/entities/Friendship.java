package com.org.ddd.domain.entities;

import java.time.LocalDateTime;

public class Friendship extends Entity<Long>{
    private Long userId1;
    private Long userId2;
    private LocalDateTime friendsFrom;

    public Friendship(Long userId1, Long userId2) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.friendsFrom = LocalDateTime.now();
    }

    public Friendship(Long userId1, Long userId2, LocalDateTime friendsFrom) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.friendsFrom = friendsFrom;
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
                '}';
    }
}
