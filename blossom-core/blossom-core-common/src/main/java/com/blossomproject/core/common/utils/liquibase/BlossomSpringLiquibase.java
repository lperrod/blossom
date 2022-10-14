package com.blossomproject.core.common.utils.liquibase;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.integration.spring.SpringResourceAccessor;
import liquibase.util.file.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class BlossomSpringLiquibase extends SpringLiquibase {

  private static final Logger logger = LoggerFactory.getLogger(BlossomSpringLiquibase.class);

  private final ResourceLoader resourceLoader;

  public BlossomSpringLiquibase(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  protected SpringResourceAccessor createResourceOpener() {
    return new BlossomSpringResourceOpener(getChangeLog());
  }


  public class BlossomSpringResourceOpener extends SpringResourceAccessor {

    private String parentFile;

    public BlossomSpringResourceOpener(String parentFile) {
      super(resourceLoader);
      this.parentFile = parentFile;
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean includeFiles,
      boolean includeDirectories, boolean recursive) throws IOException {
      SortedSet<String> returnSet = new TreeSet<>();
      if (path.startsWith("classpath*:")) {
        String tempFile = FilenameUtils.concat(FilenameUtils.getFullPath(relativeTo), path);
        final String classpathBasePath = path.substring("classpath*:".length()) + (path.endsWith("/") ? "" : "/");

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<String> resources = Lists
          .newArrayList(resolver.getResources(tempFile + "*.xml")).stream()
          .sorted(Comparator.comparing(Resource::getFilename))
          .map(resource -> "classpath:" + classpathBasePath + resource.getFilename())
          .collect(Collectors.toList());

        for (String res : resources) {
          returnSet.add(res);
        }

        return returnSet;
      } else {
        return super.list(relativeTo, path, includeFiles, includeDirectories, recursive);
      }
    }
  }
}
