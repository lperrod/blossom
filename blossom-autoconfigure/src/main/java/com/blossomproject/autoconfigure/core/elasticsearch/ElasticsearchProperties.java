package com.blossomproject.autoconfigure.core.elasticsearch;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blossom.elasticsearch")
public class ElasticsearchProperties {

  private String clusterName = "elasticsearch";
  private String clusterNodes;
  private Map<String, String> properties = new HashMap<String, String>();
  private String enabled;

  public String getClusterName() {
    return this.clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getClusterNodes() {
    return this.clusterNodes;
  }

  public void setClusterNodes(String clusterNodes) {
    this.clusterNodes = clusterNodes;
  }

  public Map<String, String> getProperties() {
    return this.properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public String getEnabled() {
    return enabled;
  }

  public void setEnabled(String enabled) {
    this.enabled = enabled;
  }
}
