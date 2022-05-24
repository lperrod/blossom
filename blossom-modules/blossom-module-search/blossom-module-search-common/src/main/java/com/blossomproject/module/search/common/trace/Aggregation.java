package com.blossomproject.module.search.common.trace;

import java.util.List;

public class Aggregation {

  private List<Bucket> buckets;

  public List<Bucket> getBuckets() {
    return buckets;
  }

  public void setBuckets(List<Bucket> buckets) {
    this.buckets = buckets;
  }

  @Override
  public String toString() {
    return "Aggregation{" +
      "buckets:" + buckets +
      '}';
  }
}
