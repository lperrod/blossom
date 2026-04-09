package com.blossomproject.autoconfigure.ui;

import com.blossomproject.autoconfigure.module.search.elasticsearch.FilteredWebContextAutoConfiguration;
import com.blossomproject.core.common.utils.privilege.Privilege;
import com.blossomproject.core.common.utils.privilege.SimplePrivilege;
import com.blossomproject.ui.i18n.RestrictedSessionLocaleResolver;
import com.blossomproject.ui.stereotype.BlossomApiController;
import com.blossomproject.ui.stereotype.BlossomController;
import com.google.common.collect.Iterables;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnMissingBean(FilteredWebContextAutoConfiguration.class)
@AutoConfigureBefore({WebMvcAutoConfiguration.class, ErrorMvcAutoConfiguration.class})
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class WebContextAutoConfiguration implements WebMvcConfigurer {

  public static final String BLOSSOM_BASE_PATH = "blossom";

  public static final String BLOSSOM_API_BASE_PATH = BLOSSOM_BASE_PATH + "/api";

  @Autowired
  private MessageSource messageSource;

  @Bean
  public LocaleResolver localeResolver(Set<Locale> availableLocales) {
    RestrictedSessionLocaleResolver resolver = new RestrictedSessionLocaleResolver(
      availableLocales);
    resolver.setDefaultLocale(Iterables.getFirst(availableLocales, Locale.ENGLISH));
    return resolver;
  }


  @Bean
  public LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
    lci.setParamName("lang");
    return lci;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(localeChangeInterceptor())
      .addPathPatterns("/" + BLOSSOM_BASE_PATH + "/**", "/" + BLOSSOM_API_BASE_PATH + "/**");
  }

  @Override
  public Validator getValidator() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.setValidationMessageSource(messageSource);
    return validator;
  }

  @Bean
  public Privilege switchUserPrivilege() {
    return new SimplePrivilege("administration", "admin", "impersonate");
  }

  @Bean
  public WebMvcRegistrations webMvcRegistrationsHandlerMapping() {
    return new WebMvcRegistrations() {
      @Override
      public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping() {

          @Override
          protected void registerHandlerMethod(Object handler, Method method,
            RequestMappingInfo mapping) {
            Class<?> beanType = method.getDeclaringClass();
            if (AnnotationUtils.findAnnotation(beanType, BlossomController.class) != null) {
              mapping = computeMapping(mapping, BLOSSOM_BASE_PATH);
            } else if (AnnotationUtils.findAnnotation(beanType, BlossomApiController.class)
              != null) {
              mapping = computeMapping(mapping, BLOSSOM_API_BASE_PATH);
            }

            super.registerHandlerMethod(handler, method, mapping);
          }

          private RequestMappingInfo computeMapping(RequestMappingInfo mapping, String prefix) {
            PatternsRequestCondition apiPattern = new PatternsRequestCondition(prefix)
              .combine(
                new PatternsRequestCondition(mapping.getPatternsCondition().getPatterns().toArray(new String[]{})));

            return new RequestMappingInfo(mapping.getName(), apiPattern,
              mapping.getMethodsCondition(),
              mapping.getParamsCondition(), mapping.getHeadersCondition(),
              mapping.getConsumesCondition(),
              mapping.getProducesCondition(), null, mapping.getCustomCondition());
          }
        };
      }
    };
  }
}
