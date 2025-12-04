package com.org.ddd.domain.entities;

import com.org.ddd.utils.Identifiable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Group<T extends User> extends Identifiable<Long> {
    private String name;
    private final List<Long> memberIds;
    private LocalDateTime createdAt;

    public Group(String name) {
        this.name = name;
        this.memberIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreationTime(){
        return this.createdAt;
    }

    public void setCreationTime(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public boolean hasMemberId(Long userId) {
        return memberIds.contains(userId);
    }

    public void addMemberId(Long userId) {
        memberIds.add(userId);
    }
    public void removeMemberId(Long userId) {
        memberIds.remove(userId);
    }

    @Override
    public String toString() {
        return "Group{" +
                "name='" + name +
                "', memberIds=" + memberIds +
                ", createdAt=" + createdAt +
                '}';
    }
}
