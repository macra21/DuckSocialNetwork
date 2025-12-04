package com.org.ddd.repository;

import com.org.ddd.utils.Identifiable;
import com.org.ddd.utils.paging.*;

public interface PagingRepository<ID, E extends Identifiable<ID>> extends AbstractRepository<ID, E> {
    Page<E> findAllOnPage(Pageable pageable);
}
