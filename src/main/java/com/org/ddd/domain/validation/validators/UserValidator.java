package com.org.ddd.domain.validation.validators;

import com.org.ddd.domain.entities.Duck;
import com.org.ddd.domain.entities.Person;
import com.org.ddd.domain.entities.User;
import com.org.ddd.domain.validation.exceptions.ValidationException;

public class UserValidator implements Validator<User>{

    @Override
    public void validate(User user) throws ValidationException {
        StringBuilder errors = new StringBuilder();
        validateUser(user, errors);

        if (user instanceof Person)
            validatePerson((Person)user, errors);
        else if(user instanceof Duck)
            validateDuck((Duck)user, errors);

        if (!errors.isEmpty()){
            throw new ValidationException(errors.toString());
        }
    }

    private void validateUser(User user, StringBuilder errors) throws ValidationException {
        if (user.getUsername() == null || user.getUsername().isEmpty()){
            errors.append("Username cannot be null or empty!\n");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()){
            errors.append("Email cannot be null or empty!\n");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().length() < 8) {
            errors.append("Password cannot be null, empty, or shorter than 8 characters!\n");
        }
    }

    private void validatePerson(Person person, StringBuilder errors) throws ValidationException {
        if (person.getFirstName() == null || person.getFirstName().isEmpty()){
            errors.append("First name cannot be null or empty!\n");
        }
        if (person.getLastName() == null || person.getLastName().isEmpty()){
            errors.append("Last name cannot be null or empty!\n");
        }
        if (person.getBirthDate() == null){
            errors.append("Birth date cannot be null!\n");
        }
        if (person.getOccupation() == null || person.getOccupation().isEmpty()){
            errors.append("Occupation cannot be null or empty!\n");
        }
        if (person.getEmpathyLevel() < 0 || person.getEmpathyLevel() > 10){
            errors.append("Empathy level must be between 0 and 10!\n");
        }
    }

    private void validateDuck(Duck duck, StringBuilder errors) throws ValidationException{
        if (duck.getSpeed() <= 0){
            errors.append("Speed must be a positive number!\n");
        }
        if (duck.getResistance() <= 0){
            errors.append("Resistance must be a positive number!\n");
        }
    }
}
