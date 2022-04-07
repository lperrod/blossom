package com.blossomproject.core.scheduler.supervision;

import com.blossomproject.core.scheduler.job.JobInfo;
import com.blossomproject.core.scheduler.job.ScheduledJobService;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class JobExecutionHealthIndicator implements HealthIndicator {


  private final ScheduledJobService jobService;


  public JobExecutionHealthIndicator(ScheduledJobService jobService) {
    this.jobService = jobService;
  }

  @Override
  public Health health() {

    if (!jobService.getSchedulerInfo().isStarted() || jobService.getSchedulerInfo().isStandBy()) {
      return Health.up().build();
    }

    Map<String, Health> groupHealthMap = new HashMap<>();

    for (String group : jobService.getGroups()) {
      groupHealthMap.put(group.replaceAll(" ", ""), healthForGroup(group));
    }

    return
      groupHealthMap.entrySet().stream().anyMatch(entry -> entry.getValue().getStatus().equals(Health.down().build().getStatus()))
        ? Health.down().build()
        : Health.up().build();
  }

  private Health healthForGroup(String group) {
    Map<String, Health> taskHealthMap = new HashMap<>();

    for (JobInfo jobInfo : jobService.getAll(group)) {
      taskHealthMap.put(jobInfo.getKey().getName().replaceAll(" ", ""), healthForTask(jobInfo));
    }

    return
      taskHealthMap.entrySet().stream().anyMatch(entry -> entry.getValue().getStatus().equals(Health.down().build().getStatus()))
        ? Health.down().build()
        : Health.up().build();
  }

  private Health healthForTask(JobInfo jobInfo) {
    if (jobInfo.isActive() && !jobInfo.isExecuting() && jobInfo.getNextFireTime() != null
      && jobInfo.getNextFireTime().toInstant().isBefore(Instant.now())) {
      return Health.down().build();
    } else {
      return Health.up().build();
    }
  }
}
