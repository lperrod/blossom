package com.blossomproject.ui.api.system;

import com.blossomproject.ui.stereotype.BlossomApiController;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@BlossomApiController
@RequestMapping("/system/liquibase")
@PreAuthorize("hasAuthority('system:liquibase:manager')")
public class LiquibaseApiController {

  private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseApiController.class);

  private final Map<String, SpringLiquibase> liquibaseBeans;

  public LiquibaseApiController(Map<String, SpringLiquibase> liquibaseBeans) {
    this.liquibaseBeans = liquibaseBeans;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> liquibase() {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, SpringLiquibase> entry : liquibaseBeans.entrySet()) {
      try {
        result.put(entry.getKey(), getChangeSets(entry.getValue()));
      } catch (Exception e) {
        LOGGER.warn("Failed to get Liquibase report for bean '{}'", entry.getKey(), e);
      }
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  private List<Map<String, Object>> getChangeSets(SpringLiquibase liquibase) throws Exception {
    try (Connection connection = liquibase.getDataSource().getConnection()) {
      DatabaseFactory factory = DatabaseFactory.getInstance();
      Database database = factory.findCorrectDatabaseImplementation(new JdbcConnection(connection));
      try {
        return database.getRanChangeSetList().stream()
          .map(this::toMap)
          .collect(Collectors.toList());
      } finally {
        database.close();
      }
    }
  }

  private Map<String, Object> toMap(RanChangeSet changeSet) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", changeSet.getId());
    map.put("author", changeSet.getAuthor());
    map.put("changeLog", changeSet.getChangeLog());
    map.put("comments", changeSet.getComments());
    map.put("dateExecuted", changeSet.getDateExecuted() != null ? changeSet.getDateExecuted().toInstant().toString() : null);
    map.put("deploymentId", changeSet.getDeploymentId());
    map.put("description", changeSet.getDescription());
    map.put("execType", changeSet.getExecType() != null ? changeSet.getExecType().name() : null);
    map.put("orderExecuted", changeSet.getOrderExecuted());
    map.put("tag", changeSet.getTag());
    return map;
  }
}
