package com.blossomproject.autoconfigure.ui.web;

import static com.blossomproject.autoconfigure.ui.WebContextAutoConfiguration.BLOSSOM_BASE_PATH;
import static com.blossomproject.autoconfigure.ui.WebSecurityAutoConfiguration.BLOSSOM_REMEMBER_ME_COOKIE_NAME;

import com.blossomproject.core.common.utils.privilege.Privilege;
import com.blossomproject.ui.BlossomAuthenticationSuccessHandlerImpl;
import com.blossomproject.ui.security.LimitLoginAuthenticationProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@ConditionalOnBean(WebInterfaceAutoConfiguration.class)
@AutoConfigureAfter(WebInterfaceAutoConfiguration.class)
@Configuration
public class FormLoginWebSecurityConfigurerAdapter {

  private static final String ANGULAR_LOGIN_PATH = "/" + BLOSSOM_BASE_PATH + "/ng/login";

  private static final RequestMatcher PUBLIC_URLS = new OrRequestMatcher(
    PathPatternRequestMatcher.pathPattern("/public/**"),
    PathPatternRequestMatcher.pathPattern("/favicon.ico")
  );

  private static final RequestMatcher BLOSSOM_PUBLIC_URLS = new OrRequestMatcher(
    PathPatternRequestMatcher.pathPattern("/" + BLOSSOM_BASE_PATH + "/public/**"),
    PathPatternRequestMatcher.pathPattern("/" + BLOSSOM_BASE_PATH + "/login"),
    PathPatternRequestMatcher.pathPattern("/" + BLOSSOM_BASE_PATH + "/ng/**")
  );

  private final UserDetailsService userDetailsService;

  private final BlossomAuthenticationSuccessHandlerImpl blossomAuthenticationSuccessHandler;

  private final SessionRegistry sessionRegistry;

  private final Privilege switchUserPrivilege;

  private final LimitLoginAuthenticationProvider limitLoginAuthenticationProvider;

  private final BlossomWebBackOfficeProperties webBackOfficeProperties;


  public FormLoginWebSecurityConfigurerAdapter(
    UserDetailsService userDetailsService,
    BlossomAuthenticationSuccessHandlerImpl blossomAuthenticationSuccessHandler,
    SessionRegistry sessionRegistry,
    LimitLoginAuthenticationProvider limitLoginAuthenticationProvider,
    @Qualifier("switchUserPrivilege") Privilege switchUserPrivilege,
    BlossomWebBackOfficeProperties webBackOfficeProperties) {
    this.userDetailsService = userDetailsService;
    this.blossomAuthenticationSuccessHandler = blossomAuthenticationSuccessHandler;
    this.sessionRegistry = sessionRegistry;
    this.limitLoginAuthenticationProvider = limitLoginAuthenticationProvider;
    this.switchUserPrivilege = switchUserPrivilege;
    this.webBackOfficeProperties = webBackOfficeProperties;
  }

  @Bean
  public static ServletListenerRegistrationBean httpSessionEventPublisher() {
    return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
  }

  @Bean
  public SwitchUserFilter switchUserProcessingFilter() {
    SwitchUserFilter filter = new SwitchUserFilter();
    filter.setUserDetailsService(userDetailsService);
    filter.setSwitchAuthorityRole(switchUserPrivilege.privilege());
    filter.setSwitchUserUrl("/" + BLOSSOM_BASE_PATH + "/administration/_impersonate");
    filter.setExitUserUrl("/" + BLOSSOM_BASE_PATH + "/administration/_impersonate/logout");
    filter.setTargetUrl("/" + BLOSSOM_BASE_PATH + "/ng/");
    filter.setSwitchFailureUrl("/" + BLOSSOM_BASE_PATH + "/ng/");
    return filter;
  }


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

    http.csrf(csrf -> {
      CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
      requestHandler.setCsrfRequestAttributeName(null);
      csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(requestHandler);
    });

    http.authorizeHttpRequests(
      authorize -> authorize
        .requestMatchers(PUBLIC_URLS).permitAll()
        .requestMatchers(BLOSSOM_PUBLIC_URLS).permitAll()
        .requestMatchers(new NegatedRequestMatcher(new OrRequestMatcher(PUBLIC_URLS, BLOSSOM_PUBLIC_URLS))).fullyAuthenticated());

    http
      .authenticationProvider(limitLoginAuthenticationProvider)
      .addFilter(switchUserProcessingFilter())
      .formLogin(form -> form
        .loginPage(ANGULAR_LOGIN_PATH)
        .loginProcessingUrl("/" + BLOSSOM_BASE_PATH + "/login")
        .failureUrl(ANGULAR_LOGIN_PATH + "?error")
        .successHandler(blossomAuthenticationSuccessHandler))
      .logout(logout -> logout
        .logoutRequestMatcher(PathPatternRequestMatcher.pathPattern("/" + BLOSSOM_BASE_PATH + "/logout"))
        .deleteCookies(BLOSSOM_REMEMBER_ME_COOKIE_NAME)
        .logoutSuccessUrl(ANGULAR_LOGIN_PATH).permitAll())
      .rememberMe(rememberMe -> rememberMe
        .rememberMeCookieName(BLOSSOM_REMEMBER_ME_COOKIE_NAME))
      .exceptionHandling(exceptionHandling -> exceptionHandling
        .defaultAuthenticationEntryPointFor(
          (request, response, authException) -> response.sendError(401),
          new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest")))
      .sessionManagement(sessionManagement -> sessionManagement
        .maximumSessions(webBackOfficeProperties.getMaxSessionsPerUser()).maxSessionsPreventsLogin(true)
        .expiredSessionStrategy(
          new WebInterfaceAutoConfiguration.BlossomInvalidSessionStrategy(ANGULAR_LOGIN_PATH))
        .sessionRegistry(sessionRegistry));

    return http.build();
  }


}
