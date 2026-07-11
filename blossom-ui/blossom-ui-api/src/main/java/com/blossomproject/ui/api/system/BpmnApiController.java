package com.blossomproject.ui.api.system;

import com.blossomproject.ui.stereotype.BlossomApiController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@BlossomApiController
@RequestMapping("/system/bpmn")
@PreAuthorize("hasAuthority('system:bpmn:manager')")
public class BpmnApiController {

  private final ProcessEngine processEngine;

  public BpmnApiController(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> bpmn() {
    Map<String, Object> result = new HashMap<>();

    List<ProcessDefinition> definitions = processEngine.getRepositoryService()
      .createProcessDefinitionQuery()
      .latestVersion()
      .list();

    List<Map<String, Object>> definitionList = definitions.stream().map(def -> {
      Map<String, Object> defMap = new HashMap<>();
      defMap.put("id", def.getId());
      defMap.put("key", def.getKey());
      defMap.put("name", def.getName());
      defMap.put("version", def.getVersion());
      defMap.put("deploymentId", def.getDeploymentId());
      defMap.put("resourceName", def.getResourceName());

      long instanceCount = processEngine.getRuntimeService()
        .createProcessInstanceQuery()
        .processDefinitionId(def.getId())
        .count();
      defMap.put("runningInstances", instanceCount);

      return defMap;
    }).collect(Collectors.toList());

    result.put("definitions", definitionList);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
