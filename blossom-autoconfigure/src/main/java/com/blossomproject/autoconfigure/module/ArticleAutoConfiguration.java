package com.blossomproject.autoconfigure.module;

import com.blossomproject.autoconfigure.core.CommonAutoConfiguration;
import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.core.common.service.AssociationServicePlugin;
import com.blossomproject.module.article.Article;
import com.blossomproject.module.article.ArticleDTO;
import com.blossomproject.module.article.ArticleDTOMapper;
import com.blossomproject.module.article.ArticleDao;
import com.blossomproject.module.article.ArticleDaoImpl;
import com.blossomproject.module.article.ArticleRepository;
import com.blossomproject.module.article.ArticleService;
import com.blossomproject.module.article.ArticleServiceImpl;
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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.plugin.core.PluginRegistry;

/**
 * Created by Maël Gargadennnec on 19/05/2017.
 */
@Configuration
@ConditionalOnClass(Article.class)
@AutoConfigureAfter(CommonAutoConfiguration.class)
@EnableJpaRepositories(basePackageClasses = ArticleRepository.class)
@EntityScan(basePackageClasses = Article.class)
public class ArticleAutoConfiguration {

  @Qualifier(PluginConstants.PLUGIN_ASSOCIATION_SERVICE)
  @Autowired
  private PluginRegistry<AssociationServicePlugin, Class<? extends AbstractDTO>> associationServicePlugins;


  @Bean
  @ConditionalOnMissingBean(ArticleService.class)
  public ArticleService articleService(ArticleDao articleDao, ArticleDTOMapper articleDTOMapper,
    ApplicationEventPublisher eventPublisher) {
    return new ArticleServiceImpl(articleDao, articleDTOMapper, eventPublisher,
      associationServicePlugins);
  }

  @Bean
  public SearchEngineConfiguration<ArticleDTO> articleSearchEngineConfiguration() {
    return new SearchEngineConfiguration<ArticleDTO>() {
      @Override
      public String getName() {
        return "menu.content.articles";
      }

      @Override
      public Class<ArticleDTO> getSupportedClass() {
        return ArticleDTO.class;
      }

      @Override
      public String[] getFields() {
        return new String[]{"dto.name", "dto.summary", "dto.content", "dto.status"};
      }

      @Override
      public String getAlias() {
        return "articles";
      }
    };
  }

  @Bean
  @ConditionalOnMissingBean(ArticleDao.class)
  public ArticleDao articleDao(ArticleRepository articleRepository) {
    return new ArticleDaoImpl(articleRepository);
  }

  @Bean
  @ConditionalOnMissingBean(ArticleDTOMapper.class)
  public ArticleDTOMapper articleDTOMapper() {
    return new ArticleDTOMapper();
  }


}
