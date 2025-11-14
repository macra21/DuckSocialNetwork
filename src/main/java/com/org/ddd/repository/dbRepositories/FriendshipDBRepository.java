package com.org.ddd.repository.dbRepositories;

import com.org.ddd.domain.entities.Friendship;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.exceptions.RepositoryException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FriendshipDBRepository implements AbstractRepository<Long, Friendship> {

    private final String url;
    private final String username;
    private final String password;

    public FriendshipDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private Friendship parseFriendshipFromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long userId1 = rs.getLong("user_id1");
        Long userId2 = rs.getLong("user_id2");
        LocalDateTime friendsFrom = rs.getTimestamp("friends_from").toLocalDateTime();

        Friendship friendship = new Friendship(userId1, userId2);
        friendship.setId(id);
        friendship.setFriendsFrom(friendsFrom);
        return friendship;
    }

    @Override
    public void add(Friendship entity) throws RepositoryException {
        if (entity == null) {
            throw new RepositoryException("Add error: Entity cannot be null!");
        }

        String sql = "INSERT INTO friendships (user_id1, user_id2, friends_from) VALUES (?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, entity.getUserId1());
            ps.setLong(2, entity.getUserId2());
            ps.setTimestamp(3, Timestamp.valueOf(entity.getFriendsFrom()));

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                } else {
                    throw new RepositoryException("Add error: Failed to retrieve generated ID.");
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Add error: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Friendship entity) throws RepositoryException {
        if (entity == null || entity.getId() == null) {
            throw new RepositoryException("Update error: Entity or ID cannot be null.");
        }

        String sql = "UPDATE friendships SET user_id1 = ?, user_id2 = ?, friends_from = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, entity.getUserId1());
            ps.setLong(2, entity.getUserId2());
            ps.setTimestamp(3, Timestamp.valueOf(entity.getFriendsFrom()));
            ps.setLong(4, entity.getId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RepositoryException("Update error: Entity with id " + entity.getId() + " not found.");
            }
        } catch (SQLException e) {
            throw new RepositoryException("Update error: " + e.getMessage(), e);
        }
    }

    @Override
    public Friendship delete(Long id) throws RepositoryException {
        if (id == null) {
            throw new RepositoryException("Remove error: ID cannot be null.");
        }

        Friendship friendshipToRemove = findById(id);

        String sql = "DELETE FROM friendships WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RepositoryException("Remove error: Entity with id " + id + " not found.");
            }

        } catch (SQLException e) {
            throw new RepositoryException("Remove error: " + e.getMessage(), e);
        }

        return friendshipToRemove;
    }

    @Override
    public Friendship findById(Long id) throws RepositoryException {
        if (id == null) {
            throw new RepositoryException("Search error: ID cannot be null.");
        }

        String sql = "SELECT * FROM friendships WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseFriendshipFromResultSet(rs);
                } else {
                    throw new RepositoryException("Search error: Entity with id " + id + " not found.");
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Search error: " + e.getMessage(), e);
        }
    }

    @Override
    public Iterable<Friendship> findAll() throws RepositoryException {
        List<Friendship> friendships = new ArrayList<>();
        String sql = "SELECT * FROM friendships";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                friendships.add(parseFriendshipFromResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RepositoryException("FindAll error: " + e.getMessage(), e);
        }

        return friendships;
    }
}