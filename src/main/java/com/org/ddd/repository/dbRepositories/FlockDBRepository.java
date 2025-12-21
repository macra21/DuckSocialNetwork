package com.org.ddd.repository.dbRepositories;

import com.org.ddd.domain.entities.Flock;
import com.org.ddd.domain.entities.FlockPurpose;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.utils.DatabaseConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlockDBRepository implements AbstractRepository<Long, Flock> {

    public FlockDBRepository() {}

    @Override
    public void add(Flock entity) throws RepositoryException {
        if (entity == null) {
            throw new RepositoryException("Add error: Entity cannot be null!");
        }

        String sqlFlock = "INSERT INTO flocks (name, purpose, created_at) VALUES (?, ?, ?)";
        String sqlMember = "INSERT INTO flock_members (flock_id, duck_id) VALUES (?, ?)";

        try (Connection connection = DatabaseConnectionManager.getConnection()) {
            Long generatedId = null;
            try (PreparedStatement ps = connection.prepareStatement(sqlFlock, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, entity.getName());
                ps.setString(2, entity.getFlockPurpose().toString());
                ps.setTimestamp(3, Timestamp.valueOf(entity.getCreationTime()));

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getLong(1);
                        entity.setId(generatedId);
                    } else {
                        throw new RepositoryException("Add error: No ID obtained for flock.");
                    }
                }
            }

            if (!entity.getMemberIds().isEmpty()) {
                try (PreparedStatement psMembers = connection.prepareStatement(sqlMember)) {
                    for (Long duckId : entity.getMemberIds()) {
                        psMembers.setLong(1, generatedId);
                        psMembers.setLong(2, duckId);
                        psMembers.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Add error: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Flock entity) throws RepositoryException {
        throw new RepositoryException("Operation not supported!\n");
    }

    @Override
    public Flock delete(Long id) throws RepositoryException {
        if (id == null) throw new RepositoryException("ID cannot be null");

        Flock flockToDelete = findById(id);

        String sql = "DELETE FROM flocks WHERE id = ?";

        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Delete error: " + e.getMessage(), e);
        }
        return flockToDelete;
    }

    @Override
    public Flock findById(Long id) throws RepositoryException {
        if (id == null) throw new RepositoryException("ID cannot be null");

        String sql = "SELECT * FROM flocks WHERE id = ?";

        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Flock flock = parseFlock(rs);
                    loadMembers(flock, connection);
                    return flock;
                } else {
                    throw new RepositoryException("Flock not found id=" + id);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("FindById error: " + e.getMessage(), e);
        }
    }

    @Override
    public Iterable<Flock> findAll() throws RepositoryException {
        List<Flock> flocks = new ArrayList<>();
        String sql = "SELECT * FROM flocks";

        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Flock flock = parseFlock(rs);
                loadMembers(flock, connection);
                flocks.add(flock);
            }
        } catch (SQLException e) {
            throw new RepositoryException("FindAll error: " + e.getMessage(), e);
        }
        return flocks;
    }

    private Flock parseFlock(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        String purposeStr = rs.getString("purpose");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

        FlockPurpose purpose = FlockPurpose.valueOf(purposeStr);
        Flock flock = new Flock(name, purpose);
        flock.setId(id);
        flock.setCreationTime(createdAt);
        return flock;
    }

    private void loadMembers(Flock flock, Connection conn) throws SQLException {
        String sql = "SELECT duck_id FROM flock_members WHERE flock_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, flock.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    flock.addMemberId(rs.getLong("duck_id"));
                }
            }
        }
    }
}
