package com.blossomproject.module.search.elasticsearch.module;

import com.blossomproject.module.search.common.IndexationEngine;
import com.blossomproject.module.search.common.IndexationJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ArticleIndexationJob extends IndexationJob {

  @Autowired
  @Qualifier("articleIndexationEngine")
  IndexationEngine fileIndexationEngine;

  @Override
  protected IndexationEngine getIndexationEngine() {
    return fileIndexationEngine;
  }
}
