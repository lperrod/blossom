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
import com.blossomproject.core.common.search.facet.AggregationConverterTermImpl;
import com.blossomproject.core.user.UserDTO;
import com.blossomproject.core.user.UserIndexationJob;
import com.blossomproject.core.user.UserService;
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
  public IndexationEngineImpl<UserDTO> userIndexationEngine(Client client,
    UserService userService,
    BulkProcessor bulkProcessor,
    ObjectMapper objectMapper,
    IndexationEngineConfiguration<UserDTO> userIndexationEngineConfiguration) {
    return new IndexationEngineImpl<>(client, userService, bulkProcessor, objectMapper,
      userIndexationEngineConfiguration);
  }


  @Bean
  public SearchEngineConfiguration<UserDTO> userSearchEngineConfiguration() {
    return new SearchEngineConfiguration<UserDTO>() {
      @Override
      public String getName() {
        return "menu.administration.users";
      }

      @Override
      public Class<UserDTO> getSupportedClass() {
        return UserDTO.class;
      }

      @Override
      public String[] getFields() {
        return new String[]{"dto.identifier", "dto.email", "dto.firstname", "dto.lastname",
          "dto.company", "dto.description", "dto.function"};
      }

      @Override
      public String getAlias() {
        return "users";
      }
    };
  }

  @Bean
  public SearchEngineImpl<UserDTO> userSearchEngine(Client client, ObjectMapper objectMapper,
    SearchEngineConfiguration<UserDTO> userSearchEngineConfiguration,
    @Qualifier(PluginConstants.PLUGIN_SEARCH_ENGINE_AGGREGATION_CONVERTERS)
      PluginRegistry<AggregationConverter, SearchEngine> aggregationConverters) {
    return new SearchEngineImpl<>(client, objectMapper, aggregationConverters,
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
