package com.blossomproject.ui.api;

import com.blossomproject.ui.stereotype.BlossomApiController;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.IndicatedHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.SystemHealthDescriptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@BlossomApiController
@RequestMapping("/public/status")
public class StatusApiController {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatusApiController.class);

  private final HealthEndpoint healthEndpoint;

  public StatusApiController(HealthEndpoint healthEndpoint) {
    this.healthEndpoint = healthEndpoint;
  }

  @GetMapping
  @ResponseBody
  public ResponseEntity<Health> status(
    @RequestParam(value = "exclude", required = false, defaultValue = "") Optional<List<String>> excludes) {
    HealthDescriptor descriptor = healthEndpoint.health();
    Health health = toFilteredHealth(descriptor, excludes.orElse(Lists.newArrayList()));
    if (health.getStatus().equals(Status.UP)) {
      return ResponseEntity.ok(health);
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(health);
  }

  @VisibleForTesting
  Health toFilteredHealth(HealthDescriptor descriptor, List<String> excludes) {
    Health.Builder builder = new Health.Builder(descriptor.getStatus());

    if (descriptor instanceof CompositeHealthDescriptor) {
      CompositeHealthDescriptor composite = (CompositeHealthDescriptor) descriptor;
      composite
        .getComponents()
        .entrySet()
        .stream()
        .filter(e -> !excludes.contains(e.getKey()))
        .forEach(
          e -> builder.withDetail(e.getKey(), toFilteredHealth(e.getValue(), excludes))
        );
    } else if (descriptor instanceof IndicatedHealthDescriptor) {
      IndicatedHealthDescriptor indicated = (IndicatedHealthDescriptor) descriptor;
      indicated
        .getDetails()
        .entrySet()
        .stream()
        .filter(e -> !excludes.contains(e.getKey()))
        .forEach(
          e -> builder.withDetail(e.getKey(), e.getValue())
        );
    }

    return builder.build();
  }
}
