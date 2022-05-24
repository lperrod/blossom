package com.blossomproject.module.search.common;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public class DefaultOmnisearchServiceImpl implements OmnisearchService {

  @Override
  public Map<String, SearchResult<SummaryDTO>> doMultiSearch(List<SearchEngine> searchEngines, String query, Pageable pageable) {
    return null;
  }
}
