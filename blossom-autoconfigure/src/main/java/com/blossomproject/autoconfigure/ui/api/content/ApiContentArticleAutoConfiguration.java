package com.blossomproject.autoconfigure.ui.api.content;

import com.blossomproject.autoconfigure.ui.api.ApiInterfaceAutoConfiguration;
import com.blossomproject.autoconfigure.ui.common.privileges.ArticlePrivilegesConfiguration;
import com.blossomproject.module.article.ArticleDTO;
import com.blossomproject.module.article.ArticleService;
import com.blossomproject.module.search.common.AbstractQueryBuilder;
import com.blossomproject.module.search.common.AbstractSearchRequestBuilder;
import com.blossomproject.module.search.common.AbstractSearchResponse;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.ui.api.content.ArticlesApiController;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass({ArticleService.class, ArticlesApiController.class})
@ConditionalOnBean(ArticleService.class)
@AutoConfigureAfter(ApiInterfaceAutoConfiguration.class)
@Import(ArticlePrivilegesConfiguration.class)
public class ApiContentArticleAutoConfiguration {

  @Bean
  public ArticlesApiController articlesApiController(ArticleService articleService,
    SearchEngine<? extends AbstractQueryBuilder, ? extends AbstractSearchRequestBuilder, ? extends AbstractSearchResponse, ArticleDTO> searchEngine) {
    return new ArticlesApiController(articleService, searchEngine);
  }
}
