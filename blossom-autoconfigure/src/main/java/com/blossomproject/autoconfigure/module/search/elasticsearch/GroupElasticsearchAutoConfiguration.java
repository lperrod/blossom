package com.blossomproject.autoconfigure.module.search.elasticsearch;

import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.group.GroupDTO;
import com.blossomproject.core.group.GroupService;
import com.blossomproject.module.search.common.IndexationEngineConfiguration;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.module.search.common.SearchEngineConfiguration;
import com.blossomproject.module.search.common.SummaryDTO;
import com.blossomproject.module.search.common.SummaryDTO.SummaryDTOBuilder;
import com.blossomproject.module.search.elasticsearch.AggregationConverter;
import com.blossomproject.module.search.elasticsearch.ElasticsearchIndexationEngineImpl;
import com.blossomproject.module.search.elasticsearch.ElasticsearchSearchEngineImpl;
import com.blossomproject.module.search.elasticsearch.core.GroupIndexationJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.function.Function;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.Client;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
@ConditionalOnBean({ElasticsearchAutoConfiguration.class})
public class GroupElasticsearchAutoConfiguration {

  @Bean
  public IndexationEngineConfiguration<GroupDTO> groupIndexationEngineConfiguration(
    @Value("classpath:/elasticsearch/groups.json") Resource resource) {
    return new IndexationEngineConfiguration<GroupDTO>() {
      @Override
      public Class<GroupDTO> getSupportedClass() {
        return GroupDTO.class;
      }

      @Override
      public Resource getSource() {
        return resource;
      }

      @Override
      public String getAlias() {
        return "groups";
      }

      @Override
      public Function<GroupDTO, String> getTypeFunction() {
        return u -> "group";
      }

      @Override
      public Function<GroupDTO, SummaryDTO> getSummaryFunction() {
        return u -> SummaryDTOBuilder.create().id(u.getId()).type(this.getTypeFunction().apply(u))
          .name(u.getName()).description(u.getDescription())
          .uri("/blossom/administration/groups/" + u.getId()).build();
      }
    };
  }

  @Bean
  public ElasticsearchIndexationEngineImpl<GroupDTO> groupIndexationEngine(Client client,
    GroupService groupService,
    BulkProcessor bulkProcessor,
    ObjectMapper objectMapper,
    IndexationEngineConfiguration<GroupDTO> groupIndexationEngineConfiguration
  ) throws IOException {
    return new ElasticsearchIndexationEngineImpl<>(client, groupService, bulkProcessor, objectMapper,
      groupIndexationEngineConfiguration);
  }


  @Bean
  public ElasticsearchSearchEngineImpl<GroupDTO> groupSearchEngine(Client client, ObjectMapper objectMapper,
    SearchEngineConfiguration<GroupDTO> groupSearchEngineConfiguration,
    @Qualifier(PluginConstants.PLUGIN_SEARCH_ENGINE_AGGREGATION_CONVERTERS)
    PluginRegistry<AggregationConverter, SearchEngine> aggregationConverters) {
    return new ElasticsearchSearchEngineImpl<>(client, objectMapper, aggregationConverters,
      groupSearchEngineConfiguration);
  }


  @Bean
  @Qualifier("groupIndexationFullJob")
  public JobDetailFactoryBean groupIndexationFullJob() {
    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
    factoryBean.setJobClass(GroupIndexationJob.class);
    factoryBean.setGroup("Indexation");
    factoryBean.setName("Groups Indexation Job");
    factoryBean.setDescription("Groups full indexation Job");
    factoryBean.setDurability(true);
    return factoryBean;
  }

  @Bean
  @Qualifier("groupScheduledIndexationTrigger")
  public SimpleTriggerFactoryBean groupScheduledIndexationTrigger(
    @Qualifier("groupIndexationFullJob") JobDetail groupIndexationFullJob) {
    SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
    factoryBean.setName("Group re-indexation");
    factoryBean.setDescription("Periodic re-indexation of all groups of the application");
    factoryBean.setJobDetail(groupIndexationFullJob);
    factoryBean.setStartDelay((long) 30 * 1000);
    factoryBean.setRepeatInterval(1 * 60 * 60 * 1000);
    factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
    factoryBean.setMisfireInstruction(
      SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
    return factoryBean;
  }
}
