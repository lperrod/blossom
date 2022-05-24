package com.blossomproject.autoconfigure.module.search;

import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.module.search.common.DefaultOmnisearchServiceImpl;
import com.blossomproject.module.search.common.OmnisearchService;
import com.blossomproject.module.search.common.SearchEngine;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;

@Configuration
public class SearchEngineAutoConfiguration {

  @Bean
  @Qualifier(PluginConstants.PLUGIN_SEARCH_ENGINE)
  public PluginRegistry<SearchEngine<?, ?, ?, ? extends AbstractDTO>, Class<? extends AbstractDTO>> searchEnginesRegistry(
    List<SearchEngine<?, ?, ?, ? extends AbstractDTO>> searchEngines) {
    return OrderAwarePluginRegistry.of(searchEngines);
  }

  @Bean
  @ConditionalOnMissingBean(OmnisearchService.class)
  public OmnisearchService defaultOmnisearchService() {
    return new DefaultOmnisearchServiceImpl();
  }


}
