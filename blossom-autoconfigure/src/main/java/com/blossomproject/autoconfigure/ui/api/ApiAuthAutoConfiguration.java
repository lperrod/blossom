package com.blossomproject.autoconfigure.ui.api;

import com.blossomproject.ui.api.AuthApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.session.SessionRegistry;

@Configuration
@ConditionalOnClass(AuthApiController.class)
@AutoConfigureAfter(ApiInterfaceAutoConfiguration.class)
public class ApiAuthAutoConfiguration {

  @Bean
  public AuthApiController authApiController(AuthenticationManager authenticationManager,
    @Autowired(required = false) SessionRegistry sessionRegistry) {
    return new AuthApiController(authenticationManager, sessionRegistry);
  }
}
