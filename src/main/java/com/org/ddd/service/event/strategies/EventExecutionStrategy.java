package com.org.ddd.service.event.strategies;

import com.org.ddd.domain.entities.Event;
import com.org.ddd.domain.validation.exceptions.ValidationException;
import com.org.ddd.repository.exceptions.RepositoryException;

public interface EventExecutionStrategy {

    String execute(Event event) throws ValidationException, RepositoryException;
}
