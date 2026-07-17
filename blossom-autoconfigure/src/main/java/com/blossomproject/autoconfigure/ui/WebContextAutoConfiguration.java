package com.blossomproject.autoconfigure.ui;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
@Configuration
@ConditionalOnWebApplication
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
            String[] currentPaths;
            PathPatternsRequestCondition pathPatterns = mapping.getPathPatternsCondition();
            if (pathPatterns != null) {
              currentPaths = pathPatterns.getPatterns().stream()
                .map(p -> p.getPatternString()).toArray(String[]::new);
            } else {
              PatternsRequestCondition patterns = mapping.getPatternsCondition();
              currentPaths = patterns != null
                ? patterns.getPatterns().toArray(new String[0])
                : new String[]{""};
            }

            String[] prefixedPaths = java.util.Arrays.stream(currentPaths)
              .map(p -> "/" + prefix + p).toArray(String[]::new);

            RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
            PathPatternParser parser = getPatternParser();
            if (parser != null) {
              options.setPatternParser(parser);
            }

            RequestMappingInfo.Builder builder = RequestMappingInfo.paths(prefixedPaths)
              .methods(mapping.getMethodsCondition().getMethods().toArray(new RequestMethod[0]))
              .params(mapping.getParamsCondition().getExpressions().stream().map(Object::toString).toArray(String[]::new))
              .headers(mapping.getHeadersCondition().getExpressions().stream().map(Object::toString).toArray(String[]::new))
              .consumes(mapping.getConsumesCondition().getExpressions().stream().map(Object::toString).toArray(String[]::new))
              .produces(mapping.getProducesCondition().getExpressions().stream().map(Object::toString).toArray(String[]::new))
              .options(options);
            if (mapping.getName() != null) {
              builder.mappingName(mapping.getName());
            }
            if (mapping.getCustomCondition() != null) {
              builder.customCondition(mapping.getCustomCondition());
            }
            return builder.build();
          }
        };
      }
    };
  }
}
