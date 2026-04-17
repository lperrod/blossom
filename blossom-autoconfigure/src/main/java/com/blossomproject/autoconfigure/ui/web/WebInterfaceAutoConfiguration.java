package com.blossomproject.autoconfigure.ui.web;

import com.blossomproject.autoconfigure.ui.WebContextAutoConfiguration;
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
import java.util.Locale;
import java.util.Set;
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
/**
 * Created by Mael Gargadennnec on 04/05/2017.
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(HomeController.class)
@AutoConfigureAfter(WebContextAutoConfiguration.class)
public class WebInterfaceAutoConfiguration {

  @Qualifier(PluginConstants.PLUGIN_ASSOCIATION_SERVICE)
  @Autowired
  private PluginRegistry<AssociationServicePlugin, Class<? extends AbstractDTO>> associationServicePlugins;

  @Autowired
  @Qualifier(value = PluginConstants.PLUGIN_THEME)
  private PluginRegistry<Theme, String> themePlugins;

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
  public ThemeControllerAdvice themeControllerAdvice(String blossomDefaultThemeName) {
    return new ThemeControllerAdvice(themePlugins, blossomDefaultThemeName);
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
      WebProperties resourceProperties) {
      this.applicationContext = applicationContext;
      this.resourceProperties = resourceProperties;
    }

    @Bean
    public BlossomErrorViewResolver blossomErrorViewResolver() {
      return new BlossomErrorViewResolver(this.applicationContext, this.resourceProperties);
    }
  }

}
