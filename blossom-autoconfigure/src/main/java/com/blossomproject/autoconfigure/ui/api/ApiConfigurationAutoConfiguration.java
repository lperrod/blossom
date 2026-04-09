package com.blossomproject.autoconfigure.ui.api;

import com.blossomproject.ui.api.ConfigurationApiController;
import com.blossomproject.ui.menu.Menu;
import java.util.Locale;
import java.util.Set;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(ConfigurationApiController.class)
@ConditionalOnBean(Menu.class)
@AutoConfigureAfter(ApiInterfaceAutoConfiguration.class)
public class ApiConfigurationAutoConfiguration {

  @Bean
  public ConfigurationApiController configurationApiController(Menu menu,
    Set<Locale> availableLocales) {
    return new ConfigurationApiController(menu, availableLocales);
  }
}
