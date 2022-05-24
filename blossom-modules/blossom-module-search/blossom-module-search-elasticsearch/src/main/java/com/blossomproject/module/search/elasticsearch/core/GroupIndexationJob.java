package com.blossomproject.module.search.elasticsearch.core;


import com.blossomproject.module.search.common.IndexationEngine;
import com.blossomproject.module.search.common.IndexationJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class GroupIndexationJob extends IndexationJob {

  @Autowired
  @Qualifier("groupIndexationEngine")
  IndexationEngine groupIndexationEngine;

  @Override
  protected IndexationEngine getIndexationEngine() {
    return groupIndexationEngine;
  }
}
