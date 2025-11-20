package com.org.ddd.repository.dbRepositories;

import com.org.ddd.domain.entities.Message;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.exceptions.RepositoryException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDBRepository implements AbstractRepository<Long, Message> {

    private final String url;
    private final String username;
    private final String password;

    public MessageDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public void add(Message entity) throws RepositoryException {
        if (entity == null) {
            throw new RepositoryException("Add error: Entity cannot be null!");
        }

        String sql = "INSERT INTO messages (sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, entity.getSenderId());
            ps.setLong(2, entity.getReceiverId());
            ps.setString(3, entity.getContent());
            // Convertim LocalDateTime -> Timestamp pentru SQL
            ps.setTimestamp(4, Timestamp.valueOf(entity.getTimestamp()));

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                } else {
                    throw new RepositoryException("Add error: No ID obtained.");
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Add error: " + e.getMessage(), e);
        }
    }

    //TODO: Implement for message editing functionality
    @Override
    public void update(Message entity) throws RepositoryException {
        throw new RepositoryException("Update not implemented yet!");
    }

    @Override
    public Message delete(Long id) throws RepositoryException {
        if (id == null) throw new RepositoryException("ID cannot be null");

        Message messageToDelete = findById(id); // Îl luăm ca să-l returnăm

        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Delete error: " + e.getMessage(), e);
        }
        return messageToDelete;
    }

    @Override
    public Message findById(Long id) throws RepositoryException {
        if (id == null) throw new RepositoryException("ID cannot be null");

        String sql = "SELECT * FROM messages WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseMessage(rs);
                } else {
                    throw new RepositoryException("Message not found id=" + id);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("FindById error: " + e.getMessage(), e);
        }
    }

    @Override
    public Iterable<Message> findAll() throws RepositoryException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                messages.add(parseMessage(rs));
            }

        } catch (SQLException e) {
            throw new RepositoryException("FindAll error: " + e.getMessage(), e);
        }
        return messages;
    }

    private Message parseMessage(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long senderId = rs.getLong("sender_id");
        Long receiverId = rs.getLong("receiver_id");
        String content = rs.getString("content");
        // Convertim Timestamp -> LocalDateTime
        LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();

        Message m = new Message(senderId, receiverId, content);
        m.setId(id);
        m.setTimestamp(timestamp);
        return m;
    }
}