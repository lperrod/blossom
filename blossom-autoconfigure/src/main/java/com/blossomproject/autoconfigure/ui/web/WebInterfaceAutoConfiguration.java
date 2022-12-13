package com.blossomproject.autoconfigure.ui.web;

import com.blossomproject.autoconfigure.ui.WebContextAutoConfiguration;
import com.blossomproject.core.association_user_role.AssociationUserRoleService;
import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.core.common.service.AssociationServicePlugin;
import com.blossomproject.core.common.utils.action_token.ActionTokenService;
import com.blossomproject.core.user.UserService;
import com.blossomproject.module.search.common.OmnisearchService;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.ui.current_user.CurrentUserControllerAdvice;
import com.blossomproject.ui.i18n.LocaleControllerAdvice;
import com.blossomproject.ui.menu.Menu;
import com.blossomproject.ui.menu.MenuControllerAdvice;
import com.blossomproject.ui.theme.Theme;
import com.blossomproject.ui.theme.ThemeControllerAdvice;
import com.blossomproject.ui.web.ActivationController;
import com.blossomproject.ui.web.CSRFControllerAdvice;
import com.blossomproject.ui.web.HomeController;
import com.blossomproject.ui.web.LoginController;
import com.blossomproject.ui.web.OmnisearchController;
import com.blossomproject.ui.web.ProfileController;
import com.blossomproject.ui.web.error.BlossomErrorViewResolver;
import com.blossomproject.ui.web.error.ErrorControllerAdvice;
import com.blossomproject.ui.web.utils.session.BlossomSessionRegistryImpl;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ThemeResolver;

/**
 * Created by Maël Gargadennnec on 04/05/2017.
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(HomeController.class)
@AutoConfigureAfter(WebContextAutoConfiguration.class)
public class WebInterfaceAutoConfiguration {

  private final AssociationUserRoleService associationUserRoleService;

  @Qualifier(PluginConstants.PLUGIN_ASSOCIATION_SERVICE)
  @Autowired
  private PluginRegistry<AssociationServicePlugin, Class<? extends AbstractDTO>> associationServicePlugins;

  @Autowired
  @Qualifier(value = PluginConstants.PLUGIN_THEME)
  private PluginRegistry<Theme, String> themePlugins;

  public WebInterfaceAutoConfiguration(
    AssociationUserRoleService associationUserRoleService) {
    this.associationUserRoleService = associationUserRoleService;
  }

  @Bean
  public SessionRegistry blossomSessionRegistry() {
    return new BlossomSessionRegistryImpl(associationUserRoleService);
  }

  @Bean
  public LoginController loginController() {
    return new LoginController();
  }

  @Bean
  public HomeController homeController() {
    return new HomeController();
  }

  @Bean
  public OmnisearchController searchController(OmnisearchService omnisearchService,
    @Qualifier(PluginConstants.PLUGIN_SEARCH_ENGINE) PluginRegistry<SearchEngine<?, ?, ?, ? extends AbstractDTO>, Class<? extends AbstractDTO>> registry) {
    return new OmnisearchController(omnisearchService, registry);
  }

  @Bean
  public ProfileController profileController(UserService userService) {
    return new ProfileController(userService);
  }

  @Bean
  public ActivationController activationController(ActionTokenService tokenService,
    UserService userService) {
    return new ActivationController(tokenService, userService);
  }

  @Bean
  public CurrentUserControllerAdvice currentUserControllerAdvice() {
    return new CurrentUserControllerAdvice();
  }

  @Bean
  public ErrorControllerAdvice errorControllerAdvice() {
    return new ErrorControllerAdvice();
  }

  @Bean
  public MenuControllerAdvice menuControllerAdvice(Menu menu) {
    return new MenuControllerAdvice(menu);
  }

  @Bean
  public LocaleControllerAdvice languageControllerAdvice(Set<Locale> availableLocales) {
    return new LocaleControllerAdvice(availableLocales);
  }

  @Bean
  public ThemeControllerAdvice themeControllerAdvice(ThemeResolver themeResolver) {
    return new ThemeControllerAdvice(themePlugins, themeResolver);
  }

  @Bean
  public CSRFControllerAdvice csrfControllerAdvice() {
    return new CSRFControllerAdvice();
  }


  @Configuration
  static class BlossomErrorViewResolverConfiguration {

    private final ApplicationContext applicationContext;

    private final WebProperties resourceProperties;

    BlossomErrorViewResolverConfiguration(ApplicationContext applicationContext,
      WebProperties resourceProperties,
      AssociationUserRoleService associationUserRoleService) {
      this.applicationContext = applicationContext;
      this.resourceProperties = resourceProperties;
    }

    @Bean
    public BlossomErrorViewResolver blossomErrorViewResolver() {
      return new BlossomErrorViewResolver(this.applicationContext, this.resourceProperties);
    }
  }

  public static class BlossomInvalidSessionStrategy implements SessionInformationExpiredStrategy {

    private final Logger logger = LoggerFactory.getLogger(BlossomInvalidSessionStrategy.class);

    private final String destinationUrl;

    private final RedirectStrategy redirectStrategy;

    public BlossomInvalidSessionStrategy(String invalidSessionUrl) {
      this(invalidSessionUrl, new DefaultRedirectStrategy());
    }

    public BlossomInvalidSessionStrategy(String invalidSessionUrl,
      RedirectStrategy redirectStrategy) {
      Assert.isTrue(UrlUtils.isValidRedirectUrl(invalidSessionUrl),
        "url must start with '/' or with 'http(s)'");
      this.destinationUrl = invalidSessionUrl;
      this.redirectStrategy = redirectStrategy;
    }

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event)
      throws IOException, ServletException {
      if (logger.isDebugEnabled()) {
        logger.debug("Redirecting to '" + destinationUrl + "'");
      }
      String ajaxHeader = event.getRequest().getHeader("X-Requested-With");

      if (ajaxHeader != null && "XMLHttpRequest".equals(ajaxHeader)) {
        logger.info("Ajax call detected, send {} error code", 401);
        event.getResponse().sendError(401);
        return;
      }

      redirectStrategy.sendRedirect(event.getRequest(), event.getResponse(), destinationUrl);
    }
  }
}
