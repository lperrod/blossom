package com.blossomproject.autoconfigure.ui.angular;

import static com.blossomproject.autoconfigure.ui.WebContextAutoConfiguration.BLOSSOM_BASE_PATH;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication
public class AngularInterfaceAutoConfiguration {

  @Bean
  public WebMvcConfigurer angularWebMvcConfigurer() {
    return new WebMvcConfigurer() {

      @Override
      public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/" + BLOSSOM_BASE_PATH + "/ng/**")
          .addResourceLocations("classpath:/static/blossom/")
          .resourceChain(true);
      }

      @Override
      public void addViewControllers(ViewControllerRegistry registry) {
        // Forward all Angular routes to index.html for SPA routing
        registry.addViewController("/" + BLOSSOM_BASE_PATH + "/ng/")
          .setViewName("forward:/" + BLOSSOM_BASE_PATH + "/ng/index.html");
        registry.addViewController("/" + BLOSSOM_BASE_PATH + "/ng/{path:[^\\.]*}")
          .setViewName("forward:/" + BLOSSOM_BASE_PATH + "/ng/index.html");
        registry.addViewController("/" + BLOSSOM_BASE_PATH + "/ng/{path:[^\\.]*}/{subpath:[^\\.]*}")
          .setViewName("forward:/" + BLOSSOM_BASE_PATH + "/ng/index.html");
        registry.addViewController("/" + BLOSSOM_BASE_PATH + "/ng/{path:[^\\.]*}/{subpath:[^\\.]*}/{subsubpath:[^\\.]*}")
          .setViewName("forward:/" + BLOSSOM_BASE_PATH + "/ng/index.html");
      }
    };
  }
}
