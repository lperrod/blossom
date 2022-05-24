package com.blossomproject.autoconfigure.core;

import com.blossomproject.core.common.actuator.TraceRepository;
import com.blossomproject.core.common.actuator.TraceStatisticsMvcEndpoint;
import com.blossomproject.module.search.common.trace.DefaultInMemoryTraceRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by Maël Gargadennnec on 11/05/2017.
 */
@Configuration

@AutoConfigureBefore(HttpTraceAutoConfiguration.class)
@PropertySource("classpath:/actuator.properties")
public class ActuatorAutoConfiguration {


  @Bean
  public TraceStatisticsMvcEndpoint traceStatisticsMvcEndpoint(
    TraceRepository traceRepository) {
    return new TraceStatisticsMvcEndpoint(traceRepository);
  }

  @Bean
  @ConditionalOnMissingBean(TraceRepository.class)
  public TraceRepository traceRepository(TraceProperties traceProperties) {
    return new DefaultInMemoryTraceRepository(traceProperties.getExcludedUris());
  }

  @Configuration("BlossomActuatorAutoConfigurationTraceProperties")
  @ConfigurationProperties("blossom.actuator.traces")
  @PropertySource("classpath:/actuator.properties")
  public static class TraceProperties {

    private final Set<String> excludedUris = new HashSet<>();

    private final Set<String> excludedRequestHeaders = new HashSet<>();

    private final Set<String> excludedResponseHeaders = new HashSet<>();

    public Set<String> getExcludedUris() {
      return excludedUris;
    }

    public Set<String> getExcludedRequestHeaders() {
      return excludedRequestHeaders;
    }

    public Set<String> getExcludedResponseHeaders() {
      return excludedResponseHeaders;
    }
  }
}
