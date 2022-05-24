package com.blossomproject.module.search.elasticsearch.core;


import com.blossomproject.module.search.common.IndexationEngine;
import com.blossomproject.module.search.common.IndexationJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class RoleIndexationJob extends IndexationJob {

  @Autowired
  @Qualifier("roleIndexationEngine")
  IndexationEngine roleIndexationEngine;

  @Override
  protected IndexationEngine getIndexationEngine() {
    return roleIndexationEngine;
  }
}
