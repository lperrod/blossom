package com.blossomproject.module.search.common.trace;

public class Bucket {

  private Aggregation methods;

  private Object key;

  private int doc_count;


  public Object getKey() {
    return key;
  }

  public void setKey(Object key) {
    this.key = key;
  }

  public int getDoc_count() {
    return doc_count;
  }

  public void setDoc_count(int doc_count) {
    this.doc_count = doc_count;
  }

  public Aggregation getMethods() {
    return methods;
  }

  public void setMethods(Aggregation methods) {
    this.methods = methods;
  }

  @Override
  public String toString() {
    return "Bucket{" +
      "methods:" + methods +
      ", key:'" + key + '\'' +
      ", doc_count:" + doc_count +
      '}';
  }
}
