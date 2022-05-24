package com.blossomproject.module.search.common.trace;

public class Aggregations {

  private Aggregation methods;

  private Aggregation response_time_histogram;

  private Aggregation response_status_stats;

  private Aggregation response_content_type_stats;

  private Aggregation top_uris;

  private Aggregation flop_uris;

  private Aggregation request_histogram;

  public Aggregation getMethods() {
    return methods;
  }

  public void setMethods(Aggregation methods) {
    this.methods = methods;
  }

  public Aggregation getResponse_time_histogram() {
    return response_time_histogram;
  }

  public void setResponse_time_histogram(Aggregation response_time_histogram) {
    this.response_time_histogram = response_time_histogram;
  }


  public Aggregation getResponse_status_stats() {
    return response_status_stats;
  }

  public void setResponse_status_stats(Aggregation response_status_stats) {
    this.response_status_stats = response_status_stats;
  }

  public Aggregation getResponse_content_type_stats() {
    return response_content_type_stats;
  }

  public void setResponse_content_type_stats(Aggregation response_content_type_stats) {
    this.response_content_type_stats = response_content_type_stats;
  }

  public Aggregation getTop_uris() {
    return top_uris;
  }

  public void setTop_uris(Aggregation top_uris) {
    this.top_uris = top_uris;
  }

  public Aggregation getFlop_uris() {
    return flop_uris;
  }

  public void setFlop_uris(Aggregation flop_uris) {
    this.flop_uris = flop_uris;
  }

  public Aggregation getRequest_histogram() {
    return request_histogram;
  }

  public void setRequest_histogram(Aggregation request_histogram) {
    this.request_histogram = request_histogram;
  }

  @Override
  public String toString() {
    return "Aggregations{" +
      "methods:" + methods +
      ", response_time_histogram:" + response_time_histogram +
      ", response_status_stats:" + response_status_stats +
      ", response_content_type_stats:" + response_content_type_stats +
      ", top_uris:" + top_uris +
      ", flop_uris:" + flop_uris +
      ", request_histogram:" + request_histogram +
      '}';
  }
}
