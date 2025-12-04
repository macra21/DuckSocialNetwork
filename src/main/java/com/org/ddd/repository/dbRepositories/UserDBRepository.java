package com.org.ddd.repository.dbRepositories;


import com.org.ddd.domain.entities.*;
import com.org.ddd.dto.UserFilterDTO;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.PagingRepository;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.utils.paging.Page;
import com.org.ddd.utils.paging.Pageable;
import javafx.util.Pair;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UserDBRepository implements PagingRepository<Long, User> {

    private final String url;
    private final String username;
    private final String password;

    public UserDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public void add(User entity) throws RepositoryException {
        if (entity == null) {
            throw new RepositoryException("Add error: Entity cannot be null!");
        }

        String sql;
        if (entity instanceof Person) {
            sql = "INSERT INTO users (username, email, password, user_type, first_name, last_name, birth_date, occupation, empathy_level) " +
                    "VALUES (?, ?, ?, 'PERSON', ?, ?, ?, ?, ?)";
        } else if (entity instanceof Duck) {
            sql = "INSERT INTO users (username, email, password, user_type, type, speed, resistance, flock_id) " +
                    "VALUES (?, ?, ?, 'DUCK', ?, ?, ?, ?)";
        } else {
            throw new RepositoryException("Unsupported user type");
        }

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entity.getUsername());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getPassword());

            if (entity instanceof Person) {
                Person person = (Person) entity;
                ps.setString(4, person.getFirstName());
                ps.setString(5, person.getLastName());
                ps.setDate(6, Date.valueOf(person.getBirthDate()));
                ps.setString(7, person.getOccupation());
                ps.setInt(8, person.getEmpathyLevel());
            } else if (entity instanceof Duck) {
                Duck duck = (Duck) entity;
                ps.setObject(4, duck.getDuckType(), Types.OTHER);
                ps.setDouble(5, duck.getSpeed());
                ps.setDouble(6, duck.getResistance());
                if (duck.getFlockId() != null) {
                    ps.setLong(7, duck.getFlockId());
                } else {
                    ps.setNull(7, Types.BIGINT);
                }
            }

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
    public void update(User entity) throws RepositoryException {
        if (entity == null || entity.getId() == null) {
            throw new RepositoryException("Update error: Entity or ID cannot be null.");
        }

        String sql;
        if (entity instanceof Person) {
            sql = "UPDATE users SET username = ?, email = ?, password = ?, " +
                    "first_name = ?, last_name = ?, birth_date = ?, occupation = ?, empathy_level = ? " +
                    "WHERE id = ?";
        } else if (entity instanceof Duck) {
            sql = "UPDATE users SET username = ?, email = ?, password = ?, " +
                    "type = ?, speed = ?, resistance = ?, flock_id = ? " +
                    "WHERE id = ?";
        } else {
            throw new RepositoryException("Unsupported user type");
        }

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, entity.getUsername());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getPassword());

            if (entity instanceof Person) {
                Person person = (Person) entity;
                ps.setString(4, person.getFirstName());
                ps.setString(5, person.getLastName());
                ps.setDate(6, Date.valueOf(person.getBirthDate()));
                ps.setString(7, person.getOccupation());
                ps.setInt(8, person.getEmpathyLevel());
                ps.setLong(9, entity.getId());
            } else if (entity instanceof Duck) {
                Duck duck = (Duck) entity;
                ps.setObject(4, duck.getDuckType(), Types.OTHER);
                ps.setDouble(5, duck.getSpeed());
                ps.setDouble(6, duck.getResistance());
                if (duck.getFlockId() != null) {
                    ps.setLong(7, duck.getFlockId());
                } else {
                    ps.setNull(7, Types.BIGINT);
                }
                ps.setLong(8, entity.getId());
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RepositoryException("Update error: Entity with id " + entity.getId() + " not found.");
            }

        } catch (SQLException e) {
            throw new RepositoryException("Update error: " + e.getMessage(), e);
        }
    }

    @Override
    public User delete(Long id) throws RepositoryException {
        if (id == null) {
            throw new RepositoryException("Remove error: ID cannot be null.");
        }

        User userToRemove = findById(id);

        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Remove error: " + e.getMessage(), e);
        }

        return userToRemove;
    }

    @Override
    public User findById(Long id) throws RepositoryException {
        if (id == null) {
            throw new RepositoryException("Search error: ID cannot be null.");
        }

        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseUserFromResultSet(rs);
                } else {
                    throw new RepositoryException("Search error: Entity with id " + id + " not found.");
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Search error: " + e.getMessage(), e);
        }
    }

    @Override
    public Iterable<User> findAll() throws RepositoryException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(parseUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RepositoryException("FindAll error: " + e.getMessage(), e);
        }

        return users;
    }

    private Pair<String, List<Object>> filterToSql(UserFilterDTO filter){
        if (filter == null){
            return new Pair<>("", Collections.emptyList());
        } else {
            List<String> conditions = new ArrayList<>();
            List<Object> params = new ArrayList<>();
            // For User
            filter.getUsername().ifPresent((usernameFilter) -> {
                conditions.add("username like ?");
                params.add(usernameFilter);
            });
            // For Person
            filter.getFirstName().ifPresent((firstNameFilter) -> {
                conditions.add("first_name like ?");
                params.add(firstNameFilter);
            });
            filter.getLastName().ifPresent((lastNameFilter) -> {
                conditions.add("last_name like ?");
                params.add(lastNameFilter);
            });
            filter.getBirthDate().ifPresent((birthDateFilter) -> {
                conditions.add("birth_date = ?");
                params.add(birthDateFilter);
            });
            filter.getOccupation().ifPresent((occupationFilter) -> {
                conditions.add("occupation like ?");
                params.add(occupationFilter);
            });
            filter.getEmpathyLevel().ifPresent((empathyLevelFilter) -> {
                conditions.add("empathy_level = ?");
                params.add(empathyLevelFilter);
            });
            // For Duck
            filter.getSpeed().ifPresent((speedFilter) -> {
                conditions.add("speed = ?");
                params.add(speedFilter);
            });
            filter.getResistance().ifPresent((resistanceFilter) -> {
                conditions.add("resistance = ?");
                params.add(resistanceFilter);
            });
            filter.getFlockId().ifPresent((flockIdFilter) -> {
                conditions.add("flock_id = ?");
                params.add(flockIdFilter);
            });
            filter.getDuckType().ifPresent((duckTypeFilter) -> {
                conditions.add("type = ?");
                params.add(duckTypeFilter);
            });
            String sql = String.join(" and ", conditions);
            return new Pair<>(sql, params);
        }
    }

    private int count(Connection connection, UserFilterDTO filter){
        String sql = "SELECT COUNT(*) AS count FROM users";
        Pair<String, List<Object>> sqlFilter = this.filterToSql(filter);
        if (!((String)sqlFilter.getKey()).isEmpty()){
            sql = sql = " where " + (String)sqlFilter.getKey();
        }

        int cnt;
        try (PreparedStatement ps = connection.prepareStatement(sql)){
            int paramIndex = 0;
            for (Object param: (List)sqlFilter.getValue()){
                ps.setObject(++paramIndex, param);
            }

            try (ResultSet rs = ps.executeQuery()){
                int totalNumberOfUsers = 0;
                if (rs.next()){
                    totalNumberOfUsers = rs.getInt("count");
                }
                cnt = totalNumberOfUsers;

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return cnt;
    }

    private List<User> findAllOnPage(Connection connection, Pageable pageable, UserFilterDTO filter){
        List<User> usersOnPage = new ArrayList<>();
        String sql = "SELECT * FROM users";
        Pair<String, List<Object>> sqlFilter = this.filterToSql(filter);
        if (!((String)sqlFilter.getKey()).isEmpty()){
            sql = sql = " where " + (String)sqlFilter.getKey();
        }
        sql += " LIMIT ? OFFSET ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)){
            int paramIndex = 0;

            for (Object param : (List)sqlFilter.getValue()){
                ps.setObject(++paramIndex, param);
            }

            ps.setInt(++paramIndex, pageable.getPageSize());
            ps.setInt(++paramIndex, pageable.getPageNumber() * pageable.getPageSize());

            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    usersOnPage.add(parseUserFromResultSet(rs));
                }
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }

        return usersOnPage;
    }

    public Page<User> findAllOnPage(Pageable pageable, UserFilterDTO filter){
        try (Connection connection = getConnection()){
            int totalNumberOfUsers = this.count(connection, filter);
            List<User> usersOnPage;
            if (totalNumberOfUsers > 0){
                usersOnPage = this.findAllOnPage(connection, pageable, filter);
            } else {
                usersOnPage = new ArrayList<>();
            }
            return new Page<>(usersOnPage, totalNumberOfUsers);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<User> findAllOnPage(Pageable pageable) {
        return this.findAllOnPage(pageable, (UserFilterDTO)null);
    }

    private User parseUserFromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String userType = rs.getString("user_type");

        User user;

        if ("PERSON".equals(userType)) {
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            LocalDate birthDate = rs.getDate("birth_date").toLocalDate();
            String occupation = rs.getString("occupation");
            int empathyLevel = rs.getInt("empathy_level");

            user = new Person(
                    username, email, password, firstName, lastName,
                    birthDate, occupation, empathyLevel
            );

        } else if ("DUCK".equals(userType)) {
            DuckType type = DuckType.valueOf(rs.getString("type"));
            double speed = rs.getDouble("speed");
            double resistance = rs.getDouble("resistance");
            Long flockId = rs.getLong("flock_id");
            if (rs.wasNull()) {
                flockId = null;
            }

            user = new Duck(
                    username, email, password, speed, type, resistance
            );
            ((Duck) user).setFlockId(flockId);

        } else {
            throw new SQLException("Unknown user type in database: " + userType);
        }

        user.setId(id);
        return user;
    }
}