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
        // Redirect root to Angular app
        registry.addRedirectViewController("/", "/" + BLOSSOM_BASE_PATH + "/ng/");

        String forward = "forward:/" + BLOSSOM_BASE_PATH + "/ng/index.html";
        String base = "/" + BLOSSOM_BASE_PATH + "/ng";
        String seg = "/{s%d:[^\\.]*}";

        // Forward all Angular routes (up to 7 levels deep) to index.html for SPA routing
        registry.addViewController(base + "/").setViewName(forward);
        StringBuilder path = new StringBuilder(base);
        for (int i = 1; i <= 7; i++) {
          path.append(String.format(seg, i));
          registry.addViewController(path.toString()).setViewName(forward);
        }
      }
    };
  }
}
