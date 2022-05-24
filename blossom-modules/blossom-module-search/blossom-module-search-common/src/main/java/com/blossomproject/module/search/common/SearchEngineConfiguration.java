package com.blossomproject.module.search.common;

import com.blossomproject.core.common.dto.AbstractDTO;

public interface SearchEngineConfiguration<DTO extends AbstractDTO> {

  String getName();

  Class<DTO> getSupportedClass();

  String[] getFields();

  String getAlias();

  default boolean includeInOmnisearch() {
    return true;
  }

}
