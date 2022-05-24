package com.blossomproject.ui.web;

import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.module.search.common.OmnisearchService;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.module.search.common.SearchResult;
import com.blossomproject.module.search.common.SummaryDTO;
import com.blossomproject.ui.stereotype.BlossomController;
import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@BlossomController
@RequestMapping("/search")
public class OmnisearchController {


  private final OmnisearchService omnisearchService;


  private final PluginRegistry<SearchEngine<?, ?, ?, ? extends AbstractDTO>, Class<? extends AbstractDTO>> registry;

  public OmnisearchController(OmnisearchService omnisearchService,
    PluginRegistry<SearchEngine<?, ?, ?, ? extends AbstractDTO>, Class<? extends AbstractDTO>> registry) {
    this.omnisearchService = omnisearchService;
    this.registry = registry;
  }

  @GetMapping
  public ModelAndView omniSearch(
    @RequestParam(value = "q", defaultValue = "", required = false) String query,
    @PageableDefault(size = 20) Pageable pageable,
    Model model) {
    List<SearchEngine> plugins = filteredPlugins();
    if (plugins.isEmpty()) {
      return new ModelAndView("omnisearch/omnisearch", model.asMap());
    }

    int index = 0;
    Map<String, SearchResult<SummaryDTO>> results = omnisearchService.doMultiSearch(plugins, query, pageable);

    model.addAttribute("q", query);
    model.addAttribute("total",
      results.values().stream().mapToLong(r -> r.getPage().getTotalElements()).sum());
    model.addAttribute("duration",
      results.values().stream().mapToLong(SearchResult::getDuration).max().getAsLong());
    model.addAttribute("results", results.entrySet().stream()
      .filter(e -> e.getValue().getPage().getTotalElements() != 0)
      .sorted(Comparator.comparing(
          (Entry<String, SearchResult<SummaryDTO>> e) -> e.getValue().getPage().getTotalElements())
        .reversed())
      .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> {
        throw new IllegalStateException(String.format("Duplicate key %s", u));
      }, LinkedHashMap::new)));

    return new ModelAndView("blossom/omnisearch/omnisearch", model.asMap());
  }

  @VisibleForTesting
  List<SearchEngine> filteredPlugins() {
    return registry.getPlugins().stream().filter(SearchEngine::includeInOmnisearch).collect(Collectors.toList());
  }
}
