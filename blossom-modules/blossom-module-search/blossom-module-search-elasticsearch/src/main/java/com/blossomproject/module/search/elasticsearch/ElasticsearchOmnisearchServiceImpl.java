package com.blossomproject.module.search.elasticsearch;

import com.blossomproject.module.search.common.OmnisearchService;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.module.search.common.SearchResult;
import com.blossomproject.module.search.common.SummaryDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.core.TimeValue;
import org.springframework.data.domain.Pageable;

public class ElasticsearchOmnisearchServiceImpl implements OmnisearchService {

  private final Client client;


  public ElasticsearchOmnisearchServiceImpl(Client client) {
    this.client = client;
  }

  @Override
  public Map<String, SearchResult<SummaryDTO>> doMultiSearch(
    List<SearchEngine> searchEngines, String query,
    Pageable pageable) {
    MultiSearchRequestBuilder request = client.prepareMultiSearch();
    searchEngines.forEach(engine -> request.add((SearchRequestBuilder) engine.prepareSearch(query, pageable)));
    MultiSearchResponse response = request.get(TimeValue.timeValueSeconds(15));

    int index = 0;
    Map<String, SearchResult<SummaryDTO>> results = Maps.newHashMap();

    List<Item> items = Lists.newArrayList(response.getResponses());
    for (Item item : items) {
      SearchResponse unitResponse = item.getResponse();
      SearchEngine searchEngine = searchEngines.get(index);
      SearchResult<SummaryDTO> result = searchEngine.parseSummaryResults(unitResponse, pageable);
      results.put(searchEngine.getName(), result);
      index++;
    }
    return results;
  }
}
