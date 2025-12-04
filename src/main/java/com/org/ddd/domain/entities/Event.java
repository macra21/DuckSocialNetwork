package com.org.ddd.domain.entities;

import com.org.ddd.utils.Identifiable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Event extends Identifiable<Long> {
    private String name;
    private String description;
    private Long organizerId; // ID of the User who organized the event
    private LocalDateTime createdAt;

    private final List<Long> subscriberIds;

    public Event(Long organizerId, String description, String name) {
        this.organizerId = organizerId;
        this.description = description;
        this.name = name;
        createdAt = LocalDateTime.now();
        subscriberIds = new ArrayList<>();
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public Long getOrganizerId(){
        return this.organizerId;
    }

    public List<Long> getSubscriberIds(){
        return subscriberIds;
    }

    public boolean isSubscribed(Long userId){
        return subscriberIds.contains(userId);
    }

    public void addSubscriber(Long userId){
        subscriberIds.add(userId);
    }

    public void removeSubscriber(Long userId){
        subscriberIds.remove(userId);
    }
}
