package com.blossomproject.ui.api.system;

import com.blossomproject.ui.stereotype.BlossomApiController;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@BlossomApiController
@RequestMapping("/system/dashboard")
@PreAuthorize("hasAuthority('system:dashboard:manager')")
public class DashboardApiController {

  private final HealthEndpoint healthEndpoint;
  private final MetricsEndpoint metricsEndpoint;

  public DashboardApiController(HealthEndpoint healthEndpoint, MetricsEndpoint metricsEndpoint) {
    this.healthEndpoint = healthEndpoint;
    this.metricsEndpoint = metricsEndpoint;
  }

  @GetMapping("/status")
  public ResponseEntity<Map<String, Object>> status() {
    Map<String, Object> result = new HashMap<>();
    HealthDescriptor health = healthEndpoint.health();
    result.put("health", health);
    result.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/memory")
  public ResponseEntity<Map<String, Object>> memory() {
    Map<String, Object> result = new HashMap<>();
    result.put("heap_used", metric("jvm.memory.used", List.of("area:heap")));
    result.put("heap_max", metric("jvm.memory.max", List.of("area:heap")));
    result.put("heap_committed", metric("jvm.memory.committed", List.of("area:heap")));
    result.put("nonheap_used", metric("jvm.memory.used", List.of("area:nonheap")));
    result.put("nonheap_committed", metric("jvm.memory.committed", List.of("area:nonheap")));
    result.put("total_max", Runtime.getRuntime().maxMemory());
    result.put("total_used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/jvm")
  public ResponseEntity<Map<String, Object>> jvm() {
    Map<String, Object> result = new HashMap<>();
    result.put("classes_loaded", metric("jvm.classes.loaded"));
    result.put("classes_unloaded", metric("jvm.classes.unloaded"));
    result.put("threads_live", metric("jvm.threads.live"));
    result.put("threads_daemon", metric("jvm.threads.daemon"));
    result.put("threads_peak", metric("jvm.threads.peak"));
    result.put("gc_pause_count", metric("jvm.gc.pause"));
    result.put("processors", Runtime.getRuntime().availableProcessors());
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/charts")
  public ResponseEntity<Map<String, Object>> charts() {
    Map<String, Object> result = new HashMap<>();
    result.put("heap_used", metric("jvm.memory.used", List.of("area:heap")));
    result.put("heap_max", metric("jvm.memory.max", List.of("area:heap")));
    result.put("threads_live", metric("jvm.threads.live"));
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  private Double metric(String name) {
    return metric(name, null);
  }

  private Double metric(String name, List<String> tags) {
    try {
      MetricsEndpoint.MetricDescriptor descriptor = metricsEndpoint.metric(name, tags);
      if (descriptor != null && descriptor.getMeasurements() != null && !descriptor.getMeasurements().isEmpty()) {
        return descriptor.getMeasurements().get(0).getValue();
      }
    } catch (Exception e) {
      // metric not available
    }
    return 0d;
  }
}
