package com.blossomproject.autoconfigure.ui.api.system;

import com.blossomproject.autoconfigure.ui.api.ApiInterfaceAutoConfiguration;
import com.blossomproject.ui.api.system.DashboardApiController;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsEndpointAutoConfiguration;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({HealthEndpointAutoConfiguration.class, MetricsEndpointAutoConfiguration.class, ApiInterfaceAutoConfiguration.class})
@ConditionalOnClass(DashboardApiController.class)
@ConditionalOnBean({HealthEndpoint.class, MetricsEndpoint.class})
public class ApiSystemDashboardAutoConfiguration {

  @Bean
  public DashboardApiController dashboardApiController(HealthEndpoint healthEndpoint,
    MetricsEndpoint metricsEndpoint) {
    return new DashboardApiController(healthEndpoint, metricsEndpoint);
  }
}
