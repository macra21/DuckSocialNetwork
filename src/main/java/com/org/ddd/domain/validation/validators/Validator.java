package com.org.ddd.domain.validation.validators;

import com.org.ddd.domain.validation.exceptions.ValidationException;

public interface Validator<E> {
    void validate(E entity) throws ValidationException;
}
