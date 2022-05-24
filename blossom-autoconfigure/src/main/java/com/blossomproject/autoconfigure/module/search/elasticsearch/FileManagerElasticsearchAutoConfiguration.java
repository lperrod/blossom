package com.blossomproject.autoconfigure.module.search.elasticsearch;

import com.blossomproject.autoconfigure.module.FileManagerAutoConfiguration;
import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.module.filemanager.File;
import com.blossomproject.module.filemanager.FileDTO;
import com.blossomproject.module.filemanager.FileService;
import com.blossomproject.module.search.common.IndexationEngineConfiguration;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.module.search.common.SearchEngineConfiguration;
import com.blossomproject.module.search.common.SummaryDTO;
import com.blossomproject.module.search.common.SummaryDTO.SummaryDTOBuilder;
import com.blossomproject.module.search.elasticsearch.AggregationConverter;
import com.blossomproject.module.search.elasticsearch.ElasticsearchIndexationEngineImpl;
import com.blossomproject.module.search.elasticsearch.ElasticsearchSearchEngineImpl;
import com.blossomproject.module.search.elasticsearch.module.FileIndexationJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Function;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.Client;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
@ConditionalOnClass({File.class})
@AutoConfigureAfter(FileManagerAutoConfiguration.class)
@ConditionalOnBean({ElasticsearchAutoConfiguration.class})
public class FileManagerElasticsearchAutoConfiguration {

  @Bean
  public IndexationEngineConfiguration<FileDTO> fileIndexationEngineConfiguration(
    @Value("classpath:/elasticsearch/files.json") Resource resource) {
    return new IndexationEngineConfiguration<FileDTO>() {
      @Override
      public Class<FileDTO> getSupportedClass() {
        return FileDTO.class;
      }

      @Override
      public Resource getSource() {
        return resource;
      }

      @Override
      public String getAlias() {
        return "files";
      }

      @Override
      public Function<FileDTO, String> getTypeFunction() {
        return u -> "file";
      }

      @Override
      public Function<FileDTO, SummaryDTO> getSummaryFunction() {
        return u -> SummaryDTOBuilder.create().id(u.getId()).type(this.getTypeFunction().apply(u))
          .name(u.getName()).description(u.getContentType())
          .uri("/blossom/content/files/" + u.getId()).build();
      }
    };
  }

  @Bean
  public ElasticsearchIndexationEngineImpl<FileDTO> fileIndexationEngine(Client client,
    FileService fileService,
    BulkProcessor bulkProcessor, ObjectMapper objectMapper,
    IndexationEngineConfiguration<FileDTO> fileIndexationEngineConfiguration) {
    return new ElasticsearchIndexationEngineImpl<>(client, fileService, bulkProcessor, objectMapper,
      fileIndexationEngineConfiguration);
  }


  @Bean
  public ElasticsearchSearchEngineImpl<FileDTO> fileSearchEngine(Client client, ObjectMapper objectMapper,
    SearchEngineConfiguration<FileDTO> fileSearchEngineConfiguration,
    @Qualifier(PluginConstants.PLUGIN_SEARCH_ENGINE_AGGREGATION_CONVERTERS)
    PluginRegistry<AggregationConverter, SearchEngine> aggregationConverters) {
    return new ElasticsearchSearchEngineImpl<>(client, objectMapper, aggregationConverters, fileSearchEngineConfiguration);
  }

  @Bean
  @Qualifier("fileIndexationFullJob")
  public JobDetailFactoryBean fileIndexationFullJob() {
    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
    factoryBean.setJobClass(FileIndexationJob.class);
    factoryBean.setGroup("Indexation");
    factoryBean.setName("Files Indexation Job");
    factoryBean.setDescription("Files full indexation Job");
    factoryBean.setDurability(true);
    return factoryBean;
  }

  @Bean
  @Qualifier("fileScheduledIndexationTrigger")
  public SimpleTriggerFactoryBean fileScheduledIndexationTrigger(
    @Qualifier("fileIndexationFullJob") JobDetail fileIndexationFullJob) {
    SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
    factoryBean.setName("File re-indexation");
    factoryBean.setDescription("Periodic re-indexation of all files of the application");
    factoryBean.setJobDetail(fileIndexationFullJob);
    factoryBean.setStartDelay((long) 30 * 1000);
    factoryBean.setRepeatInterval(1 * 60 * 60 * 1000);
    factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
    factoryBean.setMisfireInstruction(
      SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
    return factoryBean;
  }
}
