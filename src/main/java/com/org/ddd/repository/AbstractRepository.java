package com.org.ddd.repository;

import com.org.ddd.utils.Identifiable;
import com.org.ddd.repository.exceptions.RepositoryException;

public interface AbstractRepository<ID, E extends Identifiable<ID>> {
    void add(E entity) throws RepositoryException;

    void update(E entity) throws RepositoryException;

    E delete(ID id) throws RepositoryException;

    E findById(ID id) throws RepositoryException;

    Iterable<E> findAll();
}
