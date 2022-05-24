package com.blossomproject.core.common.service;

import com.blossomproject.core.common.dto.AbstractDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchAndCrudService<DTO extends AbstractDTO> extends CrudService<DTO> {

  Page<DTO> getAllWithSearch(Pageable pageable, String query);

}
