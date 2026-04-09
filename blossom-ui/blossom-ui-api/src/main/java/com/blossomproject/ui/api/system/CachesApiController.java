package com.blossomproject.ui.api.system;

import com.blossomproject.core.cache.BlossomCache;
import com.blossomproject.core.cache.BlossomCacheManager;
import com.blossomproject.ui.stereotype.BlossomApiController;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.common.base.Strings;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.cache.Cache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@BlossomApiController
@RequestMapping("/system/caches")
@PreAuthorize("hasAuthority('system:caches:manager')")
public class CachesApiController {

  private final BlossomCacheManager cacheManager;

  public CachesApiController(BlossomCacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @GetMapping
  public ResponseEntity<List<Map<String, Object>>> list(
    @RequestParam(name = "q", defaultValue = "") String q) {
    Collection<String> cacheNames = cacheManager.getCacheNames();
    List<Map<String, Object>> caches = cacheNames.stream()
      .filter(name -> Strings.isNullOrEmpty(q) || name.toLowerCase().contains(q.toLowerCase()))
      .map(name -> {
        Cache cache = cacheManager.getCache(name);
        Map<String, Object> cacheInfo = new HashMap<>();
        cacheInfo.put("name", name);
        if (cache instanceof BlossomCache blossomCache) {
          cacheInfo.put("enabled", blossomCache.isEnabled());
          CacheStats stats = blossomCache.getNativeCache().stats();
          cacheInfo.put("size", blossomCache.getNativeCache().estimatedSize());
          cacheInfo.put("hits", stats.hitCount());
          cacheInfo.put("misses", stats.missCount());
          cacheInfo.put("evictions", stats.evictionCount());
        }
        return cacheInfo;
      })
      .collect(Collectors.toList());
    return new ResponseEntity<>(caches, HttpStatus.OK);
  }

  @PostMapping("/{name}/_empty")
  public ResponseEntity<Void> emptyCache(@PathVariable String name) {
    Cache cache = cacheManager.getCache(name);
    if (cache != null) {
      cache.clear();
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/{name}/_disable")
  public ResponseEntity<Void> disableCache(@PathVariable String name) {
    Cache cache = cacheManager.getCache(name);
    if (cache instanceof BlossomCache blossomCache) {
      blossomCache.disable();
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/{name}/_enable")
  public ResponseEntity<Void> enableCache(@PathVariable String name) {
    Cache cache = cacheManager.getCache(name);
    if (cache instanceof BlossomCache blossomCache) {
      blossomCache.enable();
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/_empty")
  public ResponseEntity<Void> emptyCaches() {
    cacheManager.getCacheNames().forEach(name -> {
      Cache cache = cacheManager.getCache(name);
      if (cache != null) {
        cache.clear();
      }
    });
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
