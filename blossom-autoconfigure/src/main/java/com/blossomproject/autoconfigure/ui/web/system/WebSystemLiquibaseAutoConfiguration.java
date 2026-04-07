package com.blossomproject.autoconfigure.ui.web.system;

import com.blossomproject.core.common.utils.privilege.Privilege;
import com.blossomproject.core.common.utils.privilege.SimplePrivilege;
import com.blossomproject.ui.menu.MenuItem;
import com.blossomproject.ui.menu.MenuItemBuilder;
import com.blossomproject.ui.web.system.liquibase.BlossomLiquibaseEndpoint;
import com.blossomproject.ui.web.system.liquibase.LiquibaseController;
import java.util.Map;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Mael Gargadennnec on 04/05/2017.
 */
@Configuration
@ConditionalOnClass(LiquibaseController.class)
@ConditionalOnBean(SpringLiquibase.class)
public class WebSystemLiquibaseAutoConfiguration {

  @Bean
  public MenuItem systemLiquibaseMenuItem(MenuItemBuilder builder,
    @Qualifier("systemMenuItem") MenuItem systemMenuItem) {
    return builder
      .key("liquibase")
      .label("menu.system.liquibase")
      .link("/blossom/system/liquibase")
      .order(4)
      .icon("fa fa-flask")
      .parent(systemMenuItem)
      .privilege(liquibasePrivilegePlugin())
      .build();
  }

  @Bean
  public BlossomLiquibaseEndpoint blossomLiquibaseEndpoint(Map<String, SpringLiquibase> liquibaseBeans) {
    return new BlossomLiquibaseEndpoint(liquibaseBeans);
  }

  @Bean
  public LiquibaseController liquibaseController(BlossomLiquibaseEndpoint liquibaseEndpoint) {
    return new LiquibaseController(liquibaseEndpoint);
  }

  @Bean
  public Privilege liquibasePrivilegePlugin() {
    return new SimplePrivilege("system", "liquibase", "manager");
  }
}
