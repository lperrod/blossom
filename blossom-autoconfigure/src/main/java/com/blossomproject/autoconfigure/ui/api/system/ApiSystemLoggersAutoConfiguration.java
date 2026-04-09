package com.blossomproject.autoconfigure.ui.api.system;

import com.blossomproject.autoconfigure.ui.api.ApiInterfaceAutoConfiguration;
import com.blossomproject.ui.api.system.LoggersApiController;
import org.springframework.boot.actuate.autoconfigure.logging.LoggersEndpointAutoConfiguration;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({LoggersEndpointAutoConfiguration.class, ApiInterfaceAutoConfiguration.class})
@ConditionalOnClass(LoggersApiController.class)
@ConditionalOnBean(LoggersEndpoint.class)
public class ApiSystemLoggersAutoConfiguration {

  @Bean
  public LoggersApiController loggersApiController(LoggersEndpoint loggersEndpoint) {
    return new LoggersApiController(loggersEndpoint);
  }
}
