package com.blossomproject.autoconfigure.core;

import com.blossomproject.autoconfigure.core.elasticsearch.ElasticsearchProperties;
import com.google.common.base.Splitter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lease.Releasable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/elasticsearch.properties")
@EnableConfigurationProperties(ElasticsearchProperties.class)
@ConditionalOnProperty(value = "blossom.elasticsearch.enabled", havingValue = "yes")
public class ElasticsearchAutoConfiguration implements DisposableBean {

  private static final Logger logger = LoggerFactory
    .getLogger(ElasticsearchAutoConfiguration.class);


  private final ElasticsearchProperties properties;
  private Releasable releasable;

  public ElasticsearchAutoConfiguration(ElasticsearchProperties properties) {
    this.properties = properties;
  }


  @Bean
  public Client elasticsearchClient() {
    try {
      return createClient();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private Client createClient() throws Exception {
    return createTransportClient();
  }


  private Client createTransportClient() throws Exception {

    Settings.Builder settingsBuilder = Settings.builder();
    createProperties().forEach((key, value) -> settingsBuilder.put((String) key, (String) value));

    TransportClient client = new PreBuiltTransportClient(Settings.builder().put(settingsBuilder.build()).build());

    Splitter.on(",").splitToList(this.properties.getClusterNodes()).forEach(
      a -> {
        String[] hostAndPort = a.split(":");
        try {
          client.addTransportAddress(new TransportAddress(
            InetAddress.getByName(hostAndPort[0]),
            Integer.parseInt(hostAndPort[1])));
        } catch (UnknownHostException e) {
          throw new RuntimeException("Cannot connect to inet address " + hostAndPort[0], e);
        }
      }
    );
    this.releasable = client;
    return client;
  }

  private Properties createProperties() {
    Properties properties = new Properties();
    properties.put("cluster.name", this.properties.getClusterName());
    properties.putAll(this.properties.getProperties());
    return properties;
  }

  @Override
  public void destroy() throws Exception {
    if (this.releasable != null) {
      try {
        if (logger.isInfoEnabled()) {
          logger.info("Closing Elasticsearch client");
        }
        this.releasable.close();
      } catch (final Exception ex) {
        if (logger.isErrorEnabled()) {
          logger.error("Error closing Elasticsearch client: ", ex);
        }
      }
    }
  }


  @Bean
  @ConditionalOnMissingBean(BulkProcessor.class)
  public BulkProcessor bulkProcessor(Client client) {
    return BulkProcessor.builder(client, new BulkProcessor.Listener() {

        @Override
        public void beforeBulk(long executionId, BulkRequest request) {
          logger.info("Before bulk {} with {} actions to execute", executionId,
            request.numberOfActions());
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
          logger.error("Error on bulk {} with {} actions to execute", executionId,
            request.numberOfActions(), failure);
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
          logger.info("Successful bulk {} with {} actions executed in {} ms.", executionId,
            request.numberOfActions(), response.getTook().getMillis());
        }
      })
      .setBulkActions(500)
      .setBulkSize(new ByteSizeValue(10, ByteSizeUnit.MB))
      .setFlushInterval(new TimeValue(30, TimeUnit.SECONDS))
      .build();
  }

}
