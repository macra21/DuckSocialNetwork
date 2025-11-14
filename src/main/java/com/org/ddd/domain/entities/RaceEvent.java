package com.org.ddd.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RaceEvent extends Event {
    private LocalDateTime eventTime;

    private final List<Lane> lanes;

    private Double optimalTime;
    private String raceResultReport;

    public RaceEvent(Long organizerId, String description, String name, LocalDateTime eventTime) {
        super(organizerId, description, name);
        this.eventTime = eventTime;
        this.lanes = new ArrayList<>();
        this.optimalTime = null;
        this.raceResultReport = null;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }
    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public List<Lane> getLanes() {
        return lanes;
    }
    public void setupLanes(List<Lane> lanesConfig) {
        this.lanes.clear();
        this.lanes.addAll(lanesConfig);
    }

    public Double getOptimalTime() {
        return optimalTime;
    }
    public void setOptimalTime(Double optimalTime) {
        this.optimalTime = optimalTime; }


    public String getRaceResultReport() {
        return raceResultReport;
    }
    public void setRaceResultReport(String raceResultReport) {
        this.raceResultReport = raceResultReport;
    }
}