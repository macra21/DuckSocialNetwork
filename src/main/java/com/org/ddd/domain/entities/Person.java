package com.org.ddd.domain.entities;

import java.time.LocalDate;

public class Person extends User{
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String occupation;
    private int empathyLevel;

    public Person(String username, String email, String password, String firstName, String lastName, LocalDate birthDate, String occupation, int empathyLevel) {
        super(username, email, password);
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.occupation = occupation;
        this.empathyLevel = empathyLevel;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public int getEmpathyLevel() {
        return empathyLevel;
    }

    public void setEmpathyLevel(int empathyLevel) {
        this.empathyLevel = empathyLevel;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + getId() +
                ", username='" + getUsername() +
                "', email='" + getEmail() +
                "', firstName='" + firstName +
                "', lastName='" + lastName +
                "', birthDate=" + birthDate +
                "', occupation='" + occupation +
                "', empathyLevel=" + empathyLevel +
                '}';
    }
}
