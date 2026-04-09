package com.blossomproject.autoconfigure.ui.api;

import com.blossomproject.core.user.UserService;
import com.blossomproject.ui.api.ProfileApiController;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(ProfileApiController.class)
@ConditionalOnBean(UserService.class)
@AutoConfigureAfter(ApiInterfaceAutoConfiguration.class)
public class ApiProfileAutoConfiguration {

  @Bean
  public ProfileApiController profileApiController(UserService userService) {
    return new ProfileApiController(userService);
  }
}
