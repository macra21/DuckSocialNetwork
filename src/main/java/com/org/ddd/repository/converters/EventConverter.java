package com.org.ddd.repository.converters;

import com.org.ddd.domain.entities.*;
import com.org.ddd.repository.exceptions.RepositoryException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EventConverter implements EntityConverter<Event> {

    private static final String SEP = ":::";
    private static final String LIST_SEP = ",";
    private static final String LANE_SEP = ";";
    private static final String NL_REPLACE = "|NL|";

    @Override
    public String toLine(Event event) {
        if (event instanceof RaceEvent) {
            return convertRaceEventToString((RaceEvent) event);
        }

        throw new RepositoryException("Unknown Event type to serialize: " + event.getClass().getSimpleName());
    }

    @Override
    public Event fromLine(String line) {
        String[] parts = line.split(SEP, 2);
        if (parts.length < 2) {
            throw new RepositoryException("Corrupt event line (missing type prefix): " + line);
        }

        String type = parts[0];
        String data = parts[1];

        try {
            switch (type) {
                case "RACE":
                    return parseRaceEvent(data);
                default:
                    throw new RepositoryException("Unknown event type in file: " + type);
            }
        } catch (Exception e) {
            throw new RepositoryException("Failed to parse event line: " + line, e);
        }
    }

    private String convertRaceEventToString(RaceEvent event) {
        String subscriberIds = event.getSubscriberIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(LIST_SEP));

        String lanesString = event.getLanes().stream()
                .map(Lane::toString)
                .collect(Collectors.joining(LANE_SEP));

        String optimalTimeStr = event.getOptimalTime() != null ?
                event.getOptimalTime().toString() : "null";
        String reportStr = event.getRaceResultReport() != null ?
                event.getRaceResultReport().replace("\n", NL_REPLACE) : "null";

        return "RACE" + SEP +
                event.getId() + SEP +
                event.getOrganizerId() + SEP +
                event.getName() + SEP +
                event.getDescription() + SEP +
                subscriberIds + SEP +
                event.getEventTime().toString() + SEP +
                lanesString + SEP +
                optimalTimeStr + SEP +
                reportStr;
    }

    private RaceEvent parseRaceEvent(String data) {
        String[] parts = data.split(SEP);

        if (parts.length != 9) {
            throw new RepositoryException("Corrupt RaceEvent line, expected 9 data parts: " + data);
        }

        Long id = Long.parseLong(parts[0]);
        Long organizerId = Long.parseLong(parts[1]);
        String name = parts[2];
        String description = parts[3];
        String subscriberIdsString = parts[4];
        LocalDateTime eventTime = LocalDateTime.parse(parts[5]);
        String lanesString = parts[6];
        String optimalTimeStr = parts[7];
        String reportStr = parts[8];

        RaceEvent raceEvent = new RaceEvent(organizerId, description, name, eventTime);
        raceEvent.setId(id);

        if (!subscriberIdsString.isEmpty()) {
            Arrays.stream(subscriberIdsString.split(LIST_SEP))
                    .map(Long::parseLong)
                    .forEach(raceEvent::addSubscriber);
        }

        if (!lanesString.isEmpty()) {
            List<Lane> lanes = Arrays.stream(lanesString.split(LANE_SEP))
                    .map(Lane::fromString)
                    .collect(Collectors.toList());
            raceEvent.setupLanes(lanes);
        }

        if (!optimalTimeStr.equals("null")) {
            raceEvent.setOptimalTime(Double.parseDouble(optimalTimeStr));
        }
        if (!reportStr.equals("null")) {
            raceEvent.setRaceResultReport(reportStr.replace(NL_REPLACE, "\n"));
        }

        return raceEvent;
    }
}