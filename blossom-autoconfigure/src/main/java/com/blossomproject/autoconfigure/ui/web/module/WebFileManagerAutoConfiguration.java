package com.blossomproject.autoconfigure.ui.web.module;

import com.blossomproject.autoconfigure.ui.common.privileges.FileManagerPrivilegesConfiguration;
import com.blossomproject.autoconfigure.ui.web.WebInterfaceAutoConfiguration;
import com.blossomproject.module.filemanager.FileDTO;
import com.blossomproject.module.filemanager.FileService;
import com.blossomproject.module.search.common.AbstractQueryBuilder;
import com.blossomproject.module.search.common.AbstractSearchRequestBuilder;
import com.blossomproject.module.search.common.AbstractSearchResponse;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.ui.menu.MenuItem;
import com.blossomproject.ui.menu.MenuItemBuilder;
import com.blossomproject.ui.web.content.filemanager.FileController;
import com.blossomproject.ui.web.content.filemanager.FileManagerController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

/**
 * Created by Maël Gargadennnec on 19/05/2017.
 */

@Configuration
@ConditionalOnProperty(prefix = "blossom.filemanager.", name = "enabled")
@ConditionalOnClass({FileService.class, FileController.class})
@ConditionalOnBean(FileService.class)
@AutoConfigureAfter(WebInterfaceAutoConfiguration.class)
@Import(FileManagerPrivilegesConfiguration.class)
public class WebFileManagerAutoConfiguration {

  private final FileManagerPrivilegesConfiguration fileManagerPrivilegesConfiguration;

  public WebFileManagerAutoConfiguration(
    FileManagerPrivilegesConfiguration fileManagerPrivilegesConfiguration) {
    this.fileManagerPrivilegesConfiguration = fileManagerPrivilegesConfiguration;
  }


  @Bean
  @Order(3)
  @ConditionalOnMissingBean(name = "contentMenuItem")
  public MenuItem contentMenuItem(MenuItemBuilder builder) {
    return builder
      .key("content")
      .label("menu.content")
      .icon("fa fa-book")
      .link("/blossom/content")
      .leaf(false)
      .build();
  }

  @Bean
  public MenuItem contentFileManagerMenuItem(MenuItemBuilder builder, @Qualifier("contentMenuItem") MenuItem contentMenuItem) {
    return builder
      .key("filemanager")
      .label("menu.content.filemanager")
      .link("/blossom/content/filemanager")
      .icon("fa fa-photo")
      .order(0)
      .privilege(fileManagerPrivilegesConfiguration.fileManagerReadPrivilegePlugin())
      .parent(contentMenuItem).build();
  }

  @Bean
  public FileManagerController fileManagerController(FileService fileService,
    SearchEngine<? extends AbstractQueryBuilder, ? extends AbstractSearchRequestBuilder, ? extends AbstractSearchResponse, FileDTO> searchEngine) {
    return new FileManagerController(fileService, searchEngine);
  }


}
