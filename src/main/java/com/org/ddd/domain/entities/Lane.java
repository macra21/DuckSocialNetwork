package com.org.ddd.domain.entities;

import com.org.ddd.repository.exceptions.RepositoryException;

public class Lane {
    private final int number;
    private final double distance;

    private transient Duck assignedDuck;
    private transient double time;

    public Lane(int number, double distance) {
        this.number = number;
        this.distance = distance;
    }

    public int getNumber() {
        return number;
    }

    public double getDistance() {
        return distance;
    }

    public void setAssignedDuck(Duck duck) {
        this.assignedDuck = duck;
    }

    public Duck getAssignedDuck() {
        return assignedDuck;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getTime() {
        return time;
    }

    @Override
    public String toString() {
        return number + ":" + distance;
    }

    public static Lane fromString(String line) {
        String[] parts = line.split(":");
        if (parts.length != 2) {
            throw new RepositoryException("Corrupt Lane data: " + line);
        }
        try {
            return new Lane(Integer.parseInt(parts[0]), Double.parseDouble(parts[1]));
        } catch (NumberFormatException e) {
            throw new RepositoryException("Corrupt numeric data in Lane: " + line, e);
        }
    }
}