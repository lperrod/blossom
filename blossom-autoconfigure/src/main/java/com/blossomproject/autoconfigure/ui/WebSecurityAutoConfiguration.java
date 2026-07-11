package com.blossomproject.autoconfigure.ui;

import com.blossomproject.autoconfigure.ui.common.privileges.ResponsabilityPrivilegesConfiguration;
import com.blossomproject.autoconfigure.ui.common.privileges.RolePrivilegesConfiguration;
import com.blossomproject.autoconfigure.ui.web.BlossomWebBackOfficeProperties;
import com.blossomproject.core.association_user_role.AssociationUserRoleDao;
import com.blossomproject.core.association_user_role.AssociationUserRoleService;
import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.common.utils.privilege.Privilege;
import com.blossomproject.core.user.UserService;
import com.blossomproject.ui.BlossomAuthenticationSuccessHandlerImpl;
import com.blossomproject.ui.security.AuthenticationFailureListener;
import com.blossomproject.ui.security.AuthenticationSuccessListener;
import com.blossomproject.ui.security.CompositeUserDetailsServiceImpl;
import com.blossomproject.ui.security.CurrentUserDetailsServiceImpl;
import com.blossomproject.ui.security.LimitLoginAuthenticationProvider;
import com.blossomproject.ui.security.LoginAttemptServiceImpl;
import com.blossomproject.ui.security.LoginAttemptsService;
import com.blossomproject.ui.security.SystemUserDetailsServiceImpl;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({AuthenticationFailureListener.class})
@Order(0)
@PropertySource("classpath:/security.properties")
@EnableConfigurationProperties({DefaultAccountProperties.class, BlossomWebBackOfficeProperties.class})
public class WebSecurityAutoConfiguration {

  public static final String BLOSSOM_REMEMBER_ME_COOKIE_NAME = "blossom";

  private static final Logger logger = LoggerFactory.getLogger(WebSecurityAutoConfiguration.class);

  @Bean
  public LoginAttemptsService loginAttemptsService() {
    return new LoginAttemptServiceImpl(10);
  }

  @Bean
  public AuthenticationFailureListener authenticationFailureListener(
    LoginAttemptsService loginAttemptService) {
    return new AuthenticationFailureListener(loginAttemptService);
  }

  @Bean
  public AuthenticationSuccessListener authenticationSuccessListener(
    LoginAttemptsService loginAttemptService) {
    return new AuthenticationSuccessListener(loginAttemptService);
  }

  @Bean
  public UserDetailsService dbUserDetailsService(UserService userService,
    AssociationUserRoleService associationUserRoleService) {
    return new CurrentUserDetailsServiceImpl(userService, associationUserRoleService);
  }

  @Bean
  public UserDetailsService systemUserDetailsService(
    @Qualifier(PluginConstants.PLUGIN_PRIVILEGES) PluginRegistry<Privilege, String> privilegeRegistry,
    DefaultAccountProperties properties,
    PasswordEncoder passwordEncoder,
    AssociationUserRoleDao associationUserRoleDao,
    RolePrivilegesConfiguration rolePrivilegesConfiguration,
    ResponsabilityPrivilegesConfiguration responsabilityPrivilegesConfiguration) {

    if ((properties.isEnabled() != null && properties.isEnabled()) ||
      (properties.isEnabled() == null && !associationUserRoleDao.getUserExistsByPrivilege(
        Arrays.asList(rolePrivilegesConfiguration.rolesReadPrivilegePlugin(),
          rolePrivilegesConfiguration.rolesWritePrivilegePlugin(),
          responsabilityPrivilegesConfiguration.responsabilitiesReadPrivilegePlugin(),
          responsabilityPrivilegesConfiguration.responsabilitiesChangePrivilegePlugin()
        )))) {

      logger.warn("Enabling blossom '{}' account with password '***'", properties.getIdentifier());
      return new SystemUserDetailsServiceImpl(privilegeRegistry, properties.getIdentifier(),
        passwordEncoder.encode(properties.getPassword()));
    }

    return identifier -> {
      throw new UsernameNotFoundException(String.format("User with identifier=%s was not found", identifier));
    };
  }

  @Bean
  @Primary
  public UserDetailsService compositeUserDetailsService(
    List<UserDetailsService> userDetailsServices) {
    return new CompositeUserDetailsServiceImpl(
      userDetailsServices.toArray(new UserDetailsService[userDetailsServices.size()]));
  }

  @Bean
  public BlossomAuthenticationSuccessHandlerImpl blossomAuthenticationSuccessHandler(
    UserService userService,
    BlossomWebBackOfficeProperties properties) {
    return new BlossomAuthenticationSuccessHandlerImpl(userService, properties.getMaxInactiveIntervalSeconds());
  }

  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public LimitLoginAuthenticationProvider limitLoginAuthenticationProvider(
    @Qualifier(value = "compositeUserDetailsService") UserDetailsService compositeUserDetailsService,
    PasswordEncoder passwordEncoder,
    LoginAttemptsService loginAttempsService) {

    LimitLoginAuthenticationProvider provider = new LimitLoginAuthenticationProvider(
      compositeUserDetailsService, loginAttempsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }


}
