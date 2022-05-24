package com.blossomproject.module.article;

import com.blossomproject.core.common.service.SearchAndCrudService;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
public interface ArticleService extends SearchAndCrudService<ArticleDTO> {

  ArticleDTO create(ArticleCreateForm articleCreateForm);

  ArticleDTO update(Long groupId, ArticleUpdateForm articleUpdateForm);

}
