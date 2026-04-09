package com.blossomproject.autoconfigure.ui.api;

import com.blossomproject.core.common.utils.privilege.Privilege;
import com.blossomproject.ui.api.administration.PrivilegesApiController;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.PluginRegistry;

@Configuration
@ConditionalOnClass(PrivilegesApiController.class)
@AutoConfigureAfter(ApiInterfaceAutoConfiguration.class)
public class ApiPrivilegesAutoConfiguration {

  @Bean
  public PrivilegesApiController privilegesApiController(
    PluginRegistry<Privilege, String> privilegeRegistry) {
    return new PrivilegesApiController(privilegeRegistry);
  }
}
