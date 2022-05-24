package com.blossomproject.module.search.elasticsearch;

import com.blossomproject.module.search.common.facet.DatesFacet;
import com.blossomproject.module.search.common.facet.DatesFacet.DatesFacetResult;
import com.blossomproject.module.search.common.facet.DatesFacetConfiguration;
import com.blossomproject.module.search.common.facet.Facet;
import com.blossomproject.module.search.common.facet.FacetConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;

public class AggregationConverterDatesImpl extends AggregationConverterAbstractImpl {

  private final String field;

  public AggregationConverterDatesImpl(String name, String supportedSearchEngine,
    String field) {
    super(name, supportedSearchEngine);
    this.field = field;
  }

  @Override
  public List<AggregationBuilder> encode(FacetConfiguration configuration) {
    return new ArrayList<>();
  }

  @Override
  public Facet decode(Supplier<Aggregations> parent, FacetConfiguration configuration) {
    DatesFacetConfiguration config = (DatesFacetConfiguration) configuration;

    DatesFacet facet = new DatesFacet(this.name(), field);
    facet.setResults(config.getPeriods().stream().map(b -> {
      DatesFacetResult result = new DatesFacetResult();
      result.setTerm(b.getName());
      result.setFrom(b.getFrom());
      result.setTo(b.getTo());
      result.setCount(1L);
      return result;
    }).filter(r -> r != null).collect(Collectors.toList()));

    return facet;
  }
}
