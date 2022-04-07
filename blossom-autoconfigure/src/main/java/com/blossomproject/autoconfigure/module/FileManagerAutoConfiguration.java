package com.blossomproject.autoconfigure.module;

import com.blossomproject.autoconfigure.core.CommonAutoConfiguration;
import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.core.common.service.AssociationServicePlugin;
import com.blossomproject.module.filemanager.File;
import com.blossomproject.module.filemanager.FileContentDao;
import com.blossomproject.module.filemanager.FileContentDaoImpl;
import com.blossomproject.module.filemanager.FileContentRepository;
import com.blossomproject.module.filemanager.FileDTOMapper;
import com.blossomproject.module.filemanager.FileDao;
import com.blossomproject.module.filemanager.FileDaoImpl;
import com.blossomproject.module.filemanager.FileRepository;
import com.blossomproject.module.filemanager.FileService;
import com.blossomproject.module.filemanager.FileServiceImpl;
import com.blossomproject.module.filemanager.digest.DigestUtil;
import com.blossomproject.module.filemanager.digest.DigestUtilImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.plugin.core.PluginRegistry;

/**
 * Created by Maël Gargadennnec on 19/05/2017.
 */
@Configuration
@ConditionalOnClass({File.class})
@AutoConfigureAfter(CommonAutoConfiguration.class)
@EnableJpaRepositories(basePackageClasses = FileRepository.class)
@PropertySource("classpath:/filemanager.properties")
@EntityScan(basePackageClasses = File.class)
public class FileManagerAutoConfiguration {

  @Qualifier(PluginConstants.PLUGIN_ASSOCIATION_SERVICE)
  @Autowired
  private PluginRegistry<AssociationServicePlugin, Class<? extends AbstractDTO>> associationServicePlugins;

  @Bean
  @ConditionalOnMissingBean(FileContentDao.class)
  public FileContentDao fileContentDao(FileContentRepository fileContentRepository) {
    return new FileContentDaoImpl(fileContentRepository);
  }

  @Bean
  @ConditionalOnMissingBean(DigestUtil.class)
  public DigestUtil digestUtil() {
    return new DigestUtilImpl();
  }

  @Bean
  @ConditionalOnMissingBean(FileService.class)
  public FileService fileService(FileDao fileDao, FileDTOMapper fileDTOMapper,
    FileContentDao fileContentDao,
    DigestUtil digestUtil, ApplicationEventPublisher eventPublisher) {
    return new FileServiceImpl(fileDao, fileDTOMapper, fileContentDao, digestUtil, eventPublisher,
      associationServicePlugins);
  }

  @Bean
  @ConditionalOnMissingBean(FileDao.class)
  public FileDao fileDao(FileRepository fileRepository) {
    return new FileDaoImpl(fileRepository);
  }

  @Bean
  @ConditionalOnMissingBean(FileDTOMapper.class)
  public FileDTOMapper fileDTOMapper() {
    return new FileDTOMapper();
  }


}
