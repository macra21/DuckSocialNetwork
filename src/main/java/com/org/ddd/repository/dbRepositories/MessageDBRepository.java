package com.org.ddd.repository.dbRepositories;

import com.org.ddd.domain.entities.Message;
import com.org.ddd.dto.MessageFilterDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDBRepository implements PagingRepository<Long, Message> {

    public MessageDBRepository() {}

    @Override
    public void add(Message entity) throws RepositoryException {
        if (entity == null) throw new RepositoryException("Entity cannot be null!");

        String msgSql = "INSERT INTO messages (from_user_id, message, date, reply_id) VALUES (?, ?, ?, ?)";
        String rcptSql = "INSERT INTO message_recipients (message_id, recipient_user_id) VALUES (?, ?)";

        try (Connection connection = DatabaseConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                long msgId;
                try (PreparedStatement ps = connection.prepareStatement(msgSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, entity.getSenderId());
                    ps.setString(2, entity.getContent());
                    ps.setTimestamp(3, Timestamp.valueOf(entity.getTimestamp()));
                    if (entity.getReplyId() != null) ps.setLong(4, entity.getReplyId());
                    else ps.setNull(4, Types.BIGINT);
                    ps.executeUpdate();
                    
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) msgId = rs.getLong(1);
                        else throw new SQLException("No ID generated");
                    }
                    entity.setId(msgId);
                }

                try (PreparedStatement ps = connection.prepareStatement(rcptSql)) {
                    for (Long recipientId : entity.getReceiversIdList()) {
                        ps.setLong(1, msgId);
                        ps.setLong(2, recipientId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error saving message: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Message entity) throws RepositoryException {
        if (entity == null || entity.getId() == null) throw new RepositoryException("Entity or ID cannot be null!");

        String sql = "UPDATE messages SET message = ? WHERE id = ?";
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setString(1, entity.getContent());
            ps.setLong(2, entity.getId());
            
            int affected = ps.executeUpdate();
            if (affected == 0) throw new RepositoryException("Message with id " + entity.getId() + " not found.");
            
        } catch (SQLException e) {
            throw new RepositoryException("Update error: " + e.getMessage(), e);
        }
    }

    @Override
    public Message delete(Long id) throws RepositoryException {
        if (id == null) throw new RepositoryException("ID cannot be null!");
        
        Message msg = findById(id);
        if (msg == null) throw new RepositoryException("Message not found!");

        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Delete error: " + e.getMessage(), e);
        }
        return msg;
    }

    @Override
    public Message findById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM messages WHERE id = ?";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractMessage(connection, rs);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("FindById error: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Iterable<Message> findAll() throws RepositoryException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages ORDER BY date DESC";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                messages.add(extractMessage(connection, rs));
            }
        } catch (SQLException e) {
            throw new RepositoryException("FindAll error: " + e.getMessage(), e);
        }
        return messages;
    }

    private Pair<String, List<Object>> filterToSql(MessageFilterDTO filter) {
        if (filter == null) return new Pair<>("", Collections.emptyList());

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (filter.getUser1().isPresent() && filter.getUser2().isPresent()) {
            conditions.add("( (m.from_user_id = ? AND mr.recipient_user_id = ?) OR (m.from_user_id = ? AND mr.recipient_user_id = ?) )");
            params.add(filter.getUser1().get());
            params.add(filter.getUser2().get());
            params.add(filter.getUser2().get());
            params.add(filter.getUser1().get());
        }

        filter.getSenderId().ifPresent(senderId -> {
            conditions.add("m.from_user_id = ?");
            params.add(senderId);
        });

        filter.getReceiversIdList().ifPresent(receivers -> {
            if (!receivers.isEmpty()) {
                StringBuilder inClause = new StringBuilder("mr.recipient_user_id IN (");
                for (int i = 0; i < receivers.size(); i++) {
                    inClause.append("?");
                    if (i < receivers.size() - 1) inClause.append(",");
                    params.add(receivers.get(i));
                }
                inClause.append(")");
                conditions.add(inClause.toString());
            }
        });

        filter.getSeen().ifPresent(seen -> {
            if (seen) {
                conditions.add("mr.seen_date IS NOT NULL");
            } else {
                conditions.add("mr.seen_date IS NULL");
            }
        });

        String sql = String.join(" AND ", conditions);
        return new Pair<>(sql, params);
    }

    private int count(Connection connection, MessageFilterDTO filter) {
        String sql = "SELECT COUNT(DISTINCT m.id) AS count FROM messages m " +
                     "JOIN message_recipients mr ON m.id = mr.message_id";
        
        Pair<String, List<Object>> sqlFilter = this.filterToSql(filter);
        if (!sqlFilter.getKey().isEmpty()) {
            sql += " WHERE " + sqlFilter.getKey();
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 0;
            for (Object param : sqlFilter.getValue()) {
                ps.setObject(++index, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("count");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private List<Message> findAllOnPage(Connection connection, Pageable pageable, MessageFilterDTO filter) {
        List<Message> messages = new ArrayList<>();
        
        String sql = "SELECT DISTINCT m.* FROM messages m " +
                     "JOIN message_recipients mr ON m.id = mr.message_id";
        
        Pair<String, List<Object>> sqlFilter = this.filterToSql(filter);
        if (!sqlFilter.getKey().isEmpty()) {
            sql += " WHERE " + sqlFilter.getKey();
        }
        
        sql += " ORDER BY m.date DESC LIMIT ? OFFSET ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 0;
            for (Object param : sqlFilter.getValue()) {
                ps.setObject(++index, param);
            }
            ps.setInt(++index, pageable.getPageSize());
            ps.setInt(++index, pageable.getPageNumber() * pageable.getPageSize());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(extractMessage(connection, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return messages;
    }

    @Override
    public Page<Message> findAllOnPage(Pageable pageable) {
        return findAllOnPage(pageable, null);
    }

    public Page<Message> findAllOnPage(Pageable pageable, MessageFilterDTO filter) {
        try (Connection connection = DatabaseConnectionManager.getConnection()) {
            int total = count(connection, filter);
            List<Message> data = (total > 0) ? findAllOnPage(connection, pageable, filter) : new ArrayList<>();
            return new Page<>(data, total);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void markAsRead(Long messageId, Long userId) {
        String sql = "UPDATE message_recipients SET seen_date = ? WHERE message_id = ? AND recipient_user_id = ?";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, messageId);
            ps.setLong(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Error marking as read: " + e.getMessage(), e);
        }
    }

    public Map<Long, Integer> getUnreadMessagesSummary(Long forUser) {
        Map<Long, Integer> summary = new HashMap<>();
        String sql = "SELECT m.from_user_id, COUNT(*) as unread_count FROM message_recipients mr " +
                     "JOIN messages m ON mr.message_id = m.id " +
                     "WHERE mr.recipient_user_id = ? AND mr.seen_date IS NULL " +
                     "GROUP BY m.from_user_id";
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, forUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    summary.put(rs.getLong("from_user_id"), rs.getInt("unread_count"));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Get unread summary error: " + e.getMessage(), e);
        }
        return summary;
    }

    private Message extractMessage(Connection connection, ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long senderId = rs.getLong("from_user_id");
        String content = rs.getString("message");
        LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
        Long replyId = rs.getLong("reply_id");
        if (rs.wasNull()) replyId = null;

        List<Long> recipients = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT recipient_user_id FROM message_recipients WHERE message_id = ?")) {
            ps.setLong(1, id);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) recipients.add(r.getLong(1));
            }
        }

        return new Message(id, senderId, recipients, content, date, replyId);
    }
}
