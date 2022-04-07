package com.blossomproject.ui.supervision;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
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


  private final HealthEndpoint healthEndpoint;


  public StatusController(HealthEndpoint healthEndpoint) {
    this.healthEndpoint = healthEndpoint;
  }

  @GetMapping
  @ResponseBody
  public ResponseEntity<Health> status(
    @RequestParam(value = "exclude", required = false, defaultValue = "") Optional<List<String>> excludes,
    @RequestParam(value = "include", required = false, defaultValue = "") Optional<List<String>> includes) {
    Health health = filteredDetails((Health) healthEndpoint.health(), excludes.orElse(Lists.newArrayList()));
    if (includes.isPresent() && !includes.get().isEmpty()) {
      health = includedDetails(health, includes
          .get()
          .stream()
          .map(String::toLowerCase)
          .map(s -> toString().isEmpty() ? "." : "." + s.toLowerCase())
          .collect(Collectors.toList()),
        "");
    }
    if (health.getStatus().equals(Status.UP)) {
      return ResponseEntity.ok(health);
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(health);
  }


  @VisibleForTesting
  Health filteredDetails(Health health, List<String> excludes) {
    Map<String, Health> filteredHealth = health
      .getDetails()
      .entrySet()
      .stream()
      .filter(mapEntry -> mapEntry.getValue() instanceof Health && !excludes.contains(mapEntry.getKey()))
      .collect(Collectors.toMap(Map.Entry::getKey, e -> filteredDetails((Health) e.getValue(), excludes)));

    if (filteredHealth.isEmpty()) {
      return Health.status(health.getStatus()).build();
    }

    return
      filteredHealth.entrySet().stream().anyMatch(entry -> entry.getValue().getStatus().equals(Health.down().build().getStatus()))
        ? Health.down().build()
        : Health.up().build();
  }

  @VisibleForTesting
  Health includedDetails(Health health, List<String> includes, String currentDepth) {

    if (health.getDetails().isEmpty()) {
      return health;
    }

    Map<String, Health> filteredHealth = health
      .getDetails()
      .entrySet()
      .stream()
      .filter(mapEntry -> mapEntry.getValue() instanceof Health && includes.stream()
        .anyMatch(pattern -> pattern.startsWith(currentDepth + "." + mapEntry.getKey().toLowerCase())))
      .map(mapEntry -> {
        if (includes.stream().anyMatch(pattern -> pattern.equals(currentDepth + "." + mapEntry.getKey().toLowerCase()))) {
          return new AbstractMap.SimpleEntry<>(mapEntry.getKey(), (Health) mapEntry.getValue());
        } else {
          return new AbstractMap.SimpleEntry<>(mapEntry.getKey(),
            includedDetails((Health) mapEntry.getValue(), includes, currentDepth + "." + mapEntry.getKey().toLowerCase()));
        }
      })
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return
      filteredHealth.entrySet().stream().anyMatch(entry -> entry.getValue().getStatus().equals(Health.down().build().getStatus()))
        ? Health.down().build()
        : Health.up().build();
  }
}
