package com.blossomproject.autoconfigure.ui.api.content;

import com.blossomproject.autoconfigure.ui.api.ApiInterfaceAutoConfiguration;
import com.blossomproject.module.filemanager.FileDTO;
import com.blossomproject.module.filemanager.FileService;
import com.blossomproject.module.search.common.AbstractQueryBuilder;
import com.blossomproject.module.search.common.AbstractSearchRequestBuilder;
import com.blossomproject.module.search.common.AbstractSearchResponse;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.ui.api.administration.FileManagerApiController;
import com.blossomproject.ui.api.content.FileApiController;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(FileService.class)
@AutoConfigureAfter(ApiInterfaceAutoConfiguration.class)
public class ApiContentFileManagerAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(FileManagerApiController.class)
  @ConditionalOnClass(FileManagerApiController.class)
  public FileManagerApiController fileManagerApiController(FileService service,
      SearchEngine<? extends AbstractQueryBuilder, ? extends AbstractSearchRequestBuilder, ? extends AbstractSearchResponse, FileDTO> searchEngine) {
    return new FileManagerApiController(service, searchEngine);
  }

  @Bean
  @ConditionalOnMissingBean(FileApiController.class)
  @ConditionalOnClass(FileApiController.class)
  public FileApiController fileApiController(FileService fileService) {
    return new FileApiController(fileService);
  }
}
