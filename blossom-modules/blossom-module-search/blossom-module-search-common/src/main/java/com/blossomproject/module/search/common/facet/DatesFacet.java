package com.blossomproject.module.search.common.facet;

import java.time.Instant;
import java.util.List;

public class DatesFacet extends Facet {

  private List<DatesFacetResult> results;

  public DatesFacet(String name, String path) {
    super(FacetType.DATES, name, path);
  }

  public List<DatesFacetResult> getResults() {
    return results;
  }

  public void setResults(List<DatesFacetResult> results) {
    this.results = results;
  }

  public static class DatesFacetResult {

    private String term;

    private Instant from;

    private Instant to;

    private Long count;

    public String getTerm() {
      return term;
    }

    public void setTerm(String term) {
      this.term = term;
    }

    public Long getCount() {
      return count;
    }

    public void setCount(Long count) {
      this.count = count;
    }

    public Instant getFrom() {
      return from;
    }

    public void setFrom(Instant from) {
      this.from = from;
    }

    public Instant getTo() {
      return to;
    }

    public void setTo(Instant to) {
      this.to = to;
    }
  }
}
