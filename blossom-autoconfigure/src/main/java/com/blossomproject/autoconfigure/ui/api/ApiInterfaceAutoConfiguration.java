package com.blossomproject.autoconfigure.ui.api;

import static com.blossomproject.autoconfigure.ui.WebContextAutoConfiguration.BLOSSOM_API_BASE_PATH;

import com.blossomproject.autoconfigure.ui.WebSecurityAutoConfiguration;
import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.module.search.common.OmnisearchService;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.ui.api.OmnisearchApiController;
import com.blossomproject.ui.api.StatusApiController;
import com.blossomproject.ui.api.administration.UsersApiController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(UsersApiController.class)
public class ApiInterfaceAutoConfiguration {

  @Bean
  public OmnisearchApiController omnisearchApiController(OmnisearchService omnisearchService,
    PluginRegistry<SearchEngine<?, ?, ?, ? extends AbstractDTO>, Class<? extends AbstractDTO>> registry) {
    return new OmnisearchApiController(omnisearchService, registry);
  }

  @Bean
  public StatusApiController statusApiController(HealthEndpoint healthEndpoint) {
    return new StatusApiController(healthEndpoint);
  }

  @Configuration
  @AutoConfigureAfter(WebSecurityAutoConfiguration.class)
  public static class ApiSecurityConfiguration {

    @Value("${CSP_ANCESTORS:}")
    String cspAncestors;

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
      http
        .securityMatcher(PathPatternRequestMatcher.pathPattern("/" + BLOSSOM_API_BASE_PATH + "/**"))
        .headers(headers -> {
          headers.frameOptions(frameOptions -> frameOptions.sameOrigin());
          String cspValue = "frame-ancestors 'self' https://fonts.gstatic.com https://fonts.googleapis.com";
          if (StringUtils.hasText(cspAncestors)) {
            cspValue += " " + cspAncestors;
          }
          final String csp = cspValue;
          headers.contentSecurityPolicy(policy -> policy.policyDirectives(csp));
        })
        .authorizeHttpRequests(authorize -> authorize.anyRequest().fullyAuthenticated())
        .httpBasic(basic -> {});

      return http.build();
    }
  }
}
