package com.blossomproject.autoconfigure.ui.api.system;

import com.blossomproject.autoconfigure.ui.api.ApiInterfaceAutoConfiguration;
import com.blossomproject.ui.api.system.BpmnApiController;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({CamundaBpmAutoConfiguration.class, ApiInterfaceAutoConfiguration.class})
@ConditionalOnClass({ProcessEngine.class, BpmnApiController.class})
@ConditionalOnBean(ProcessEngine.class)
public class ApiSystemBpmnAutoConfiguration {

  @Bean
  public BpmnApiController bpmnApiController(ProcessEngine processEngine) {
    return new BpmnApiController(processEngine);
  }
}
