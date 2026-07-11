package com.blossomproject.autoconfigure.ui.api;

import com.blossomproject.core.common.utils.action_token.ActionTokenService;
import com.blossomproject.core.user.UserService;
import com.blossomproject.ui.api.ActivationApiController;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(ActivationApiController.class)
@AutoConfigureAfter(ApiInterfaceAutoConfiguration.class)
public class ApiActivationAutoConfiguration {

  @Bean
  public ActivationApiController activationApiController(ActionTokenService tokenService,
      UserService userService) {
    return new ActivationApiController(tokenService, userService);
  }
}
