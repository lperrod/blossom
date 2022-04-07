package com.blossomproject.autoconfigure.core;

import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.core.common.service.AssociationServicePlugin;
import com.blossomproject.core.group.Group;
import com.blossomproject.core.group.GroupDTOMapper;
import com.blossomproject.core.group.GroupDao;
import com.blossomproject.core.group.GroupDaoImpl;
import com.blossomproject.core.group.GroupRepository;
import com.blossomproject.core.group.GroupService;
import com.blossomproject.core.group.GroupServiceImpl;
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
@ConditionalOnClass(Group.class)
@AutoConfigureAfter(CommonAutoConfiguration.class)
@EnableJpaRepositories(basePackageClasses = GroupRepository.class)
@EntityScan(basePackageClasses = Group.class)
public class GroupAutoConfiguration {

  @Qualifier(PluginConstants.PLUGIN_ASSOCIATION_SERVICE)
  @Autowired
  private PluginRegistry<AssociationServicePlugin, Class<? extends AbstractDTO>> associationServicePlugins;

  @Bean
  @ConditionalOnMissingBean(GroupService.class)
  public GroupService groupService(GroupDao groupDao, GroupDTOMapper groupDTOMapper,
    ApplicationEventPublisher eventPublisher) {
    return new GroupServiceImpl(groupDao, groupDTOMapper, eventPublisher,
      associationServicePlugins);
  }

  @Bean
  @ConditionalOnMissingBean(GroupDao.class)
  public GroupDao groupDao(GroupRepository groupRepository) {
    return new GroupDaoImpl(groupRepository);
  }

  @Bean
  @ConditionalOnMissingBean(GroupDTOMapper.class)
  public GroupDTOMapper groupDTOMapper() {
    return new GroupDTOMapper();
  }


}
