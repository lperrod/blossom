package com.blossomproject.autoconfigure.core.elasticsearch;

import com.blossomproject.autoconfigure.core.ElasticsearchAutoConfiguration;
import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.common.search.IndexationEngineConfiguration;
import com.blossomproject.core.common.search.IndexationEngineImpl;
import com.blossomproject.core.common.search.SearchEngine;
import com.blossomproject.core.common.search.SearchEngineConfiguration;
import com.blossomproject.core.common.search.SearchEngineImpl;
import com.blossomproject.core.common.search.SummaryDTO;
import com.blossomproject.core.common.search.SummaryDTO.SummaryDTOBuilder;
import com.blossomproject.core.common.search.facet.AggregationConverter;
import com.blossomproject.core.role.RoleDTO;
import com.blossomproject.core.role.RoleIndexationJob;
import com.blossomproject.core.role.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class RoleElasticsearchAutoConfiguration {

  @Bean
  public IndexationEngineConfiguration<RoleDTO> roleIndexationEngineConfiguration(
    @Value("classpath:/elasticsearch/roles.json") Resource resource) {
    return new IndexationEngineConfiguration<RoleDTO>() {
      @Override
      public Class<RoleDTO> getSupportedClass() {
        return RoleDTO.class;
      }

      @Override
      public Resource getSource() {
        return resource;
      }

      @Override
      public String getAlias() {
        return "roles";
      }

      @Override
      public Function<RoleDTO, String> getTypeFunction() {
        return u -> "role";
      }

      @Override
      public Function<RoleDTO, SummaryDTO> getSummaryFunction() {
        return u -> SummaryDTOBuilder.create().id(u.getId()).type(this.getTypeFunction().apply(u))
          .name(u.getName())
          .description(u.getDescription()).uri("/blossom/administration/roles/" + u.getId())
          .build();
      }
    };
  }

  @Bean
  public IndexationEngineImpl<RoleDTO> roleIndexationEngine(Client client,
    RoleService roleService,
    BulkProcessor bulkProcessor,
    ObjectMapper objectMapper,
    IndexationEngineConfiguration<RoleDTO> roleIndexationEngineConfiguration) {
    return new IndexationEngineImpl<>(client, roleService, bulkProcessor, objectMapper,
      roleIndexationEngineConfiguration);
  }


  @Bean
  public SearchEngineConfiguration<RoleDTO> roleSearchEngineConfiguration() {
    return new SearchEngineConfiguration<RoleDTO>() {
      @Override
      public String getName() {
        return "menu.administration.roles";
      }

      @Override
      public Class<RoleDTO> getSupportedClass() {
        return RoleDTO.class;
      }

      @Override
      public String[] getFields() {
        return new String[]{"dto.name", "dto.description"};
      }

      @Override
      public String getAlias() {
        return "roles";
      }
    };
  }

  @Bean
  public SearchEngineImpl<RoleDTO> roleSearchEngine(Client client, ObjectMapper objectMapper,
    SearchEngineConfiguration<RoleDTO> roleSearchEngineConfiguration,
    @Qualifier(PluginConstants.PLUGIN_SEARCH_ENGINE_AGGREGATION_CONVERTERS)
      PluginRegistry<AggregationConverter, SearchEngine> aggregationConverters) {
    return new SearchEngineImpl<>(client, objectMapper, aggregationConverters,
      roleSearchEngineConfiguration);
  }


  @Bean
  @Qualifier("roleIndexationFullJob")
  public JobDetailFactoryBean roleIndexationFullJob() {
    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
    factoryBean.setJobClass(RoleIndexationJob.class);
    factoryBean.setGroup("Indexation");
    factoryBean.setName("Roles Indexation Job");
    factoryBean.setDescription("Roles full indexation Job");
    factoryBean.setDurability(true);
    return factoryBean;
  }

  @Bean
  @Qualifier("roleScheduledIndexationTrigger")
  public SimpleTriggerFactoryBean roleScheduledIndexationTrigger(
    @Qualifier("roleIndexationFullJob") JobDetail roleIndexationFullJob) {
    SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
    factoryBean.setName("Role re-indexation");
    factoryBean.setDescription("Periodic re-indexation of all roles of the application");
    factoryBean.setJobDetail(roleIndexationFullJob);
    factoryBean.setStartDelay((long) 30 * 1000);
    factoryBean.setRepeatInterval(1 * 60 * 60 * 1000);
    factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
    factoryBean.setMisfireInstruction(
      SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
    return factoryBean;
  }
}
