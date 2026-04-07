package com.blossomproject.ui.supervision;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.StatusAggregator;
import org.springframework.boot.health.actuate.endpoint.SystemHealthDescriptor;
import org.springframework.boot.health.contributor.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blossom/public/status")
public class StatusController {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatusController.class);

  private final HealthEndpoint healthEndpoint;

  private final StatusAggregator healthAggregator;

  public StatusController(HealthEndpoint healthEndpoint, StatusAggregator healthAggregator) {
    this.healthEndpoint = healthEndpoint;
    this.healthAggregator = healthAggregator;
  }

  @GetMapping
  @ResponseBody
  public ResponseEntity<HealthDescriptor> status(
    @RequestParam(value = "exclude", required = false, defaultValue = "") Optional<List<String>> excludes,
    @RequestParam(value = "include", required = false, defaultValue = "") Optional<List<String>> includes) {
    HealthDescriptor health = filteredDetails((SystemHealthDescriptor) healthEndpoint.health(), excludes.orElse(Lists.newArrayList()));
    if (health.getStatus().equals(Status.UP)) {
      return ResponseEntity.ok(health);
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(health);
  }


  @VisibleForTesting
  HealthDescriptor filteredDetails(CompositeHealthDescriptor health, List<String> excludes) {
    Map<String, HealthDescriptor> filteredHealth = health
      .getComponents()
      .entrySet()
      .stream()
      .filter(mapEntry -> !excludes.contains(mapEntry.getKey()))
      .collect(Collectors.toMap(Map.Entry::getKey, mapEntry -> {
        if (mapEntry.getValue() instanceof CompositeHealthDescriptor composite) {
          return filteredDetails(composite, excludes);
        }
        return mapEntry.getValue();
      }));

    return health;
  }
}
