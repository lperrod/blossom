package com.blossomproject.autoconfigure.ui.api.system;

import com.blossomproject.autoconfigure.core.SchedulerAutoConfiguration;
import com.blossomproject.autoconfigure.ui.api.ApiInterfaceAutoConfiguration;
import com.blossomproject.core.scheduler.job.ScheduledJobService;
import com.blossomproject.ui.api.system.SchedulerApiController;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({SchedulerAutoConfiguration.class, ApiInterfaceAutoConfiguration.class})
@ConditionalOnClass(SchedulerApiController.class)
@ConditionalOnBean(ScheduledJobService.class)
public class ApiSystemSchedulerAutoConfiguration {

  @Bean
  public SchedulerApiController schedulerApiController(ScheduledJobService scheduledJobService) {
    return new SchedulerApiController(scheduledJobService);
  }
}
