package com.blossomproject.ui.api.system;

import com.blossomproject.core.common.utils.tree.TreeNode;
import com.blossomproject.ui.stereotype.BlossomApiController;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint.LoggerLevelsDescriptor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@BlossomApiController
@RequestMapping("/system/loggers")
@PreAuthorize("hasAuthority('system:loggers:manager')")
public class LoggersApiController {

  private static final String ROOT_LOGGER = "ROOT";

  private final LoggersEndpoint loggersEndpoint;

  public LoggersApiController(LoggersEndpoint loggersEndpoint) {
    this.loggersEndpoint = loggersEndpoint;
  }

  @GetMapping
  public ResponseEntity<LoggersEndpoint.LoggersDescriptor> list() {
    return new ResponseEntity<>(loggersEndpoint.loggers(), HttpStatus.OK);
  }

  @GetMapping("/{name:.+}")
  public ResponseEntity<LoggerLevelsDescriptor> get(@PathVariable String name) {
    LoggerLevelsDescriptor levels = loggersEndpoint.loggerLevels(name);
    if (levels == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(levels, HttpStatus.OK);
  }

  @PostMapping("/{name}/{logLevel}")
  public ResponseEntity<Void> setLevel(@PathVariable String name, @PathVariable LogLevel logLevel) {
    loggersEndpoint.configureLogLevel(name, logLevel);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("/tree")
  @SuppressWarnings("unchecked")
  public ResponseEntity<TreeNode<String>> tree(@RequestParam(name = "q", defaultValue = "") String q) {
    LoggerLevelsDescriptor rootLevels = loggersEndpoint.loggerLevels(ROOT_LOGGER);
    TreeNode<String> rootNode = new TreeNode<>(ROOT_LOGGER, ROOT_LOGGER,
      rootLevels != null ? rootLevels.getConfiguredLevel() : null);

    Map<String, LoggerLevelsDescriptor> loggerLevels =
      (Map<String, LoggerLevelsDescriptor>) loggersEndpoint.loggers().getLoggers().get("loggers");

    if (loggerLevels != null) {
      loggerLevels.entrySet().stream()
        .filter(e -> !e.getKey().equals(ROOT_LOGGER))
        .filter(e -> StringUtils.isEmpty(q) || e.getKey().contains(q))
        .forEach(e -> {
          TreeNode<String> treeNode = rootNode;
          String[] keyParts = e.getKey().split("\\.");
          String currentKey = null;
          for (String keyPart : keyParts) {
            currentKey = currentKey != null ? currentKey + "." + keyPart : keyPart;
            Optional<TreeNode<String>> child = treeNode.findChildWithId(currentKey);
            if (child.isPresent()) {
              treeNode = child.get();
            } else {
              TreeNode<String> newNode = new TreeNode<>(currentKey, keyPart,
                e.getValue().getConfiguredLevel());
              treeNode.addChild(newNode);
              treeNode = newNode;
            }
          }
        });
    }
    return new ResponseEntity<>(rootNode, HttpStatus.OK);
  }
}
