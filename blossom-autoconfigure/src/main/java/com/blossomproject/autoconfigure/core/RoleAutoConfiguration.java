package com.blossomproject.autoconfigure.core;

import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.core.common.service.AssociationServicePlugin;
import com.blossomproject.core.common.utils.privilege.Privilege;
import com.blossomproject.core.role.Role;
import com.blossomproject.core.role.RoleDTOMapper;
import com.blossomproject.core.role.RoleDao;
import com.blossomproject.core.role.RoleDaoImpl;
import com.blossomproject.core.role.RoleRepository;
import com.blossomproject.core.role.RoleService;
import com.blossomproject.core.role.RoleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.plugin.core.PluginRegistry;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
@Configuration
@ConditionalOnClass(Role.class)
@AutoConfigureAfter(CommonAutoConfiguration.class)
@EnableJpaRepositories(basePackageClasses = RoleRepository.class)
@EntityScan(basePackageClasses = Role.class)
public class RoleAutoConfiguration {

  @Qualifier(PluginConstants.PLUGIN_PRIVILEGES)
  @Autowired
  private PluginRegistry<Privilege, String> privilegesRegistry;

  @Qualifier(PluginConstants.PLUGIN_ASSOCIATION_SERVICE)
  @Autowired
  private PluginRegistry<AssociationServicePlugin, Class<? extends AbstractDTO>> associationServicePlugins;

  @Bean
  @ConditionalOnMissingBean(RoleService.class)
  public RoleService roleService(RoleDao roleDao, RoleDTOMapper roleDTOMapper,
    ApplicationEventPublisher eventPublisher) {
    return new RoleServiceImpl(roleDao, roleDTOMapper, privilegesRegistry, eventPublisher,
      associationServicePlugins);
  }

  @Bean
  @ConditionalOnMissingBean(RoleDao.class)
  public RoleDao roleDao(RoleRepository roleRepository) {
    return new RoleDaoImpl(roleRepository);
  }

  @Bean
  @ConditionalOnMissingBean(RoleDTOMapper.class)
  public RoleDTOMapper roleDTOMapper() {
    return new RoleDTOMapper();
  }


}

