package com.blossomproject.autoconfigure.module;

import com.blossomproject.autoconfigure.core.CommonAutoConfiguration;
import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.core.common.service.AssociationServicePlugin;
import com.blossomproject.module.filemanager.File;
import com.blossomproject.module.filemanager.FileDTOMapper;
import com.blossomproject.module.filemanager.FileDao;
import com.blossomproject.module.filemanager.FileDaoImpl;
import com.blossomproject.module.filemanager.FileRepository;
import com.blossomproject.module.filemanager.FileService;
import com.blossomproject.module.filemanager.FileServiceImpl;
import com.blossomproject.module.filemanager.FileDTO;
import com.blossomproject.module.filemanager.digest.DigestUtil;
import com.blossomproject.module.filemanager.digest.DigestUtilImpl;
import com.blossomproject.module.search.common.DefaultSearchEngineImpl;
import com.blossomproject.module.search.common.SearchEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.plugin.core.PluginRegistry;

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
  @ConditionalOnMissingBean(DigestUtil.class)
  public DigestUtil digestUtil() {
    return new DigestUtilImpl();
  }

  @Bean
  @ConditionalOnMissingBean(FileService.class)
  public FileService fileService(FileDao fileDao, FileDTOMapper fileDTOMapper,
    DigestUtil digestUtil, ApplicationEventPublisher eventPublisher) {
    return new FileServiceImpl(fileDao, fileDTOMapper, digestUtil, eventPublisher,
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

  @Bean
  @ConditionalOnMissingBean(name = "fileSearchEngineConfiguration")
  public SearchEngineConfiguration<FileDTO> fileSearchEngineConfiguration() {
    return new SearchEngineConfiguration<>() {
      @Override public String getName() { return "menu.content.filemanager"; }
      @Override public Class<FileDTO> getSupportedClass() { return FileDTO.class; }
      @Override public String[] getFields() { return new String[]{"dto.name", "dto.contentType"}; }
      @Override public String getAlias() { return "files"; }
    };
  }

  @Bean
  @ConditionalOnMissingBean(name = "fileDefaultSearchEngine")
  public DefaultSearchEngineImpl<FileDTO> fileDefaultSearchEngine(FileService fileService,
      SearchEngineConfiguration<FileDTO> fileSearchEngineConfiguration) {
    return new DefaultSearchEngineImpl<>(fileSearchEngineConfiguration, fileService);
  }

}
