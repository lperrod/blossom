package com.blossomproject.ui.api.administration;

import com.blossomproject.core.common.utils.privilege.Privilege;
import com.blossomproject.ui.stereotype.BlossomApiController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@BlossomApiController
@RequestMapping("/administration/privileges")
@PreAuthorize("hasAuthority('administration:roles:read')")
public class PrivilegesApiController {

  private final PluginRegistry<Privilege, String> registry;

  public PrivilegesApiController(PluginRegistry<Privilege, String> registry) {
    this.registry = registry;
  }

  @GetMapping
  public ResponseEntity<List<Map<String, Object>>> privileges() {
    // Group privileges by namespace then feature
    Map<String, Map<String, List<Privilege>>> grouped = registry.getPlugins().stream()
      .collect(Collectors.groupingBy(
        Privilege::namespace,
        Collectors.groupingBy(Privilege::feature)
      ));

    List<Map<String, Object>> result = new ArrayList<>();
    grouped.forEach((namespace, features) -> {
      Map<String, Object> namespaceNode = new HashMap<>();
      namespaceNode.put("namespace", namespace);
      List<Map<String, Object>> featureList = new ArrayList<>();
      features.forEach((feature, privileges) -> {
        Map<String, Object> featureNode = new HashMap<>();
        featureNode.put("feature", feature);
        featureNode.put("privileges", privileges.stream().map(p -> {
          Map<String, String> privMap = new HashMap<>();
          privMap.put("privilege", p.privilege());
          privMap.put("right", p.right());
          return privMap;
        }).collect(Collectors.toList()));
        featureList.add(featureNode);
      });
      namespaceNode.put("features", featureList);
      result.add(namespaceNode);
    });

    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
