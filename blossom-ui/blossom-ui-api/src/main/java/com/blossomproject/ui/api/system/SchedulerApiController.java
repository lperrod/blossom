package com.blossomproject.ui.api.system;

import com.blossomproject.core.scheduler.job.JobInfo;
import com.blossomproject.core.scheduler.job.ScheduledJobService;
import com.blossomproject.core.scheduler.job.SchedulerInfo;
import com.blossomproject.ui.stereotype.BlossomApiController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.quartz.JobKey;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@BlossomApiController
@RequestMapping("/system/scheduler")
@PreAuthorize("hasAuthority('system:scheduler:manager')")
public class SchedulerApiController {

  private final ScheduledJobService scheduledJobService;

  public SchedulerApiController(ScheduledJobService scheduledJobService) {
    this.scheduledJobService = scheduledJobService;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> scheduler() {
    Map<String, Object> result = new HashMap<>();
    SchedulerInfo info = scheduledJobService.getSchedulerInfo();
    result.put("info", info);
    result.put("groups", scheduledJobService.getGroups());
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/{group}")
  public ResponseEntity<List<JobInfo>> tasks(@PathVariable String group) {
    return new ResponseEntity<>(scheduledJobService.getAll(group), HttpStatus.OK);
  }

  @GetMapping("/{group}/{name}")
  public ResponseEntity<JobInfo> task(@PathVariable String group, @PathVariable String name) {
    JobInfo jobInfo = scheduledJobService.getOne(new JobKey(name, group));
    if (jobInfo == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(jobInfo, HttpStatus.OK);
  }

  @PostMapping("/{group}/{name}/_execute")
  public ResponseEntity<Void> execute(@PathVariable String group, @PathVariable String name) {
    scheduledJobService.execute(new JobKey(name, group));
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/_changeState")
  public ResponseEntity<Void> changeState(@RequestParam("state") boolean state) {
    scheduledJobService.changeState(state);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
