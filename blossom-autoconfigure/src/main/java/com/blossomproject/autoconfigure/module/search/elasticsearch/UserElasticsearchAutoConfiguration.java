package com.blossomproject.autoconfigure.module.search.elasticsearch;

import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.user.UserDTO;
import com.blossomproject.core.user.UserService;
import com.blossomproject.module.search.common.IndexationEngineConfiguration;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.module.search.common.SearchEngineConfiguration;
import com.blossomproject.module.search.common.SummaryDTO;
import com.blossomproject.module.search.common.SummaryDTO.SummaryDTOBuilder;
import com.blossomproject.module.search.common.facet.AggregationConverter;
import com.blossomproject.module.search.common.facet.AggregationConverterTermImpl;
import com.blossomproject.module.search.elasticsearch.ElasticsearchIndexationEngineImpl;
import com.blossomproject.module.search.elasticsearch.ElasticsearchSearchEngineImpl;
import com.blossomproject.module.search.elasticsearch.core.UserIndexationJob;
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
public class UserElasticsearchAutoConfiguration {

  @Bean
  public IndexationEngineConfiguration<UserDTO> userIndexationEngineConfiguration(
    @Value("classpath:/elasticsearch/users.json") Resource resource) {
    return new IndexationEngineConfiguration<UserDTO>() {
      @Override
      public Class<UserDTO> getSupportedClass() {
        return UserDTO.class;
      }

      @Override
      public Resource getSource() {
        return resource;
      }

      @Override
      public String getAlias() {
        return "users";
      }

      @Override
      public Function<UserDTO, String> getTypeFunction() {
        return u -> "user";
      }

      @Override
      public Function<UserDTO, SummaryDTO> getSummaryFunction() {
        return u -> SummaryDTOBuilder.create().id(u.getId()).type(this.getTypeFunction().apply(u))
          .name(u.getFirstname() + " " + u.getLastname()).description(u.getDescription())
          .uri("/blossom/administration/users/" + u.getId()).build();
      }
    };
  }

  @Bean
  public ElasticsearchIndexationEngineImpl<UserDTO> userIndexationEngine(Client client,
    UserService userService,
    BulkProcessor bulkProcessor,
    ObjectMapper objectMapper,
    IndexationEngineConfiguration<UserDTO> userIndexationEngineConfiguration) {
    return new ElasticsearchIndexationEngineImpl<>(client, userService, bulkProcessor, objectMapper,
      userIndexationEngineConfiguration);
  }


  @Bean
  public ElasticsearchSearchEngineImpl<UserDTO> userSearchEngine(Client client, ObjectMapper objectMapper,
    SearchEngineConfiguration<UserDTO> userSearchEngineConfiguration,
    @Qualifier(PluginConstants.PLUGIN_SEARCH_ENGINE_AGGREGATION_CONVERTERS)
    PluginRegistry<com.blossomproject.module.search.elasticsearch.AggregationConverter, SearchEngine> aggregationConverters) {
    return new ElasticsearchSearchEngineImpl<>(client, objectMapper, aggregationConverters,
      userSearchEngineConfiguration);
  }

  @Bean
  @Qualifier("userIndexationFullJob")
  public JobDetailFactoryBean userIndexationFullJob() {
    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
    factoryBean.setJobClass(UserIndexationJob.class);
    factoryBean.setGroup("Indexation");
    factoryBean.setName("Users Indexation Job");
    factoryBean.setDescription("Users full indexation Job");
    factoryBean.setDurability(true);
    return factoryBean;
  }

  @Bean
  @Qualifier("userScheduledIndexationTrigger")
  public SimpleTriggerFactoryBean userScheduledIndexationTrigger(
    @Qualifier("userIndexationFullJob") JobDetail userIndexationFullJob) {
    SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
    factoryBean.setName("User re-indexation");
    factoryBean.setDescription("Periodic re-indexation of all users of the application");
    factoryBean.setJobDetail(userIndexationFullJob);
    factoryBean.setStartDelay((long) 30 * 1000);
    factoryBean.setRepeatInterval(1 * 60 * 60 * 1000);
    factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
    factoryBean.setMisfireInstruction(
      SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
    return factoryBean;
  }

  @Bean
  public AggregationConverter userCompanyAggregationConverter(
    SearchEngineConfiguration<UserDTO> userSearchEngineConfiguration) {
    return new AggregationConverterTermImpl("users.search.facet.company", userSearchEngineConfiguration.getName(),
      "dto.company.raw");
  }

  @Bean
  public AggregationConverter userFunctionAggregationConverter(
    SearchEngineConfiguration<UserDTO> userSearchEngineConfiguration) {
    return new AggregationConverterTermImpl("users.search.facet.function", userSearchEngineConfiguration.getName(),
      "dto.function.raw");
  }
}
