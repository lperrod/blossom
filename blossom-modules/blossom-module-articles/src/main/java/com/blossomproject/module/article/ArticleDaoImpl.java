package com.blossomproject.module.article;

import com.blossomproject.core.common.dao.GenericCrudDaoImpl;
import com.google.common.base.Preconditions;
import com.querydsl.core.types.Predicate;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
public class ArticleDaoImpl extends GenericCrudDaoImpl<Article> implements ArticleDao {

  private final ArticleRepository articleRepository;

  public ArticleDaoImpl(ArticleRepository repository) {
    super(repository);
    Preconditions.checkNotNull(repository);
    this.articleRepository = repository;
  }

  @Override
  protected Article updateEntity(Article originalEntity, Article modifiedEntity) {
    originalEntity.setName(modifiedEntity.getName());
    originalEntity.setSummary(modifiedEntity.getSummary());
    originalEntity.setContent(modifiedEntity.getContent());
    originalEntity.setStatus(modifiedEntity.getStatus());
    return originalEntity;
  }

  @Override
  protected Predicate computeSearchPredicate(String query) {
    QArticle article = QArticle.article;
    return article.name.containsIgnoreCase(query);
  }
}
