package com.org.ddd.repository.dbRepositories;

import com.org.ddd.domain.entities.Event;
import com.org.ddd.domain.entities.Lane;
import com.org.ddd.domain.entities.RaceEvent;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.utils.DatabaseConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RaceEventDBRepository implements AbstractRepository<Long, Event> {

    public RaceEventDBRepository() {
    }

    @Override
    public void add(Event entity) throws RepositoryException {
        if (entity == null) throw new RepositoryException("Entity cannot be null");
        if (!(entity instanceof RaceEvent)) throw new RepositoryException("Repository only supports RaceEvent");

        RaceEvent raceEvent = (RaceEvent) entity;

        String sqlEvent = "INSERT INTO race_events (name, description, organizer_id, event_time, created_at, optimal_time, report) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlSubscribers = "INSERT INTO event_subscribers (event_id, user_id) VALUES (?, ?)";
        String sqlLanes = "INSERT INTO race_lanes (event_id, lane_number, distance, assigned_duck_id, time_result) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnectionManager.getConnection()) {
            Long generatedId = null;
            try (PreparedStatement ps = connection.prepareStatement(sqlEvent, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, raceEvent.getName());
                ps.setString(2, raceEvent.getDescription());
                ps.setLong(3, raceEvent.getOrganizerId());
                ps.setTimestamp(4, Timestamp.valueOf(raceEvent.getEventTime()));
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

                if (raceEvent.getOptimalTime() != null) ps.setDouble(6, raceEvent.getOptimalTime());
                else ps.setNull(6, Types.DOUBLE);

                ps.setString(7, raceEvent.getRaceResultReport());

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getLong(1);
                        raceEvent.setId(generatedId);
                    } else {
                        throw new RepositoryException("No ID obtained for RaceEvent.");
                    }
                }
            }

            if (!raceEvent.getSubscriberIds().isEmpty()) {
                try (PreparedStatement ps = connection.prepareStatement(sqlSubscribers)) {
                    for (Long userId : raceEvent.getSubscriberIds()) {
                        ps.setLong(1, generatedId);
                        ps.setLong(2, userId);
                        ps.executeUpdate();
                    }
                }
            }

            if (!raceEvent.getLanes().isEmpty()) {
                try (PreparedStatement ps = connection.prepareStatement(sqlLanes)) {
                    for (Lane lane : raceEvent.getLanes()) {
                        ps.setLong(1, generatedId);
                        ps.setInt(2, lane.getNumber());
                        ps.setDouble(3, lane.getDistance());

                        if (lane.getAssignedDuck() != null) {
                            ps.setLong(4, lane.getAssignedDuck().getId());
                        } else {
                            ps.setNull(4, Types.BIGINT);
                        }

                        ps.setDouble(5, lane.getTime());
                        ps.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Add RaceEvent error: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Event entity) throws RepositoryException {
        if (entity == null || entity.getId() == null) throw new RepositoryException("ID required for update");
        if (!(entity instanceof RaceEvent)) throw new RepositoryException("Repository only supports RaceEvent");

        RaceEvent raceEvent = (RaceEvent) entity;

        String sqlUpdate = "UPDATE race_events SET name=?, description=?, organizer_id=?, event_time=?, optimal_time=?, report=? WHERE id=?";
        String sqlDelSubs = "DELETE FROM event_subscribers WHERE event_id=?";
        String sqlInsSubs = "INSERT INTO event_subscribers (event_id, user_id) VALUES (?, ?)";
        String sqlDelLanes = "DELETE FROM race_lanes WHERE event_id=?";
        String sqlInsLanes = "INSERT INTO race_lanes (event_id, lane_number, distance, assigned_duck_id, time_result) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnectionManager.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sqlUpdate)) {
                ps.setString(1, raceEvent.getName());
                ps.setString(2, raceEvent.getDescription());
                ps.setLong(3, raceEvent.getOrganizerId());
                ps.setTimestamp(4, Timestamp.valueOf(raceEvent.getEventTime()));

                if (raceEvent.getOptimalTime() != null) ps.setDouble(5, raceEvent.getOptimalTime());
                else ps.setNull(5, Types.DOUBLE);

                ps.setString(6, raceEvent.getRaceResultReport());
                ps.setLong(7, raceEvent.getId());

                int rows = ps.executeUpdate();
                if (rows == 0) throw new RepositoryException("Event not found id=" + raceEvent.getId());
            }

            try (PreparedStatement ps = connection.prepareStatement(sqlDelSubs)) {
                ps.setLong(1, raceEvent.getId());
                ps.executeUpdate();
            }
            if (!raceEvent.getSubscriberIds().isEmpty()) {
                try (PreparedStatement ps = connection.prepareStatement(sqlInsSubs)) {
                    for (Long userId : raceEvent.getSubscriberIds()) {
                        ps.setLong(1, raceEvent.getId());
                        ps.setLong(2, userId);
                        ps.executeUpdate();
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(sqlDelLanes)) {
                ps.setLong(1, raceEvent.getId());
                ps.executeUpdate();
            }
            if (!raceEvent.getLanes().isEmpty()) {
                try (PreparedStatement ps = connection.prepareStatement(sqlInsLanes)) {
                    for (Lane lane : raceEvent.getLanes()) {
                        ps.setLong(1, raceEvent.getId());
                        ps.setInt(2, lane.getNumber());
                        ps.setDouble(3, lane.getDistance());

                        if (lane.getAssignedDuck() != null) {
                            ps.setLong(4, lane.getAssignedDuck().getId());
                        } else {
                            ps.setNull(4, Types.BIGINT);
                        }

                        ps.setDouble(5, lane.getTime());
                        ps.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Update error: " + e.getMessage(), e);
        }
    }

    @Override
    public Event delete(Long id) throws RepositoryException {
        if (id == null) throw new RepositoryException("ID cannot be null");
        Event eventToDelete = findById(id);

        String sql = "DELETE FROM race_events WHERE id = ?";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Delete error: " + e.getMessage(), e);
        }
        return eventToDelete;
    }

    @Override
    public Event findById(Long id) throws RepositoryException {
        if (id == null) throw new RepositoryException("ID cannot be null");
        String sql = "SELECT * FROM race_events WHERE id = ?";

        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RaceEvent event = parseEvent(rs);
                    loadSubscribers(event, connection);
                    loadLanes(event, connection);
                    return event;
                } else {
                    throw new RepositoryException("RaceEvent not found id=" + id);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("FindById error: " + e.getMessage(), e);
        }
    }

    @Override
    public Iterable<Event> findAll() throws RepositoryException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM race_events";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RaceEvent event = parseEvent(rs);
                loadSubscribers(event, connection);
                loadLanes(event, connection);
                events.add(event);
            }
        } catch (SQLException e) {
            throw new RepositoryException("FindAll error: " + e.getMessage(), e);
        }
        return events;
    }

    private RaceEvent parseEvent(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        Long organizerId = rs.getLong("organizer_id");
        LocalDateTime eventTime = rs.getTimestamp("event_time").toLocalDateTime();

        Double optimalTime = rs.getDouble("optimal_time");
        if (rs.wasNull()) optimalTime = null;

        String report = rs.getString("report");

        RaceEvent event = new RaceEvent(organizerId, description, name, eventTime);
        event.setId(id);
        event.setOptimalTime(optimalTime);
        event.setRaceResultReport(report);
        return event;
    }

    private void loadSubscribers(RaceEvent event, Connection conn) throws SQLException {
        String sql = "SELECT user_id FROM event_subscribers WHERE event_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, event.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    event.addSubscriber(rs.getLong("user_id"));
                }
            }
        }
    }

    private void loadLanes(RaceEvent event, Connection conn) throws SQLException {
        String sql = "SELECT * FROM race_lanes WHERE event_id = ?";
        List<Lane> lanes = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, event.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int number = rs.getInt("lane_number");
                    double distance = rs.getDouble("distance");
                    double time = rs.getDouble("time_result");

                    Lane lane = new Lane(number, distance);
                    lane.setTime(time);
                    lanes.add(lane);
                }
            }
        }
        event.setupLanes(lanes);
    }
}
