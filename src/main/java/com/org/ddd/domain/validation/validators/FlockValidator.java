package com.org.ddd.domain.validation.validators;

import com.org.ddd.domain.entities.Flock;
import com.org.ddd.domain.validation.exceptions.ValidationException;

public class FlockValidator implements Validator<Flock>{
    @Override
    public void validate(Flock entity) throws ValidationException {
        StringBuilder errors = new StringBuilder();
        if (entity.getName() == null || entity.getName().isEmpty()) {
            errors.append("Flock name cannot be null or empty!\n");
        }
        if (entity.getFlockPurpose() == null) {
            errors.append("Flock purpose cannot be null!\n");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString());
        }
    }
}
