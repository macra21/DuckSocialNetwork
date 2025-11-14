
package com.org.ddd.repository.converters;

import com.org.ddd.domain.entities.Person;
import com.org.ddd.repository.exceptions.RepositoryException;

import java.time.LocalDate;

public class PersonConverter implements EntityConverter<Person> {
    private static final String SEPARATOR = ":::";

    @Override
    public String toLine(Person entity) {
        return entity.getId() + SEPARATOR +
                entity.getUsername() + SEPARATOR +
                entity.getEmail() + SEPARATOR +
                entity.getPassword() + SEPARATOR +
                entity.getFirstName() + SEPARATOR +
                entity.getLastName() + SEPARATOR +
                entity.getBirthDate() + SEPARATOR +
                entity.getOccupation() + SEPARATOR +
                entity.getEmpathyLevel();
    }

    @Override
    public Person fromLine(String line) throws RepositoryException {
        String[] parts = line.split(SEPARATOR);

        if (parts.length != 9) {
            throw new RepositoryException("Corrupt line or invalid format in file.\n");
        }

        Person person = new Person(
                parts[1], // username
                parts[2], // email
                parts[3], // password
                parts[4], // firstName
                parts[5], // lastName
                LocalDate.parse(parts[6]), // birthDate
                parts[7], // occupation
                Integer.parseInt(parts[8]) // empathyLevel
        );
        person.setId(Long.parseLong(parts[0]));
        return person;
    }
}