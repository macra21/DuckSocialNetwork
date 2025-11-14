package com.org.ddd.repository;

import com.org.ddd.domain.entities.Entity;
import com.org.ddd.repository.exceptions.RepositoryException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryRepository<E extends Entity<Long>> implements AbstractRepository<Long, E> {
    protected final Map<Long, E> entities;
    protected final AtomicLong idCounter;

    public InMemoryRepository() {
        this.entities = new HashMap<>();
        this.idCounter = new AtomicLong(0L);
    }

    @Override
    public void add(E entity) throws RepositoryException{
        if (entity == null){
            throw new RepositoryException("Add error: Entity cannot be null!\n");
        }

        if (entity.getId() == null) {
            Long newId = idCounter.incrementAndGet();
            entity.setId(newId);
        }

        if (entities.containsKey(entity.getId())){
            throw new RepositoryException("Add error: Entity with id " + entity.getId() + " already exists!\n");
        }

        entities.put(entity.getId(), entity);
    }

    @Override
    public void update(E entity) throws RepositoryException {
        if (entity == null || entity.getId() == null){
            throw new RepositoryException("Update error: Entity cannot be null or have null id!\n");
        }

        if (!entities.containsKey(entity.getId())){
            throw new RepositoryException("Update error: Entity with id " + entity.getId() + " does not exist!\n");
        }

        entities.put(entity.getId(), entity);
    }

    @Override
    public E delete(Long id) throws RepositoryException {
        if (id == null){
            throw new RepositoryException("Remove error: ID cannot be null!\n");
        }

        E removed = entities.remove(id);
        if (removed == null){
            throw new RepositoryException("Remove error: Entity with id " + id + " does not exist!\n");
        }
        return removed;
    }

    @Override
    public E findById(Long id) throws RepositoryException {
        if (id == null){
            throw new RepositoryException("FindById error: ID cannot be null!\n");
        }
        E found = entities.get(id);
        if (found == null) {
            throw new RepositoryException("FindById error: Entity with id " + id + " does not exist!\n");
        }
        return found;
    }

    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }
}
