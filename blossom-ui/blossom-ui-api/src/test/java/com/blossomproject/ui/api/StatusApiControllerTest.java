package com.blossomproject.ui.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class StatusApiControllerTest {

  @Mock
  HealthEndpoint healthEndpoint;

  @InjectMocks
  @Spy
  private StatusApiController controller;

  @Test
  public void should_return_all_status_with_health_up() throws Exception {
    Map<String, String> map = new HashMap();
    map.put("test", "testMessage");
    Health.Builder builder = new Health.Builder(Status.UP, map);
    Health health = builder.build();

    doReturn(health).when(controller).filteredDetails(any(), any(List.class));

    ResponseEntity<Health> response = controller.status(Optional.empty());

    assertNotNull(response);
    assertTrue(response.getStatusCode() == HttpStatus.OK);
    assertEquals(health, response.getBody());
  }

  @Test
  public void should_display_all_status_with_health_down() throws Exception {
    Map<String, String> map = new HashMap();
    map.put("test", "testMessage");
    Health.Builder builder = new Health.Builder(Status.DOWN, map);
    Health health = builder.build();

    doReturn(health).when(controller).filteredDetails(any(), any(List.class));

    ResponseEntity<Health> response = controller.status(Optional.empty());

    assertNotNull(response);
    assertTrue(response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR);
    assertEquals(health, response.getBody());
  }

  @Test
  public void should_display_status_up_with_excludes() throws Exception {
    Map<String, String> map = new HashMap();
    map.put("test", "testMessage");
    Health.Builder builder = new Health.Builder(Status.UP, map);
    Health health = builder.build();

    doReturn(health).when(controller).filteredDetails(any(), any(List.class));

    ResponseEntity<Health> response = controller.status(Optional.of(Lists.newArrayList("test1", "test2")));

    assertNotNull(response);
    assertTrue(response.getStatusCode() == HttpStatus.OK);
    assertEquals(health, response.getBody());
  }

  @Test
  public void should_display_status_down_without_excludes() throws Exception {
    Map<String, String> map = new HashMap();
    map.put("test", "testMessage");
    Health.Builder builder = new Health.Builder(Status.DOWN, map);
    Health health = builder.build();

    doReturn(health).when(controller).filteredDetails(any(), any(List.class));

    ResponseEntity<Health> response = controller.status(Optional.of(Lists.newArrayList("test1", "test2")));

    assertNotNull(response);
    assertTrue(response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR);
    assertEquals(health, response.getBody());
  }

  @Test
  public void should_filter_details_without_excludes() throws Exception {
    Health.Builder builder = new Health.Builder(Status.DOWN);
    Health healthChild = builder.build();
    Health.Builder builder2 = new Health.Builder(Status.DOWN);
    builder2.withDetail("healthChild", healthChild);
    Health health = builder2.build();

    SystemHealth systemHealth = Mockito.mock(SystemHealth.class);
    doReturn(Status.UP).when(systemHealth).getStatus();

    Health healthResponse = controller.filteredDetails(systemHealth, Lists.newArrayList());
    assertNotNull(healthResponse);
    assertEquals(healthResponse.getStatus(), systemHealth.getStatus());

  }

  @Test
  public void should_filter_details_with_excludes() throws Exception {
    Health.Builder builder = new Health.Builder(Status.DOWN);
    Health healthChild = builder.build();
    Health.Builder builder2 = new Health.Builder(Status.DOWN);
    builder2.withDetail("healthChild", healthChild);
    Health health = builder2.build();

    SystemHealth systemHealth = Mockito.mock(SystemHealth.class);
    doReturn(Status.UP).when(systemHealth).getStatus();

    Health healthResponse = controller.filteredDetails(systemHealth, Lists.newArrayList("healthChild"));
    assertNotNull(healthResponse);
    assertTrue(healthResponse.getDetails().isEmpty());
  }
}
