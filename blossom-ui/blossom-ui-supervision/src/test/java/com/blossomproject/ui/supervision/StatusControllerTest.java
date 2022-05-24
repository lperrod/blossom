package com.blossomproject.ui.supervision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.SimpleStatusAggregator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.StatusAggregator;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class StatusControllerTest {


  HealthEndpoint healthEndpoint;

  private StatusController controller;

  private StatusAggregator statusAggregator;

  @Before
  public void setUp() {
    statusAggregator = new SimpleStatusAggregator();
    healthEndpoint = Mockito.mock(HealthEndpoint.class);
    controller = new StatusController(healthEndpoint, statusAggregator);
    controller = Mockito.spy(controller);

  }

  @Test
  public void should_display_all_status_with_health_up() throws Exception {
    Map<String, String> map = new HashMap<>();
    map.put("test", "testMessage");
    Health.Builder builder = new Health.Builder(Status.UP, map);
    Health health = builder.build();

    doReturn(health).when(controller).filteredDetails(any(), any(List.class));

    ResponseEntity<Health> response = controller.status(Optional.empty(), Optional.empty());

    assertNotNull(response);
    assertTrue(response.getStatusCode() == HttpStatus.OK);
    assertEquals(health, response.getBody());
  }

  @Test
  public void should_display_all_status_with_health_down() throws Exception {
    Map<String, String> map = new HashMap<>();
    map.put("test", "testMessage");
    Health.Builder builder = new Health.Builder(Status.DOWN, map);
    Health health = builder.build();

    doReturn(health).when(controller).filteredDetails(any(), any(List.class));

    ResponseEntity<Health> response = controller.status(Optional.empty(), Optional.empty());

    assertNotNull(response);
    assertTrue(response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR);
    assertEquals(health, response.getBody());
  }

  @Test
  public void should_display_status_up_with_excludes() throws Exception {
    Map<String, String> map = new HashMap<>();
    map.put("test", "testMessage");
    Health.Builder builder = new Health.Builder(Status.UP, map);
    Health health = builder.build();

    doReturn(health).when(controller).filteredDetails(any(), any(List.class));

    ResponseEntity<Health> response = controller.status(Optional.of(Lists.newArrayList("test1", "test2")), Optional.empty());

    assertNotNull(response);
    assertTrue(response.getStatusCode() == HttpStatus.OK);
    assertEquals(health, response.getBody());
  }

  @Test
  public void should_display_status_down_without_excludes() throws Exception {
    Map<String, String> map = new HashMap<>();
    map.put("test", "testMessage");
    Health.Builder builder = new Health.Builder(Status.DOWN, map);
    Health health = builder.build();

    Map<String, HealthComponent> components = new HashMap<>();
    components.put("test", health);

    SystemHealth systemHealth = PowerMockito.mock(SystemHealth.class);

    doReturn(systemHealth).when(healthEndpoint).health();
    doReturn(components).when(systemHealth).getComponents();

    doReturn(health).when(controller).filteredHealthDetails(any(), any(List.class));
 
    ResponseEntity<Health> response = controller.status(Optional.of(Lists.newArrayList("test1", "test2")), Optional.empty());

    assertNotNull(response);
    assertTrue(response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR);
    assertEquals(systemHealth.getComponents().get("test"), response.getBody().getDetails().get("test"));
  }

  @Test
  public void should_filter_details_without_excludes() throws Exception {
    Health.Builder builder = new Health.Builder(Status.DOWN);
    Health healthChild = builder.build();
    Health.Builder builder2 = new Health.Builder(Status.DOWN);
    builder2.withDetail("healthChild", healthChild);
    Health health = builder2.build();

    Health healthResponse = controller.filteredHealthDetails(health, Lists.newArrayList());
    assertNotNull(healthResponse);
    assertEquals(health, healthResponse);

  }

  @Test
  public void should_filter_details_with_excludes() throws Exception {
    Health.Builder builder = new Health.Builder(Status.DOWN);
    Health healthChild = builder.build();
    Health.Builder builder2 = new Health.Builder(Status.DOWN);
    builder2.withDetail("healthChild", healthChild);
    Health health = builder2.build();

    Health healthResponse = controller.filteredHealthDetails(health, Lists.newArrayList("healthChild"));
    assertNotNull(healthResponse);
    assertTrue(healthResponse.getDetails().isEmpty());

  }

  private Health buildTestHealth() {

    Health healthLeafDown = Health.down().build();
    Health healthLeafUp = Health.up().build();

    Map<String, Health> downSubRootChildren = new HashMap<>();
    downSubRootChildren.put("healthLeafDown", healthLeafDown);
    downSubRootChildren.put("healthLeafUp", healthLeafUp);

    Status downRootStatus = statusAggregator.getAggregateStatus(
      downSubRootChildren.values().stream().map(Health::getStatus).collect(Collectors.toSet()));

    Health downSubRoot = Health.status(downRootStatus).withDetails(downSubRootChildren).build();

    Map<String, Health> upSubRootChildren = new HashMap<>();
    upSubRootChildren.put("healthLeafUp", healthLeafUp);
    upSubRootChildren.put("healthLeafStillUp", healthLeafUp);

    Status upSubRootStatus = statusAggregator.getAggregateStatus(
      upSubRootChildren.values().stream().map(Health::getStatus).collect(Collectors.toSet()));

    Health upSubRoot = Health.status(upSubRootStatus).withDetails(upSubRootChildren).build();

    Map<String, Health> level2SubRootMap = new HashMap<>();
    level2SubRootMap.put("downSubRoot", downSubRoot);
    level2SubRootMap.put("upSubRoot", upSubRoot);

    Status level2SubRootStatus = statusAggregator.getAggregateStatus(
      level2SubRootMap.values().stream().map(Health::getStatus).collect(Collectors.toSet()));

    Health level2SubRoot = Health.status(level2SubRootStatus).withDetails(level2SubRootMap).build();

    Map<String, Health> returnMap = new HashMap<>();
    returnMap.put("level2SubRoot", level2SubRoot);
    returnMap.put("upSubRoot", upSubRoot);

    Status globalStatus = statusAggregator.getAggregateStatus(
      returnMap.values().stream().map(Health::getStatus).collect(Collectors.toSet()));

    return Health.status(globalStatus).withDetails(returnMap).build();
  }

  private Health buildFilteredTestHealth() {
    Health healthLeafUp = Health.up().build();

    Map<String, Health> downSubRootChildren = new HashMap<>();
    ;
    downSubRootChildren.put("healthLeafUp", healthLeafUp);

    Status downRootStatus = statusAggregator.getAggregateStatus(
      downSubRootChildren.values().stream().map(Health::getStatus).collect(Collectors.toSet()));

    Health downSubRoot = Health.status(downRootStatus).withDetails(downSubRootChildren).build();

    Map<String, Health> upSubRootChildren = new HashMap<>();
    upSubRootChildren.put("healthLeafUp", healthLeafUp);
    upSubRootChildren.put("healthLeafStillUp", healthLeafUp);

    Status upSubRootStatus = statusAggregator.getAggregateStatus(
      upSubRootChildren.values().stream().map(Health::getStatus).collect(Collectors.toSet()));

    Health upSubRoot = Health.status(upSubRootStatus).withDetails(upSubRootChildren).build();

    Map<String, Health> level2SubRootMap = new HashMap<>();
    level2SubRootMap.put("downSubRoot", downSubRoot);
    level2SubRootMap.put("upSubRoot", upSubRoot);

    Status level2SubRootStatus = statusAggregator.getAggregateStatus(
      level2SubRootMap.values().stream().map(Health::getStatus).collect(Collectors.toSet()));

    Health level2SubRoot = Health.status(level2SubRootStatus).withDetails(level2SubRootMap).build();

    Map<String, Health> returnMap = new HashMap<>();
    returnMap.put("level2SubRoot", level2SubRoot);
    returnMap.put("upSubRoot", upSubRoot);

    Status globalStatus = statusAggregator.getAggregateStatus(
      returnMap.values().stream().map(Health::getStatus).collect(Collectors.toSet()));

    return Health.status(globalStatus).withDetails(returnMap).build();
  }


  @Test
  public void should_display_status_down_without_includes() {
    Health health = buildTestHealth();
    SystemHealth systemHealth = PowerMockito.mock(SystemHealth.class);
    doReturn(systemHealth).when(healthEndpoint).health();
    doReturn(health).when(controller).filteredDetails(any(), any());
    ResponseEntity<Health> response = controller.status(Optional.empty(), Optional.empty());

    assertNotNull(response);
    assertSame(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    assertEquals(health, response.getBody());
  }

  @Test
  public void should_display_status_up_with_includes() {
    Health health = buildTestHealth();
    SystemHealth systemHealth = PowerMockito.mock(SystemHealth.class);
    doReturn(systemHealth).when(healthEndpoint).health();
    doReturn(health).when(controller).filteredDetails(any(), any());
    ResponseEntity<Health> response = controller.status(Optional.empty(), Optional.of(Lists.newArrayList("upSubRoot")));

    assertNotNull(response);
    assertSame(response.getStatusCode(), HttpStatus.OK);
    assertTrue(response.getBody().getDetails().keySet().contains("upSubRoot"));
    assertFalse(response.getBody().getDetails().keySet().contains("level2SubRoot"));
  }

  @Test
  public void should_display_status_up_with_includes_and_excludes() {
    Health health = buildTestHealth();
    SystemHealth systemHealth = PowerMockito.mock(SystemHealth.class);
    doReturn(systemHealth).when(healthEndpoint).health();
    doReturn(buildFilteredTestHealth()).when(controller).filteredDetails(any(), any());
    ResponseEntity<Health> response = controller.status(Optional.of(Lists.newArrayList("healthLeafDown")),
      Optional.of(Lists.newArrayList("level2SubRoot.downSubRoot")));

    assertNotNull(response);
    assertSame(response.getStatusCode(), HttpStatus.OK);
    assertFalse(response.getBody().getDetails().keySet().contains("upSubRoot"));
    assertTrue(response.getBody().getDetails().keySet().contains("level2SubRoot"));
    assertFalse(((Health) response.getBody().getDetails().get("level2SubRoot")).getDetails().keySet().contains("upSubRoot"));
  }

  @Test
  public void should_display_status_up_with_includes_leaf() {
    Health health = buildTestHealth();
    SystemHealth systemHealth = PowerMockito.mock(SystemHealth.class);
    doReturn(systemHealth).when(healthEndpoint).health();
    doReturn(health).when(controller).filteredDetails(any(), any());
    ResponseEntity<Health> response = controller.status(Optional.empty(),
      Optional.of(Lists.newArrayList("level2SubRoot.downSubRoot.healthLeafUp")));

    assertNotNull(response);
    assertSame(response.getStatusCode(), HttpStatus.OK);
    assertFalse(response.getBody().getDetails().keySet().contains("upSubRoot"));
    assertTrue(response.getBody().getDetails().keySet().contains("level2SubRoot"));
    assertTrue(((Health) response.getBody().getDetails().get("level2SubRoot")).getDetails().keySet().contains("downSubRoot"));
  }

  @Test
  public void should_display_root_if_leaf_health() {
    Health health = Health.up().build();
    SystemHealth systemHealth = PowerMockito.mock(SystemHealth.class);
    doReturn(systemHealth).when(healthEndpoint).health();
    doReturn(health).when(controller).filteredDetails(any(), any());

    ResponseEntity<Health> response = controller.status(Optional.empty(), Optional.of(Lists.newArrayList("")));

    assertNotNull(response);
    assertSame(response.getStatusCode(), HttpStatus.OK);
    assertTrue(response.getBody().getDetails().isEmpty());

  }


}
