package com.blossomproject.module.search.common;

import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.core.common.service.SearchAndCrudService;
import com.blossomproject.module.search.common.facet.FacetConfiguration;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class DefaultSearchEngineImpl<DTO extends AbstractDTO> implements
  SearchEngine<AbstractQueryBuilder, AbstractSearchRequestBuilder, AbstractSearchResponse, DTO> {

  private final SearchEngineConfiguration<DTO> configuration;

  private final SearchAndCrudService<DTO> searchAndCrudService;

  public DefaultSearchEngineImpl(SearchEngineConfiguration<DTO> configuration, SearchAndCrudService<DTO> searchAndCrudService) {
    this.configuration = configuration;
    this.searchAndCrudService = searchAndCrudService;
  }

  @Override
  public String getName() {
    return this.configuration.getName();
  }

  @Override
  public boolean includeInOmnisearch() {
    return this.configuration.includeInOmnisearch();
  }


  @Override
  public AbstractSearchRequestBuilder prepareSearch(String q, Pageable pageable) {
    throw new UnsupportedOperationException();
  }


  @Override
  public AbstractSearchRequestBuilder prepareSearch(String q, Pageable pageable, Iterable<AbstractQueryBuilder> filters) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AbstractSearchRequestBuilder prepareSearch(String q, Pageable pageable, Iterable<AbstractQueryBuilder> filters,
    Iterable<FacetConfiguration> facetConfigurations) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SearchResult<DTO> parseResults(AbstractSearchResponse abstractSearchResponse, Pageable pageable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SearchResult<DTO> parseResults(AbstractSearchResponse abstractSearchResponse, Pageable pageable,
    Iterable<FacetConfiguration> facetConfigurations) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SearchResult<SummaryDTO> parseSummaryResults(AbstractSearchResponse abstractSearchResponse, Pageable pageable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SearchResult<DTO> search(String q, Pageable pageable) {
    long start = System.currentTimeMillis();
    Page<DTO> results = searchAndCrudService.getAllWithSearch(pageable, q);
    long end = System.currentTimeMillis();
    return new SearchResult<>(end - start, results, new ArrayList<>());
  }

  @Override
  public SearchResult<DTO> search(String q, Pageable pageable, Iterable<AbstractQueryBuilder> filters) {
    return search(q, pageable);
  }

  @Override
  public SearchResult<DTO> search(String q, Pageable pageable, Iterable<AbstractQueryBuilder> filters,
    Iterable<FacetConfiguration> aggregations) {
    return search(q, pageable);
  }

  @Override
  public boolean supports(Class<? extends AbstractDTO> delimiter) {
    return delimiter.isAssignableFrom(this.configuration.getSupportedClass());
  }

}
