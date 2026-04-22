package com.blossomproject.autoconfigure.ui.api;

import com.blossomproject.autoconfigure.ui.MenuAutoConfiguration;
import com.blossomproject.autoconfigure.ui.common.privileges.GroupPrivilegesConfiguration;
import com.blossomproject.autoconfigure.ui.common.privileges.RolePrivilegesConfiguration;
import com.blossomproject.autoconfigure.ui.common.privileges.UserPrivilegesConfiguration;
import com.blossomproject.core.common.utils.privilege.Privilege;
import com.blossomproject.core.common.utils.privilege.SimplePrivilege;
import com.blossomproject.core.group.GroupService;
import com.blossomproject.core.role.RoleService;
import com.blossomproject.core.user.UserService;
import com.blossomproject.ui.menu.MenuItem;
import com.blossomproject.ui.menu.MenuItemBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Registers menu items for administration and system features.
 * These are conditional on core services only (not web controllers),
 * so they work for both FreeMarker and Angular-based projects.
 * Each bean uses @ConditionalOnMissingBean to avoid duplicates with
 * the WebAdministration/WebSystem configs when blossom-ui-web is present.
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnBean(MenuAutoConfiguration.class)
@AutoConfigureAfter(MenuAutoConfiguration.class)
@Import({UserPrivilegesConfiguration.class, GroupPrivilegesConfiguration.class, RolePrivilegesConfiguration.class})
public class ApiMenuItemsAutoConfiguration {

  // ── Administration ────────────────────────────────────────────────

  @Bean
  @ConditionalOnMissingBean(name = "administrationUserMenuItem")
  @ConditionalOnBean(UserService.class)
  public MenuItem administrationUserMenuItem(MenuItemBuilder builder,
    @Qualifier("administrationMenuItem") MenuItem administrationMenuItem,
    UserPrivilegesConfiguration priv) {
    return builder
      .key("users")
      .label("menu.administration.users")
      .link("/blossom/administration/users")
      .icon("fa fa-user")
      .order(1)
      .privilege(priv.usersReadPrivilege())
      .parent(administrationMenuItem)
      .build();
  }

  @Bean
  @ConditionalOnMissingBean(name = "administrationGroupMenuItem")
  @ConditionalOnBean(GroupService.class)
  public MenuItem administrationGroupMenuItem(MenuItemBuilder builder,
    @Qualifier("administrationMenuItem") MenuItem administrationMenuItem,
    GroupPrivilegesConfiguration priv) {
    return builder
      .key("groups")
      .label("menu.administration.groups")
      .link("/blossom/administration/groups")
      .icon("fa fa-users")
      .order(2)
      .privilege(priv.groupsReadPrivilegePlugin())
      .parent(administrationMenuItem)
      .build();
  }

  @Bean
  @ConditionalOnMissingBean(name = "administrationRoleMenuItem")
  @ConditionalOnBean(RoleService.class)
  public MenuItem administrationRoleMenuItem(MenuItemBuilder builder,
    @Qualifier("administrationMenuItem") MenuItem administrationMenuItem,
    RolePrivilegesConfiguration priv) {
    return builder
      .key("roles")
      .label("menu.administration.roles")
      .link("/blossom/administration/roles")
      .icon("fa fa-key")
      .order(3)
      .privilege(priv.rolesReadPrivilegePlugin())
      .parent(administrationMenuItem)
      .build();
  }

  // ── System ────────────────────────────────────────────────────────

  @Bean
  @ConditionalOnMissingBean(name = "systemDashboardMenuItem")
  public MenuItem systemDashboardMenuItem(MenuItemBuilder builder,
    @Qualifier("systemMenuItem") MenuItem systemMenuItem) {
    return builder
      .key("dashboard")
      .label("menu.system.dashboard")
      .link("/blossom/system/dashboard")
      .icon("fa fa-bar-chart")
      .order(0)
      .parent(systemMenuItem)
      .privilege(systemDashboardPrivilege())
      .build();
  }

  @Bean
  @ConditionalOnMissingBean(name = "systemDashboardPrivilege")
  public Privilege systemDashboardPrivilege() {
    return new SimplePrivilege("system", "dashboard", "manager");
  }

  @Bean
  @ConditionalOnMissingBean(name = "systemSessionMenuItem")
  public MenuItem systemSessionMenuItem(MenuItemBuilder builder,
    @Qualifier("systemMenuItem") MenuItem systemMenuItem) {
    return builder
      .key("sessions")
      .label("menu.system.sessions")
      .link("/blossom/system/sessions")
      .icon("fa fa-plug")
      .order(1)
      .parent(systemMenuItem)
      .privilege(systemSessionsPrivilege())
      .build();
  }

  @Bean
  @ConditionalOnMissingBean(name = "systemSessionsPrivilege")
  public Privilege systemSessionsPrivilege() {
    return new SimplePrivilege("system", "sessions", "manager");
  }

  @Bean
  @ConditionalOnMissingBean(name = "systemCacheMenuItem")
  public MenuItem systemCacheMenuItem(MenuItemBuilder builder,
    @Qualifier("systemMenuItem") MenuItem systemMenuItem) {
    return builder
      .key("caches")
      .label("menu.system.caches")
      .link("/blossom/system/caches")
      .icon("fa fa-magnet")
      .order(2)
      .parent(systemMenuItem)
      .privilege(systemCachesPrivilege())
      .build();
  }

  @Bean
  @ConditionalOnMissingBean(name = "systemCachesPrivilege")
  public Privilege systemCachesPrivilege() {
    return new SimplePrivilege("system", "caches", "manager");
  }

  @Bean
  @ConditionalOnMissingBean(name = "systemLoggerManagerMenuItem")
  public MenuItem systemLoggerManagerMenuItem(MenuItemBuilder builder,
    @Qualifier("systemMenuItem") MenuItem systemMenuItem) {
    return builder
      .key("loggers")
      .label("menu.system.loggers")
      .link("/blossom/system/loggers")
      .icon("fa fa-pencil")
      .order(3)
      .parent(systemMenuItem)
      .privilege(systemLoggersPrivilege())
      .build();
  }

  @Bean
  @ConditionalOnMissingBean(name = "systemLoggersPrivilege")
  public Privilege systemLoggersPrivilege() {
    return new SimplePrivilege("system", "loggers", "manager");
  }

  @Bean
  @ConditionalOnMissingBean(name = "systemSchedulerMenuItem")
  public MenuItem systemSchedulerMenuItem(MenuItemBuilder builder,
    @Qualifier("systemMenuItem") MenuItem systemMenuItem) {
    return builder
      .key("scheduler")
      .label("menu.system.scheduler")
      .link("/blossom/system/scheduler")
      .icon("fa fa-calendar")
      .order(4)
      .parent(systemMenuItem)
      .privilege(systemSchedulerPrivilege())
      .build();
  }

  @Bean
  @ConditionalOnMissingBean(name = "systemSchedulerPrivilege")
  public Privilege systemSchedulerPrivilege() {
    return new SimplePrivilege("system", "scheduler", "manager");
  }
}
