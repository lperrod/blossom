package com.blossomproject.autoconfigure.ui.api.system;

import com.blossomproject.autoconfigure.ui.api.ApiInterfaceAutoConfiguration;
import com.blossomproject.ui.api.system.LiquibaseApiController;
import java.util.Map;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(LiquibaseApiController.class)
@ConditionalOnBean(SpringLiquibase.class)
@AutoConfigureAfter(ApiInterfaceAutoConfiguration.class)
public class ApiSystemLiquibaseAutoConfiguration {

  @Bean
  public LiquibaseApiController liquibaseApiController(Map<String, SpringLiquibase> liquibaseBeans) {
    return new LiquibaseApiController(liquibaseBeans);
  }
}
