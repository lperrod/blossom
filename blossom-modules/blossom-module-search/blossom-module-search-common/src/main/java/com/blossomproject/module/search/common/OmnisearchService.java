package com.blossomproject.module.search.common;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public interface OmnisearchService {

  Map<String, SearchResult<SummaryDTO>> doMultiSearch(List<SearchEngine> searchEngines, String query, Pageable pageable);

}
