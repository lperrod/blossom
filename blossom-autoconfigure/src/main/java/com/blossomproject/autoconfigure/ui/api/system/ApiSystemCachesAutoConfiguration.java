package com.blossomproject.autoconfigure.ui.api.system;

import com.blossomproject.autoconfigure.core.CacheAutoConfiguration.BlossomCacheAutoConfiguration;
import com.blossomproject.autoconfigure.ui.api.ApiInterfaceAutoConfiguration;
import com.blossomproject.core.cache.BlossomCacheManager;
import com.blossomproject.ui.api.system.CachesApiController;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({BlossomCacheAutoConfiguration.class, ApiInterfaceAutoConfiguration.class})
@ConditionalOnClass(CachesApiController.class)
@ConditionalOnBean(BlossomCacheManager.class)
public class ApiSystemCachesAutoConfiguration {

  @Bean
  public CachesApiController cachesApiController(BlossomCacheManager cacheManager) {
    return new CachesApiController(cacheManager);
  }
}
