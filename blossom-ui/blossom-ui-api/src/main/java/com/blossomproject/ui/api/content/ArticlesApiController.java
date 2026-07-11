package com.blossomproject.ui.api.content;

import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.module.article.ArticleCreateForm;
import com.blossomproject.module.article.ArticleDTO;
import com.blossomproject.module.article.ArticleService;
import com.blossomproject.module.article.ArticleUpdateForm;
import com.blossomproject.module.search.common.AbstractQueryBuilder;
import com.blossomproject.module.search.common.AbstractSearchRequestBuilder;
import com.blossomproject.module.search.common.AbstractSearchResponse;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.ui.stereotype.BlossomApiController;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@BlossomApiController
@RequestMapping("/content/articles")
public class ArticlesApiController {

  private final ArticleService articleService;
  private final SearchEngine<? extends AbstractQueryBuilder, ? extends AbstractSearchRequestBuilder, ? extends AbstractSearchResponse, ArticleDTO> searchEngine;

  public ArticlesApiController(ArticleService articleService,
    SearchEngine<? extends AbstractQueryBuilder, ? extends AbstractSearchRequestBuilder, ? extends AbstractSearchResponse, ArticleDTO> searchEngine) {
    this.articleService = articleService;
    this.searchEngine = searchEngine;
  }

  @GetMapping
  @PreAuthorize("hasAuthority('content:articles:read')")
  public Page<ArticleDTO> list(
    @RequestParam(value = "q", required = false) String q,
    @PageableDefault(size = 25) Pageable pageable) {
    if (Strings.isNullOrEmpty(q)) {
      return this.articleService.getAll(pageable);
    }
    return this.searchEngine.search(q, pageable).getPage();
  }

  @PostMapping
  @PreAuthorize("hasAuthority('content:articles:create')")
  public ResponseEntity<ArticleDTO> create(
    @NotNull @Valid @RequestBody ArticleCreateForm articleCreateForm) throws Exception {
    return new ResponseEntity<>(articleService.create(articleCreateForm), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('content:articles:read')")
  public ResponseEntity<ArticleDTO> get(@PathVariable Long id) {
    ArticleDTO article = articleService.getOne(id);
    if (article == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(article, HttpStatus.OK);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('content:articles:write')")
  public ResponseEntity<ArticleDTO> update(@PathVariable Long id,
    @Valid @RequestBody ArticleUpdateForm articleUpdateForm) {
    ArticleDTO article = articleService.getOne(id);
    if (article == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(articleService.update(id, articleUpdateForm), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('content:articles:delete')")
  public ResponseEntity<Map<Class<? extends AbstractDTO>, Long>> delete(@PathVariable Long id,
    @RequestParam(value = "force", defaultValue = "false", required = false) boolean force) {
    ArticleDTO article = this.articleService.getOne(id);
    if (article == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    Optional<Map<Class<? extends AbstractDTO>, Long>> result = this.articleService.delete(article, force);
    if (!result.isPresent() || result.get().isEmpty()) {
      return new ResponseEntity<>(HttpStatus.OK);
    } else {
      return new ResponseEntity<>(result.get(), HttpStatus.CONFLICT);
    }
  }
}
