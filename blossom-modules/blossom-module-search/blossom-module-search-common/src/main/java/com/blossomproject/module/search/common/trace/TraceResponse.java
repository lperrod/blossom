package com.blossomproject.module.search.common.trace;

public class TraceResponse {

  private Aggregations aggregations;


  public Aggregations getAggregations() {
    return aggregations;
  }

  public void setAggregations(Aggregations aggregations) {
    this.aggregations = aggregations;
  }

  @Override
  public String toString() {
    return "TraceResponse{" +
      "aggregations:" + aggregations +
      '}';
  }
}
