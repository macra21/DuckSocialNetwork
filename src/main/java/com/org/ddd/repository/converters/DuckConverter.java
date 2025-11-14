package com.org.ddd.repository.converters;

import com.org.ddd.domain.entities.*;
import com.org.ddd.repository.exceptions.RepositoryException;

public class DuckConverter implements EntityConverter<Duck> {
    private static final String SEPARATOR = ":::";

    @Override
    public String toLine(Duck entity) {
        return entity.getId() + SEPARATOR +
                entity.getUsername() + SEPARATOR +
                entity.getEmail() + SEPARATOR +
                entity.getPassword() + SEPARATOR +
                entity.getDuckType().name() + SEPARATOR +
                entity.getSpeed() + SEPARATOR +
                entity.getResistance() + SEPARATOR +
                (entity.getFlockId() != null ? entity.getFlockId() : "null");
    }

    @Override
    public Duck fromLine(String line) throws RepositoryException {
        String[] parts = line.split(SEPARATOR);

        if (parts.length != 8) {
            throw new RepositoryException("Corrupt line or invalid format in file. Expected 8 parts, got " + parts.length + ": " + line);
        }

        try {
            DuckType duckType = DuckType.valueOf(parts[4]);
            Duck duck;

            // Constructor: (username, email, password, speed, duckType, resistance)
            if (duckType == DuckType.FLYING) {
                duck = new FlyingDuck(parts[1], parts[2], parts[3],
                        Double.parseDouble(parts[5]), duckType, Double.parseDouble(parts[6]));
            } else if (duckType == DuckType.SWIMMING) {
                duck = new SwimmingDuck(parts[1], parts[2], parts[3],
                        Double.parseDouble(parts[5]), duckType, Double.parseDouble(parts[6]));
            } else if (duckType == DuckType.FLYING_AND_SWIMMING) {
                duck = new FlyingAndSwimingDuck(parts[1], parts[2], parts[3],
                        Double.parseDouble(parts[5]), duckType, Double.parseDouble(parts[6]));
            } else {
                throw new RepositoryException("Unknown duck type found: " + duckType);
            }

            duck.setId(Long.parseLong(parts[0]));
            if (!parts[7].equals("null")) {
                duck.setFlockId(Long.parseLong(parts[7]));
            }
            return duck;

        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Invalid data format in line: " + line, e);
        }
    }
}