package com.blossomproject.module.search.elasticsearch;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceCreator;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchTraceRepositoryImplTest {

  private Client client;

  private BulkProcessor bulkProcessor;

  private Set<String> ignoredPatterns = new HashSet<>();

  private String alias = "test";

  private ElasticsearchTraceRepositoryImpl repository;

  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    this.client = mock(Client.class);
    this.bulkProcessor = mock(BulkProcessor.class);
    this.ignoredPatterns.add("/ignored.*");
    this.alias = "test";
    this.objectMapper = JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .addModule(new JavaTimeModule()).build();
    this.repository = spy(
      new ElasticsearchTraceRepositoryImpl(client, bulkProcessor, "test", ignoredPatterns,
        Collections.singleton("authorization"), Collections.singleton("set-cookie"), "{}", objectMapper));

    doNothing().when(this.repository).initializeIndex();
  }

  @Test
  public void should_initialize_correctly() {
    this.repository.initializeIndex();
  }

  @Test
  public void should_not_add_ignored_trace_info() throws JsonProcessingException {
    this.repository.add(HttpTraceCreator.createHttpTraceMock("/ignored/test"));
    verify(this.repository, times(0)).indexTrace(any(HttpTrace.class));
  }


  @Test
  public void should_add_trace_info() throws JsonProcessingException {
    HttpTrace traceInfo = HttpTraceCreator.createHttpTraceMock("/test");
    String serialized = "{\"test\":\"test\"}";

    IndexRequestBuilder mockedRequest = mock(IndexRequestBuilder.class);
    when(mockedRequest.setSource(anyString())).thenReturn(mockedRequest);

    when(this.client.prepareIndex(eq(alias), eq(alias))).thenReturn(mockedRequest);

    this.repository.add(traceInfo);
    verify(this.repository, times(1)).indexTrace(eq(traceInfo));
    verify(mockedRequest, times(1)).setSource(anyString());
  }

  @Test
  public void should_find_all_when_empty() {
    Long currentTimestamp = System.currentTimeMillis();

    List<HttpTrace> traces = this.repository.findAll();

    assertTrue(traces.isEmpty());
  }

  @Test
  public void should_find_all() throws JsonProcessingException {
    HttpTrace traceInfo = HttpTraceCreator.createHttpTraceMock("/test");
    String serialized = "{\"test\":\"test\"}";

    IndexRequestBuilder mockedRequest = mock(IndexRequestBuilder.class);
    when(mockedRequest.setSource(anyString())).thenReturn(mockedRequest);

    when(this.client.prepareIndex(eq(alias), eq(alias))).thenReturn(mockedRequest);

    this.repository.add(traceInfo);
    List<HttpTrace> traces = this.repository.findAll();
    assertFalse(traces.isEmpty());
    assertEquals(traceInfo, traces.get(0));
  }

  @Test
  public void should_filter_headers() {
    Map<String, List<String>> requestHeaders = new HashMap<>();
    Map<String, List<String>> responseHeaders = new HashMap<>();

    requestHeaders.put("Authorization", Collections.emptyList());
    requestHeaders.put("Host", Collections.emptyList());
    responseHeaders.put("Set-Cookie", Collections.emptyList());
    responseHeaders.put("Date", Collections.emptyList());

    HttpTrace traceInfo = HttpTraceCreator.createHttpTraceMock("/test", requestHeaders, responseHeaders);

    IndexRequestBuilder mockedRequest = mock(IndexRequestBuilder.class);
    when(mockedRequest.setSource(anyString())).thenAnswer(invocation -> {
      String arg = invocation.getArgument(0);
      assertFalse(arg.contains("Authorization"));
      assertTrue(arg.contains("Host"));
      assertFalse(arg.contains("Set-Cookie"));
      assertTrue(arg.contains("Date"));
      return mockedRequest;
    });

    when(this.client.prepareIndex(eq(alias), eq(alias))).thenReturn(mockedRequest);

    this.repository.add(traceInfo);
  }
}
