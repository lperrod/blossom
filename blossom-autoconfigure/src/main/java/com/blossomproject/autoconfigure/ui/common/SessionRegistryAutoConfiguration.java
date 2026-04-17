package com.blossomproject.autoconfigure.ui.common;

import com.blossomproject.core.association_user_role.AssociationUserRoleService;
import com.blossomproject.ui.session.BlossomSessionRegistryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;

@Configuration
@ConditionalOnWebApplication
public class SessionRegistryAutoConfiguration {

  @Bean
  public SessionRegistry blossomSessionRegistry(
    AssociationUserRoleService associationUserRoleService) {
    return new BlossomSessionRegistryImpl(associationUserRoleService);
  }
}
