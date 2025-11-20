package com.org.ddd.service.event;

import com.org.ddd.domain.entities.*;
import com.org.ddd.domain.validation.exceptions.ValidationException;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.service.event.strategies.EventExecutionStrategy;
import com.org.ddd.service.exceptions.ServiceException;
import com.org.ddd.service.strategies.RaceExecutionStrategy;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventService {

    private final AbstractRepository<Long, Event> eventRepo;
    private final AbstractRepository<Long, User> userRepo;

    private final Map<Class<? extends Event>, EventExecutionStrategy> executionStrategies;

    public EventService(AbstractRepository<Long, Event> eventRepo, AbstractRepository<Long, User> userRepo) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;

        this.executionStrategies = new HashMap<>();
        this.executionStrategies.put(
                RaceEvent.class,
                new RaceExecutionStrategy(userRepo, eventRepo)
        );
    }

    public Event createRaceEvent(Long organizerId, String name, String description, LocalDateTime eventTime, List<Lane> lanes) throws ValidationException, RepositoryException {

        User organizer = userRepo.findById(organizerId);
        if (!(organizer instanceof Person)) {
            throw new ValidationException("Only Persons can create events.");
        }

        RaceEvent raceEvent = new RaceEvent(organizerId, description, name, eventTime);
        raceEvent.setupLanes(lanes);

        eventRepo.add(raceEvent);
        return raceEvent;
    }

    public Event deleteEvent(Long eventId) throws RepositoryException {
        return eventRepo.delete(eventId);
    }

    public Event findEvent(Long eventId) throws RepositoryException{
        return eventRepo.findById(eventId);
    }

    public Iterable<Event> findAll(){
        return eventRepo.findAll();
    }

    public void executeEvent(Long eventId) {
        Event event = eventRepo.findById(eventId);

        EventExecutionStrategy strategy = executionStrategies.get(event.getClass());

        if (strategy == null) {
            throw new ServiceException("No execution strategy found for event type: " + event.getClass().getSimpleName());
        }

        notifySubscribers(eventId, "Race '" + event.getName() + "' is starting!");

        String executionReport = strategy.execute(event);

        notifySubscribers(eventId, executionReport);
    }

    public void subscribe(Long userId, Long eventId) {
        Event event = eventRepo.findById(eventId);
        userRepo.findById(userId);

        event.addSubscriber(userId);
        eventRepo.update(event);
    }

    public void unsubscribe(Long userId, Long eventId) {
        Event event = eventRepo.findById(eventId);
        event.removeSubscriber(userId);
        eventRepo.update(event);
    }

    public void notifySubscribers(Long eventId, String message) {
        Event event = eventRepo.findById(eventId);
        for (Long subId : event.getSubscriberIds()) {
            System.out.println("Notification to user " + subId + ": " + message);
        }
    }
}