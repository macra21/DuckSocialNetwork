package com.org.ddd.service.strategies; // Pachet nou

import com.org.ddd.domain.entities.*;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.service.event.EventService;
import com.org.ddd.service.event.strategies.EventExecutionStrategy;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RaceExecutionStrategy implements EventExecutionStrategy {
    private final AbstractRepository<Long, User> userRepo;
    private final AbstractRepository<Long, Event> eventRepo;
    private final EventService eventService;

    private List<SwimmingDuck> ducks;
    private List<Lane> bestAssigment;
    private int N, M;
    private double minTime;

    public RaceExecutionStrategy(AbstractRepository<Long, User> userRepo,
                                 AbstractRepository<Long, Event> eventRepo,
                                 EventService eventService) {
        this.userRepo = userRepo;
        this.eventRepo = eventRepo;
        this.eventService = eventService;
    }

    @Override
    public void execute(Event event) throws RepositoryException {
        if (!(event instanceof RaceEvent)) {
            throw new RepositoryException("Invalid event type for RaceExecutionStrategy.");
        }
        RaceEvent raceEvent = (RaceEvent) event;

        List<SwimmingDuck> availableDucks = raceEvent.getSubscriberIds().stream()
                .map(id -> {
                    try { return userRepo.findById(id); }
                    catch (RepositoryException e) { return null; }
                })
                .filter(Objects::nonNull)
                .filter(u -> u instanceof SwimmingDuck)
                .map(u -> (SwimmingDuck) u)
                .filter(d -> d.getDuckType().canSwim())
                .collect(Collectors.toList());

        List<Lane> raceLanes = raceEvent.getLanes();

        this.ducks = availableDucks;
        this.N = this.ducks.size();
        this.M = raceLanes.size();

        if (N < M) {
            eventService.notifySubscribers(event.getId(), "Race '" + event.getName() + "' could not start: not enough participants.");
            return;
        }

        eventService.notifySubscribers(event.getId(), "Race '" + event.getName() + "' is starting!");

        sortDucksByResistance();
        this.minTime = binarySearch(raceLanes);

        String report = generateReport(bestAssigment);
        raceEvent.setOptimalTime(minTime);
        raceEvent.setRaceResultReport(report);

        eventRepo.update(raceEvent);

        eventService.notifySubscribers(event.getId(), "Race finished! Time: " + minTime + "\n" + report);
    }

    private void sortDucksByResistance() {
        this.ducks.sort(Comparator.comparingDouble(SwimmingDuck::getResistance));
    }

    private boolean isValidAssigment(List<Lane> assigment) {
        if (assigment == null || assigment.size() != this.M) return false;
        for (Lane lane : assigment) {
            if (lane == null || lane.getAssignedDuck() == null) return false;
        }
        return true;
    }

    private boolean isPossible(double timeTarget, List<Lane> raceLanes) {
        List<Lane> currentAssigment = new ArrayList<>();
        int duckIndex = 0;

        for (Lane configLane : raceLanes) {
            boolean found = false;
            while (duckIndex < N) {
                SwimmingDuck duck = ducks.get(duckIndex);
                double time = (2.0 * configLane.getDistance()) / duck.getSpeed();

                if (time <= timeTarget) {
                    Lane resultLane = new Lane(configLane.getNumber(), configLane.getDistance());
                    resultLane.setAssignedDuck(duck);
                    resultLane.setTime(time);
                    currentAssigment.add(resultLane);
                    found = true;
                    duckIndex++;
                    break;
                }
                duckIndex++;
            }
            if (!found) return false;
        }

        if (isValidAssigment(currentAssigment)) {
            bestAssigment = currentAssigment;
            return true;
        }
        return false;
    }

    private double binarySearch(List<Lane> raceLanes) {
        double left = 0, right = 2e9, maxError = 1e-4;
        while (right - left > maxError) {
            double mid = (left + right) / 2.0;
            if (isPossible(mid, raceLanes))
                right = mid;
            else
                left = mid;
        }
        return right;
    }

    private String generateReport(List<Lane> assignment) {
        if (assignment == null) return "No valid assignment found.";
        StringBuilder report = new StringBuilder();
        assignment.sort(Comparator.comparingDouble(Lane::getTime));
        for (Lane lane : assignment) {
            report.append("Duck ").append(lane.getAssignedDuck().getId())
                    .append(" on lane ").append(lane.getNumber())
                    .append(": ").append(String.format("%.3f", lane.getTime()))
                    .append(" seconds.\n");
        }
        return report.toString();
    }
}