package com.blossomproject.autoconfigure.ui.api;

import com.blossomproject.ui.api.AuthApiController;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

@Configuration
@ConditionalOnClass(AuthApiController.class)
@AutoConfigureAfter(ApiInterfaceAutoConfiguration.class)
public class ApiAuthAutoConfiguration {

  @Bean
  public AuthApiController authApiController(AuthenticationManager authenticationManager) {
    return new AuthApiController(authenticationManager);
  }
}
