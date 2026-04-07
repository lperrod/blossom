package com.blossomproject.ui.web.system.liquibase;

import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom replacement for the removed Spring Boot Actuator LiquibaseEndpoint.
 * Provides Liquibase changeset information using SpringLiquibase beans directly.
 */
public class BlossomLiquibaseEndpoint {

  private static final Logger LOGGER = LoggerFactory.getLogger(BlossomLiquibaseEndpoint.class);

  private final Map<String, SpringLiquibase> liquibaseBeans;

  public BlossomLiquibaseEndpoint(Map<String, SpringLiquibase> liquibaseBeans) {
    this.liquibaseBeans = liquibaseBeans;
  }

  public Map<String, LiquibaseBeanDescriptor> getLiquibaseBeans() {
    Map<String, LiquibaseBeanDescriptor> result = new HashMap<>();
    for (Map.Entry<String, SpringLiquibase> entry : liquibaseBeans.entrySet()) {
      try {
        result.put(entry.getKey(), createReport(entry.getValue()));
      } catch (Exception e) {
        LOGGER.warn("Failed to get Liquibase report for bean '{}'", entry.getKey(), e);
      }
    }
    return result;
  }

  private LiquibaseBeanDescriptor createReport(SpringLiquibase liquibase) throws Exception {
    Connection connection = liquibase.getDataSource().getConnection();
    try {
      DatabaseFactory factory = DatabaseFactory.getInstance();
      Database database = factory.findCorrectDatabaseImplementation(new JdbcConnection(connection));
      try {
        List<ChangeSetDescriptor> changeSets = database.getRanChangeSetList()
          .stream()
          .map(ChangeSetDescriptor::new)
          .collect(Collectors.toList());
        return new LiquibaseBeanDescriptor(changeSets);
      } finally {
        database.close();
      }
    } catch (Exception e) {
      connection.close();
      throw e;
    }
  }

  public static class LiquibaseBeanDescriptor {

    private final List<ChangeSetDescriptor> changeSets;

    public LiquibaseBeanDescriptor(List<ChangeSetDescriptor> changeSets) {
      this.changeSets = changeSets;
    }

    public List<ChangeSetDescriptor> getChangeSets() {
      return changeSets;
    }
  }

  public static class ChangeSetDescriptor {

    private final String author;
    private final String changeLog;
    private final String comments;
    private final Set<String> contexts;
    private final Instant dateExecuted;
    private final String deploymentId;
    private final String description;
    private final ExecType execType;
    private final String id;
    private final Set<String> labels;
    private final String checksum;
    private final Integer orderExecuted;
    private final String tag;

    public ChangeSetDescriptor(RanChangeSet ranChangeSet) {
      this.author = ranChangeSet.getAuthor();
      this.changeLog = ranChangeSet.getChangeLog();
      this.comments = ranChangeSet.getComments();
      this.contexts = ranChangeSet.getContextExpression() != null
        ? ranChangeSet.getContextExpression().getContexts()
        : null;
      this.dateExecuted = ranChangeSet.getDateExecuted() != null
        ? ranChangeSet.getDateExecuted().toInstant()
        : null;
      this.deploymentId = ranChangeSet.getDeploymentId();
      this.description = ranChangeSet.getDescription();
      this.execType = ranChangeSet.getExecType();
      this.id = ranChangeSet.getId();
      this.labels = ranChangeSet.getLabels() != null
        ? ranChangeSet.getLabels().getLabels()
        : null;
      this.checksum = ranChangeSet.getLastCheckSum() != null
        ? ranChangeSet.getLastCheckSum().toString()
        : null;
      this.orderExecuted = ranChangeSet.getOrderExecuted();
      this.tag = ranChangeSet.getTag();
    }

    public String getAuthor() {
      return author;
    }

    public String getChangeLog() {
      return changeLog;
    }

    public String getComments() {
      return comments;
    }

    public Set<String> getContexts() {
      return contexts;
    }

    public Instant getDateExecuted() {
      return dateExecuted;
    }

    public String getDeploymentId() {
      return deploymentId;
    }

    public String getDescription() {
      return description;
    }

    public ExecType getExecType() {
      return execType;
    }

    public String getId() {
      return id;
    }

    public Set<String> getLabels() {
      return labels;
    }

    public String getChecksum() {
      return checksum;
    }

    public Integer getOrderExecuted() {
      return orderExecuted;
    }

    public String getTag() {
      return tag;
    }
  }
}
