package com.blossomproject.core.common.service;

import com.blossomproject.core.common.dao.SearchAndCrudDao;
import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.core.common.entity.AbstractEntity;
import com.blossomproject.core.common.mapper.DTOMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.PluginRegistry;

public abstract class GenericSearchAndCrudServiceImpl<DTO extends AbstractDTO, ENTITY extends AbstractEntity> extends
  GenericCrudServiceImpl<DTO, ENTITY> implements SearchAndCrudService<DTO> {

  protected final SearchAndCrudDao<ENTITY> searchAndCrudDao;

  protected GenericSearchAndCrudServiceImpl(SearchAndCrudDao<ENTITY> dao,
    DTOMapper<ENTITY, DTO> mapper,
    ApplicationEventPublisher publisher,
    PluginRegistry<AssociationServicePlugin, Class<? extends AbstractDTO>> associationRegistry) {
    super(dao, mapper, publisher, associationRegistry);
    this.searchAndCrudDao = dao;
  }

  @Override
  public Page<DTO> getAllWithSearch(Pageable pageable, String query) {
    return mapper.mapEntitiesPage(searchAndCrudDao.getAllWithSearch(pageable, query));
  }
}
