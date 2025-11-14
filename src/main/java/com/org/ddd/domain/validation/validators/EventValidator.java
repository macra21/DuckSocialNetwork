package com.org.ddd.domain.validation.validators;

import com.org.ddd.domain.entities.Event;
import com.org.ddd.domain.entities.RaceEvent;
import com.org.ddd.domain.validation.exceptions.ValidationException;

import java.time.LocalDateTime;

public class EventValidator implements Validator<Event> {

    @Override
    public void validate(Event entity) throws ValidationException {
        StringBuilder errors = new StringBuilder();

        validateBaseEvent(entity, errors);

        if (entity instanceof RaceEvent) {
            validateRaceEvent((RaceEvent) entity, errors);
        }

        if (errors.length() > 0) {
            throw new ValidationException(errors.toString());
        }
    }

    private void validateBaseEvent(Event event, StringBuilder errors) {
        if (event.getOrganizerId() == null) {
            errors.append("Event must have an organizer ID.\n");
        }

        if (event.getName() == null || event.getName().isBlank()) {
            errors.append("Event name cannot be null or empty.\n");
        }

        if (event.getDescription() == null || event.getDescription().isBlank()) {
            errors.append("Event description cannot be null or empty.\n");
        }
    }

    private void validateRaceEvent(RaceEvent raceEvent, StringBuilder errors) {
        if (raceEvent.getEventTime() == null) {
            errors.append("RaceEvent must have an event time.\n");
        }

        if (raceEvent.getEventTime() != null &&
                raceEvent.getEventTime().isBefore(LocalDateTime.now())) {
            errors.append("Event time cannot be set in the past.\n");
        }

        if (raceEvent.getLanes().isEmpty()) {
            errors.append("RaceEvent must be configured with at least one lane.\n");
        }
    }
}