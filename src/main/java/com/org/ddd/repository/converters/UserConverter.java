package com.org.ddd.repository.converters;

import com.org.ddd.domain.entities.*;
import com.org.ddd.repository.exceptions.RepositoryException;

import java.time.LocalDate;

public class UserConverter implements EntityConverter<User> {

    private static final String SEPARATOR = ":::";
    private static final String PERSON_PREFIX = "PERSON";
    private static final String DUCK_PREFIX = "DUCK";

    private final PersonConverter personConverter = new PersonConverter();
    private final DuckConverter duckConverter = new DuckConverter();

    @Override
    public String toLine(User entity) {
        if (entity instanceof Person) {
            return PERSON_PREFIX + SEPARATOR + personConverter.toLine((Person) entity);
        } else if (entity instanceof Duck) {
            return DUCK_PREFIX + SEPARATOR + duckConverter.toLine((Duck) entity);
        }
        throw new RepositoryException("Unknown User type for serialization: " + entity.getClass());
    }

    @Override
    public User fromLine(String line) {
        String[] parts = line.split(SEPARATOR, 2);
        if (parts.length < 2) {
            throw new RepositoryException("Corrupt user line (missing type prefix): " + line);
        }

        String type = parts[0];
        String data = parts[1];

        try {
            switch (type) {
                case PERSON_PREFIX:
                    return personConverter.fromLine(data);
                case DUCK_PREFIX:
                    return duckConverter.fromLine(data);
                default:
                    throw new RepositoryException("Unknown user type in file: " + type);
            }
        } catch (Exception e) {
            throw new RepositoryException("Failed to parse user line: " + line, e);
        }
    }
}