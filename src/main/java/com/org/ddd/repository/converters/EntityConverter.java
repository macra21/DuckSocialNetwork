package com.org.ddd.repository.converters;

public interface EntityConverter<E> {
    String toLine(E entity);

    E fromLine(String line);
}
