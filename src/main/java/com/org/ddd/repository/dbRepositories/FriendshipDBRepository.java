package com.org.ddd.repository.dbRepositories;

import com.org.ddd.domain.entities.Friendship;
import com.org.ddd.domain.entities.FriendshipStatus;
import com.org.ddd.dto.FriendshipFilterDTO;
import com.org.ddd.repository.PagingRepository;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.utils.DatabaseConnectionManager;
import com.org.ddd.utils.paging.Page;
import com.org.ddd.utils.paging.Pageable;
import javafx.util.Pair;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendshipDBRepository implements PagingRepository<Long, Friendship> {

    public FriendshipDBRepository() {}

    private Friendship parseFriendshipFromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long userId1 = rs.getLong("user_id1");
        Long userId2 = rs.getLong("user_id2");
        LocalDateTime friendsFrom = rs.getTimestamp("friends_from").toLocalDateTime();
        FriendshipStatus status = FriendshipStatus.valueOf(rs.getString("status"));

        Friendship friendship = new Friendship(userId1, userId2, friendsFrom);
        friendship.setId(id);
        friendship.setStatus(status);
        return friendship;
    }

    @Override
    public void add(Friendship entity) throws RepositoryException {
        if (entity == null) {
            throw new RepositoryException("Add error: Entity cannot be null!");
        }

        String sql = "INSERT INTO friendships (user_id1, user_id2, friends_from, status) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, entity.getUserId1());
            ps.setLong(2, entity.getUserId2());
            ps.setTimestamp(3, Timestamp.valueOf(entity.getFriendsFrom()));
            ps.setString(4, entity.getStatus().name());

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

        String sql = "UPDATE friendships SET user_id1 = ?, user_id2 = ?, friends_from = ?, status = ? WHERE id = ?";

        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, entity.getUserId1());
            ps.setLong(2, entity.getUserId2());
            ps.setTimestamp(3, Timestamp.valueOf(entity.getFriendsFrom()));
            ps.setString(4, entity.getStatus().name());
            ps.setLong(5, entity.getId());

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
        try (Connection connection = DatabaseConnectionManager.getConnection();
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

        try (Connection connection = DatabaseConnectionManager.getConnection();
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

        try (Connection connection = DatabaseConnectionManager.getConnection();
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

    private Pair<String, List<Object>> filterToSql(FriendshipFilterDTO filter) {
        if (filter == null) {
            return new Pair<>("", Collections.emptyList());
        } else {
            List<String> conditions = new ArrayList<>();
            List<Object> params = new ArrayList<>();

            filter.getUserId1().ifPresent(userId1 -> {
                conditions.add("user_id1 = ?");
                params.add(userId1);
            });

            filter.getUserId2().ifPresent(userId2 -> {
                conditions.add("user_id2 = ?");
                params.add(userId2);
            });

            filter.getFriendsFrom().ifPresent(friendsFrom -> {
                conditions.add("friends_from = ?");
                params.add(friendsFrom);
            });

            filter.getStatus().ifPresent(status -> {
                conditions.add("status = ?");
                params.add(status.name());
            });

            filter.getInvolvedUser().ifPresent(userId -> {
                conditions.add("(user_id1 = ? OR user_id2 = ?)");
                params.add(userId);
                params.add(userId);
            });

            String sql = String.join(" AND ", conditions);
            return new Pair<>(sql, params);
        }
    }

    private int count(Connection connection, FriendshipFilterDTO filter) {
        String sql = "SELECT COUNT(*) AS count FROM friendships";
        Pair<String, List<Object>> sqlFilter = this.filterToSql(filter);
        if (!sqlFilter.getKey().isEmpty()) {
            sql = sql + " WHERE " + sqlFilter.getKey();
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int paramIndex = 0;
            for (Object param : sqlFilter.getValue()) {
                ps.setObject(++paramIndex, param);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private List<Friendship> findAllOnPage(Connection connection, Pageable pageable, FriendshipFilterDTO filter) {
        List<Friendship> friendshipsOnPage = new ArrayList<>();
        String sql = "SELECT * FROM friendships";
        Pair<String, List<Object>> sqlFilter = this.filterToSql(filter);
        if (!sqlFilter.getKey().isEmpty()) {
            sql = sql + " WHERE " + sqlFilter.getKey();
        }
        sql += " LIMIT ? OFFSET ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int paramIndex = 0;
            for (Object param : sqlFilter.getValue()) {
                ps.setObject(++paramIndex, param);
            }
            ps.setInt(++paramIndex, pageable.getPageSize());
            ps.setInt(++paramIndex, pageable.getPageNumber() * pageable.getPageSize());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    friendshipsOnPage.add(parseFriendshipFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return friendshipsOnPage;
    }

    public Page<Friendship> findAllOnPage(Pageable pageable, FriendshipFilterDTO filter) {
        try (Connection connection = DatabaseConnectionManager.getConnection()) {
            int total = this.count(connection, filter);
            List<Friendship> friendshipsOnPage = (total > 0) ? this.findAllOnPage(connection, pageable, filter) : new ArrayList<>();
            return new Page<>(friendshipsOnPage, total);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Friendship> findAllOnPage(Pageable pageable) {
        return this.findAllOnPage(pageable, null);
    }
}
