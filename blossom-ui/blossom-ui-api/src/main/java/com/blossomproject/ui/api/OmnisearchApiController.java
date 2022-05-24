package com.blossomproject.ui.api;

import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.module.search.common.OmnisearchService;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.module.search.common.SearchResult;
import com.blossomproject.module.search.common.SummaryDTO;
import com.blossomproject.ui.stereotype.BlossomApiController;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@BlossomApiController
@RequestMapping("/search")
public class OmnisearchApiController {

  private final OmnisearchService omnisearchService;


  private final PluginRegistry<SearchEngine<?, ?, ?, ? extends AbstractDTO>, Class<? extends AbstractDTO>> registry;

  public OmnisearchApiController(OmnisearchService omnisearchService,
    PluginRegistry<SearchEngine<?, ?, ?, ? extends AbstractDTO>, Class<? extends AbstractDTO>> registry) {
    this.omnisearchService = omnisearchService;
    this.registry = registry;
  }

  @GetMapping
  public Map<String, Object> omniSearch(
    @RequestParam(value = "q", defaultValue = "", required = false) String query,
    @PageableDefault(size = 20) Pageable pageable) {
    List<SearchEngine> plugins = filteredPlugins();
    if (plugins.isEmpty()) {
      Map<String, Object> model = Maps.newHashMap();
      model.put("q", query);
      model.put("total", 0);
      model.put("duration", 0L);
      model.put("results", Maps.newHashMap());
      return model;
    }

    int index = 0;
    Map<String, SearchResult<SummaryDTO>> results = omnisearchService.doMultiSearch(plugins, query, pageable);

    Map<String, Object> model = Maps.newHashMap();
    model.put("q", query);
    model.put("total",
      results.values().stream().mapToLong(r -> r.getPage().getTotalElements()).sum());
    model.put("duration",
      results.values().stream().mapToLong(SearchResult::getDuration).max().getAsLong());
    model.put("results", results.entrySet().stream()
      .filter(e -> e.getValue().getPage().getTotalElements() != 0)
      .sorted(Comparator.comparing(
          (Entry<String, SearchResult<SummaryDTO>> e) -> e.getValue().getPage().getTotalElements())
        .reversed())
      .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> {
        throw new IllegalStateException(String.format("Duplicate key %s", u));
      }, LinkedHashMap::new)));

    return model;
  }

  @VisibleForTesting
  List<SearchEngine> filteredPlugins() {
    return registry.getPlugins().stream().filter(SearchEngine::includeInOmnisearch)
      .collect(Collectors.toList());
  }
}
