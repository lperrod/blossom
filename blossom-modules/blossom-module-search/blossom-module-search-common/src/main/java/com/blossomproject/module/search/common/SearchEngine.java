package com.blossomproject.module.search.common;


import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.module.search.common.facet.FacetConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.Plugin;

@Qualifier(value = PluginConstants.PLUGIN_SEARCH_ENGINE)
public interface SearchEngine<QUERY_BUILDER, REQUEST_BUILDER, SEARCH_RESPONSE, DTO extends AbstractDTO> extends
  Plugin<Class<? extends AbstractDTO>> {

  String getName();

  boolean includeInOmnisearch();

  REQUEST_BUILDER prepareSearch(String q, Pageable pageable);


  REQUEST_BUILDER prepareSearch(String q, Pageable pageable, Iterable<QUERY_BUILDER> filters);

  REQUEST_BUILDER prepareSearch(String q, Pageable pageable, Iterable<QUERY_BUILDER> filters,
    Iterable<FacetConfiguration> facetConfigurations);

  SearchResult<DTO> parseResults(SEARCH_RESPONSE response, Pageable pageable);

  SearchResult<DTO> parseResults(SEARCH_RESPONSE response, Pageable pageable,
    Iterable<FacetConfiguration> facetConfigurations);

  SearchResult<SummaryDTO> parseSummaryResults(SEARCH_RESPONSE response, Pageable pageable);

  SearchResult<DTO> search(String q, Pageable pageable);

  SearchResult<DTO> search(String q, Pageable pageable, Iterable<QUERY_BUILDER> filters);

  SearchResult<DTO> search(String q, Pageable pageable, Iterable<QUERY_BUILDER> filters,
    Iterable<FacetConfiguration> aggregations);


}
