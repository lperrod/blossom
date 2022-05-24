package com.blossomproject.core.common.dao;

import com.blossomproject.core.common.entity.AbstractEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchAndCrudDao<ENTITY extends AbstractEntity> extends CrudDao<ENTITY> {


  /**
   * Retrieves a paginated subset of the entities from the underlying datasource with a query string
   *
   * @param pageable a spring data {@link Pageable}. Throws an {@code IllegalArgumentException} if null.
   * @return the asked {@link Page} of entities
   */
  Page<ENTITY> getAllWithSearch(Pageable pageable, String query);

}
