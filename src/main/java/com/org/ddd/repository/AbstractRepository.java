package com.org.ddd.repository;

import com.org.ddd.domain.entities.Entity;
import com.org.ddd.repository.exceptions.RepositoryException;

public interface AbstractRepository<ID, E extends Entity<ID>> {
    void add(E entity) throws RepositoryException;

    void update(E entity) throws RepositoryException;

    E delete(ID id) throws RepositoryException;

    E findById(ID id) throws RepositoryException;

    Iterable<E> findAll();
}
