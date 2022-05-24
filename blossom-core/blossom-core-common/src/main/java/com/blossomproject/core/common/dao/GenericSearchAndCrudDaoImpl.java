package com.blossomproject.core.common.dao;

import com.blossomproject.core.common.entity.AbstractEntity;
import com.blossomproject.core.common.repository.CrudRepository;
import com.google.common.base.Preconditions;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public abstract class GenericSearchAndCrudDaoImpl<ENTITY extends AbstractEntity> extends GenericCrudDaoImpl<ENTITY> implements
  SearchAndCrudDao<ENTITY> {


  protected GenericSearchAndCrudDaoImpl(CrudRepository<ENTITY> repository) {
    super(repository);
  }

  @Override
  public Page<ENTITY> getAllWithSearch(Pageable pageable, String query) {
    Preconditions.checkArgument(pageable != null);
    return repository.findAll(computeSearchPredicate(query), pageable);
  }

  protected abstract Predicate computeSearchPredicate(String query);
}
