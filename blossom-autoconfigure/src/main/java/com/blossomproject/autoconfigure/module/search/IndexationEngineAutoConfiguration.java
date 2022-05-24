package com.blossomproject.autoconfigure.module.search;

import com.blossomproject.autoconfigure.module.search.elasticsearch.ElasticsearchAutoConfiguration;
import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.module.search.common.IndexationEngine;
import com.blossomproject.module.search.common.IndexationEventListeners;
import com.blossomproject.module.search.common.facet.AggregationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.config.EnablePluginRegistries;

/**
 * Created by maelg on 07/05/2017.
 */
@Configuration
@EnablePluginRegistries({IndexationEngine.class, AggregationConverter.class})
@ConditionalOnBean({ElasticsearchAutoConfiguration.class})
public class IndexationEngineAutoConfiguration {

  @Autowired
  @Qualifier(value = PluginConstants.PLUGIN_INDEXATION_ENGINE)
  private PluginRegistry<IndexationEngine, Class<? extends AbstractDTO>> registry;


  @Bean
  public IndexationEventListeners indexationEventListeners() {
    return new IndexationEventListeners(registry);
  }

}
