package com.blossomproject.ui.supervision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.SimpleStatusAggregator;
import org.springframework.boot.health.actuate.endpoint.StatusAggregator;
import org.springframework.boot.health.actuate.endpoint.SystemHealthDescriptor;
import org.springframework.boot.health.contributor.Status;
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
  public void should_display_status_up() throws Exception {
    SystemHealthDescriptor systemHealth = mock(SystemHealthDescriptor.class);
    doReturn(Status.UP).when(systemHealth).getStatus();
    doReturn(systemHealth).when(healthEndpoint).health();
    doReturn(systemHealth).when(controller).filteredDetails(any(), any());

    ResponseEntity<HealthDescriptor> response = controller.status(Optional.empty(), Optional.empty());

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void should_display_status_down() throws Exception {
    SystemHealthDescriptor systemHealth = mock(SystemHealthDescriptor.class);
    doReturn(Status.DOWN).when(systemHealth).getStatus();
    doReturn(systemHealth).when(healthEndpoint).health();
    doReturn(systemHealth).when(controller).filteredDetails(any(), any());

    ResponseEntity<HealthDescriptor> response = controller.status(Optional.empty(), Optional.empty());

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  public void should_display_status_up_with_excludes() throws Exception {
    SystemHealthDescriptor systemHealth = mock(SystemHealthDescriptor.class);
    doReturn(Status.UP).when(systemHealth).getStatus();
    doReturn(systemHealth).when(healthEndpoint).health();
    doReturn(systemHealth).when(controller).filteredDetails(any(), any());

    ResponseEntity<HealthDescriptor> response = controller.status(
      Optional.of(Lists.newArrayList("test1", "test2")), Optional.empty());

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void should_filter_details() throws Exception {
    CompositeHealthDescriptor compositeHealth = mock(CompositeHealthDescriptor.class);
    Map<String, HealthDescriptor> components = new HashMap<>();
    CompositeHealthDescriptor child1 = mock(CompositeHealthDescriptor.class);
    CompositeHealthDescriptor child2 = mock(CompositeHealthDescriptor.class);
    components.put("db", child1);
    components.put("disk", child2);
    doReturn(components).when(compositeHealth).getComponents();

    HealthDescriptor result = controller.filteredDetails(compositeHealth, Lists.newArrayList("db"));

    assertNotNull(result);
  }
}
