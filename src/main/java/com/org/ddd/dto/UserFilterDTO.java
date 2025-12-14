package com.org.ddd.dto;

import com.org.ddd.domain.entities.DuckType;
import com.org.ddd.domain.entities.User;

import java.time.LocalDate;
import java.util.Optional;

public class UserFilterDTO {
    // For filtering by entity type
    private Optional<Class<? extends User>> userClass = Optional.empty();

    // For User
    private Optional<String> username = Optional.empty();
    private Optional<String> email = Optional.empty(); // Added email field

    // For Person
    private Optional<String> firstName = Optional.empty();
    private Optional<String> lastName = Optional.empty();
    private Optional<LocalDate> birthDate = Optional.empty();
    private Optional<String> occupation = Optional.empty();
    private Optional<Integer> empathyLevel = Optional.empty();

    // For Duck
    private Optional<Double> speed = Optional.empty();
    private Optional<Double> resistance = Optional.empty();
    private Optional<Long> flockId = Optional.empty();
    private Optional<DuckType> duckType = Optional.empty();

    public Optional<Class<? extends User>> getUserClass() {
        return userClass;
    }

    public void setUserClass(Optional<Class<? extends User>> userClass) {
        this.userClass = userClass;
    }

    public Optional<String> getUsername() {
        return username;
    }

    public void setUsername(Optional<String> username) {
        this.username = username;
    }

    public Optional<String> getEmail() {
        return email;
    }

    public void setEmail(Optional<String> email) {
        this.email = email;
    }

    public Optional<String> getFirstName() {
        return firstName;
    }

    public void setFirstName(Optional<String> firstName) {
        this.firstName = firstName;
    }

    public Optional<String> getLastName() {
        return lastName;
    }

    public void setLastName(Optional<String> lastName) {
        this.lastName = lastName;
    }

    public Optional<LocalDate> getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Optional<LocalDate> birthDate) {
        this.birthDate = birthDate;
    }

    public Optional<String> getOccupation() {
        return occupation;
    }

    public void setOccupation(Optional<String> occupation) {
        this.occupation = occupation;
    }

    public Optional<Integer> getEmpathyLevel() {
        return empathyLevel;
    }

    public void setEmpathyLevel(Optional<Integer> empathyLevel) {
        this.empathyLevel = empathyLevel;
    }

    public Optional<Double> getSpeed() {
        return speed;
    }

    public void setSpeed(Optional<Double> speed) {
        this.speed = speed;
    }

    public Optional<Double> getResistance() {
        return resistance;
    }

    public void setResistance(Optional<Double> resistance) {
        this.resistance = resistance;
    }

    public Optional<Long> getFlockId() {
        return flockId;
    }

    public void setFlockId(Optional<Long> flockId) {
        this.flockId = flockId;
    }

    public Optional<DuckType> getDuckType() {
        return duckType;
    }

    public void setDuckType(Optional<DuckType> duckType) {
        this.duckType = duckType;
    }
}
