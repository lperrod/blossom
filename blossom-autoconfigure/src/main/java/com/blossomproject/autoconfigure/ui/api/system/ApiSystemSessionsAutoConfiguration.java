package com.blossomproject.autoconfigure.ui.api.system;

import com.blossomproject.autoconfigure.core.CommonAutoConfiguration;
import com.blossomproject.autoconfigure.ui.api.ApiInterfaceAutoConfiguration;
import com.blossomproject.ui.api.system.SessionsApiController;
import com.blossomproject.ui.security.LoginAttemptsService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;

@Configuration
@AutoConfigureAfter({CommonAutoConfiguration.class, ApiInterfaceAutoConfiguration.class})
@ConditionalOnClass(SessionsApiController.class)
@ConditionalOnBean(SessionRegistry.class)
public class ApiSystemSessionsAutoConfiguration {

  @Bean
  public SessionsApiController sessionsApiController(SessionRegistry sessionRegistry,
    LoginAttemptsService loginAttemptsService) {
    return new SessionsApiController(sessionRegistry, loginAttemptsService);
  }
}
