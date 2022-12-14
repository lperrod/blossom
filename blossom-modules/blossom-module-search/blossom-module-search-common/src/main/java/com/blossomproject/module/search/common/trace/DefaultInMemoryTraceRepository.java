package com.blossomproject.module.search.common.trace;

import com.blossomproject.core.common.actuator.TraceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.util.CollectionUtils;

public class DefaultInMemoryTraceRepository extends InMemoryHttpExchangeRepository implements TraceRepository {

  private final Map<LocalDateTime, List<HttpExchange>> traces = new ConcurrentHashMap<>();

  private final Map<Long, Map<String, Integer>> responseTimeHistogram = new ConcurrentHashMap<>();

  private final List<Pattern> ignoredPatterns;


  private final Map<Long, Map<String, Integer>> responseStatusStats = new ConcurrentHashMap<>();

  private final Map<Long, Map<String, Integer>> responseContentTypeStats = new ConcurrentHashMap<>();


  private final Map<Long, Map<String, Integer>> calledUris = new ConcurrentHashMap<>();


  private final Map<Long, Map<String, Integer>> requestHistogram = new ConcurrentHashMap<>();


  public DefaultInMemoryTraceRepository(Set<String> ignoredPatterns) {
    this.ignoredPatterns = ignoredPatterns.stream().map(Pattern::compile).collect(Collectors.toList());
  }

  @Override
  public String stats(Instant from, Instant to, String precision) {
    if (from == null) {
      from = LocalDate.now().minusDays(7L).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
    if (to == null) {
      to = LocalDate.now().plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
    if (precision == null) {
      precision = "1h";
    }
    if ("2h".equals(precision)) {
      precision = "1h";
    }

    LocalDateTime dateFrom = LocalDateTime.ofInstant(from, ZoneOffset.UTC);
    LocalDateTime dateTo = LocalDateTime.ofInstant(to, ZoneOffset.UTC);

    long adjustment = getChronoAdjustment(precision);
    ChronoUnit chronoUnit = getChronoUnitFromShiftUnit(precision);
    dateFrom = dateFrom.truncatedTo(chronoUnit);
    dateTo = dateTo.truncatedTo(chronoUnit);

    List<LocalDateTime> dates = new ArrayList<>();
    dates.add(dateFrom);
    LocalDateTime currentDate = LocalDateTime.ofInstant(dateFrom.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    while (currentDate.isBefore(dateTo)) {
      currentDate = currentDate.plus(adjustment, chronoUnit);
      dates.add(currentDate);
    }

    List<Long> timestamps = dates.stream().map(date -> Timestamp.valueOf(date).getTime()).collect(Collectors.toList());

    Aggregations aggregations = new Aggregations();
    aggregations.setMethods(computeMethodAggregation(timestamps));
    aggregations.setFlop_uris(computeFlopURIs(timestamps));
    aggregations.setTop_uris(computeTopURIs(timestamps));
    aggregations.setResponse_content_type_stats(computeContentTypeAggregation(timestamps));
    aggregations.setResponse_status_stats(computeResponseStatusAggregation(timestamps));
    aggregations.setResponse_time_histogram(computeResponseTimeAggregation(timestamps));
    aggregations.setRequest_histogram(computeRequestAggregation(dates));

    TraceResponse response = new TraceResponse();
    response.setAggregations(aggregations);

    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(response);
    } catch (JsonProcessingException e) {
      return "";
    }
  }

  @Override
  public List<HttpExchange> findAll() {
    return traces.values().stream().flatMap(List::stream).sorted(Comparator.comparing(HttpExchange::getTimestamp))
      .collect(Collectors.toList());
  }

  @Override
  public void add(HttpExchange trace) {

    String path = trace.getRequest().getUri().getPath();
    if (Strings.isNullOrEmpty(path)) {
      return;
    }

    boolean ignore = ignoredPatterns.stream().anyMatch(pattern -> pattern.matcher(path).matches());
    if (ignore) {
      return;
    }
    LocalDateTime localDateTimeSeconds = LocalDateTime.ofInstant(trace.getTimestamp(), ZoneOffset.UTC)
      .truncatedTo(ChronoUnit.SECONDS);
    LocalDateTime localDateTimeMinutes = localDateTimeSeconds.truncatedTo(ChronoUnit.MINUTES);
    LocalDateTime localDateTimeHours = localDateTimeMinutes.truncatedTo(ChronoUnit.HOURS);

    List<LocalDateTime> dates = Lists.newArrayList(localDateTimeHours, localDateTimeMinutes, localDateTimeSeconds);
    dates.forEach(date -> {
      computeAllTraces(date, trace);
      computeCalledUris(date, trace);
      computeRequestHistogram(date, trace);
      computeResponseStatusStat(date, trace);
      computeResponseContentType(date, trace);
      computeResponseTimeHistogram(date, trace);
      deleteOldTraces(date);
    });

    super.add(trace);
  }

  private void deleteOldTraces(LocalDateTime dateTime) {

    LocalDateTime aWeekAgo = dateTime.minusDays(8);
    traces.remove(aWeekAgo);
    Long timestamp = Timestamp.valueOf(aWeekAgo).getTime();
    responseTimeHistogram.remove(timestamp);
    responseStatusStats.remove(timestamp);
    requestHistogram.remove(timestamp);
    calledUris.remove(timestamp);
    responseContentTypeStats.remove(timestamp);


  }

  private void computeAllTraces(LocalDateTime localDateTime, HttpExchange httpExchange) {
    List<HttpExchange> tracesForTime = traces.computeIfAbsent(localDateTime, value -> new ArrayList<>());
    tracesForTime.add(httpExchange);
    traces.put(localDateTime, tracesForTime);
  }

  private void computeResponseStatusStat(LocalDateTime localDateTime, HttpExchange httpExchange) {
    computeData(localDateTime, responseStatusStats, String.valueOf(httpExchange.getResponse().getStatus()));
  }

  private void computeCalledUris(LocalDateTime localDateTime, HttpExchange httpExchange) {
    computeData(localDateTime, calledUris, httpExchange.getRequest().getUri().toString());
  }

  private void computeRequestHistogram(LocalDateTime localDateTime, HttpExchange httpExchange) {
    computeData(localDateTime, requestHistogram, httpExchange.getRequest().getMethod());
  }

  private void computeResponseContentType(LocalDateTime localDateTime, HttpExchange httpExchange) {
    Map<String, List<String>> headers = httpExchange.getResponse().getHeaders();
    if (headers != null && !CollectionUtils.isEmpty(headers.get("Content-Type"))) {
      computeData(localDateTime, responseContentTypeStats,
        headers.get("Content-Type").get(0));
    }
  }

  private void computeResponseTimeHistogram(LocalDateTime localDateTime, HttpExchange httpExchange) {
    computeData(localDateTime, responseTimeHistogram, String.valueOf(httpExchange.getTimeTaken()));
  }

  private void computeData(LocalDateTime localDateTime,
    Map<Long, Map<String, Integer>> repository, String dataKey) {
    Map<String, Integer> buckets = repository.computeIfAbsent(Timestamp.valueOf(localDateTime).getTime(),
      value -> new ConcurrentHashMap<>());
    Integer docCounts = buckets.computeIfAbsent(dataKey, key -> 0);

    buckets.put(dataKey, docCounts + 1);
  }

  long getChronoAdjustment(String precision) {
    return Long.parseLong(precision.substring(0, 1));
  }

  ChronoUnit getChronoUnitFromShiftUnit(String precision) {

    if (precision.contains("ms")) {
      return ChronoUnit.MILLIS;
    }
    if (precision.contains("m")) {
      return ChronoUnit.MINUTES;
    }
    if (precision.contains("h")) {
      return ChronoUnit.HOURS;
    }
    if (precision.contains("s")) {
      return ChronoUnit.SECONDS;
    }

    return ChronoUnit.MINUTES;
  }

  private Aggregation computeRequestAggregation(List<LocalDateTime> dates) {
    Aggregation aggregation = new Aggregation();

    aggregation.setBuckets(dates.stream().map(date -> {
      Long timestamp = Timestamp.valueOf(date).getTime();
      Map<String, Integer> data = requestHistogram.computeIfAbsent(timestamp, key -> new ConcurrentHashMap<>());

      int totalDocCount = data.values().stream().mapToInt(Integer::intValue).sum();
      Bucket bucket = computeBucket(timestamp, totalDocCount);
      Aggregation methodAggregation = new Aggregation();
      methodAggregation.setBuckets(data.entrySet().stream().map(entry ->
        computeBucket(entry.getKey(), entry.getValue())

      ).collect(Collectors.toList()));
      bucket.setMethods(methodAggregation);

      return bucket;
    }).filter(bucket -> bucket.getDoc_count() > 0).collect(Collectors.toList()));

    return aggregation;
  }


  private Aggregation computeMethodAggregation(List<Long> dates) {
    Aggregation aggregation = new Aggregation();
    aggregation.setBuckets(dates.stream().map(date -> requestHistogram.computeIfAbsent(date, key -> new ConcurrentHashMap<>()))
      .filter(map -> !map.isEmpty()).map(Map::entrySet)
      .map(set -> set.stream().map(entry -> computeBucket(entry.getKey(), entry.getValue())).collect(Collectors.toList()))
      .flatMap(List::stream).collect(Collectors.toList()));

    return aggregation;
  }

  private Aggregation computeFlopURIs(List<Long> dates) {
    Aggregation aggregation = new Aggregation();

    Map<String, Integer> mergedValues = new ConcurrentHashMap<>();
    List<Entry<String, Integer>> sets = dates.stream()
      .map(date -> calledUris.computeIfAbsent(date, key -> new ConcurrentHashMap<>()))
      .filter(map -> !map.isEmpty()).map(Map::entrySet).flatMap(Set::stream).collect(Collectors.toList());
    sets.forEach(entry -> {
      Integer result = mergedValues.computeIfAbsent(entry.getKey(), key -> 0);
      result = result + entry.getValue();
      mergedValues.put(entry.getKey(), result);
    });

    aggregation.setBuckets(mergedValues.entrySet().stream().map(entry -> computeBucket(entry.getKey(), entry.getValue())).collect(
        Collectors.toList()).stream().sorted(Comparator.comparing(Bucket::getDoc_count)).limit(7)
      .collect(Collectors.toList()));

    return aggregation;
  }

  private Aggregation computeTopURIs(List<Long> dates) {
    Aggregation aggregation = new Aggregation();
    Map<String, Integer> mergedValues = new ConcurrentHashMap<>();
    List<Entry<String, Integer>> sets = dates.stream()
      .map(date -> calledUris.computeIfAbsent(date, key -> new ConcurrentHashMap<>()))
      .filter(map -> !map.isEmpty()).map(Map::entrySet).flatMap(Set::stream).collect(Collectors.toList());
    sets.forEach(entry -> {
      Integer result = mergedValues.computeIfAbsent(entry.getKey(), key -> 0);
      result = result + entry.getValue();
      mergedValues.put(entry.getKey(), result);
    });

    aggregation.setBuckets(mergedValues.entrySet().stream().map(entry -> computeBucket(entry.getKey(), entry.getValue())).collect(
        Collectors.toList()).stream().sorted(Comparator.comparing(Bucket::getDoc_count).reversed()).limit(7)
      .collect(Collectors.toList()));

    return aggregation;
  }

  private Aggregation computeContentTypeAggregation(List<Long> dates) {
    Aggregation aggregation = new Aggregation();

    Map<String, Integer> mergedValues = new ConcurrentHashMap<>();
    List<Entry<String, Integer>> sets = dates.stream()
      .map(date -> responseContentTypeStats.computeIfAbsent(date, key -> new ConcurrentHashMap<>()))
      .filter(map -> !map.isEmpty()).map(Map::entrySet).flatMap(Set::stream).collect(Collectors.toList());
    sets.forEach(entry -> {
      Integer result = mergedValues.computeIfAbsent(entry.getKey(), key -> 0);
      result = result + entry.getValue();
      mergedValues.put(entry.getKey(), result);
    });

    aggregation.setBuckets(mergedValues.entrySet().stream().map(entry -> computeBucket(entry.getKey(), entry.getValue())).collect(
      Collectors.toList()));

    return aggregation;
  }

  private Aggregation computeResponseStatusAggregation(List<Long> dates) {
    Aggregation aggregation = new Aggregation();

    Map<String, Integer> mergedValues = new ConcurrentHashMap<>();
    List<Entry<String, Integer>> sets = dates.stream()
      .map(date -> responseStatusStats.computeIfAbsent(date, key -> new ConcurrentHashMap<>()))
      .filter(map -> !map.isEmpty()).map(Map::entrySet).flatMap(Set::stream).collect(Collectors.toList());
    sets.forEach(entry -> {
      Integer result = mergedValues.computeIfAbsent(entry.getKey(), key -> 0);
      result = result + entry.getValue();
      mergedValues.put(entry.getKey(), result);
    });

    aggregation.setBuckets(mergedValues.entrySet().stream().map(entry -> computeBucket(entry.getKey(), entry.getValue())).collect(
      Collectors.toList()));

    return aggregation;
  }

  private Aggregation computeResponseTimeAggregation(List<Long> dates) {
    Aggregation aggregation = new Aggregation();

    Map<String, Integer> mergedValues = new ConcurrentHashMap<>();
    List<Entry<String, Integer>> sets = dates.stream()
      .map(date -> responseTimeHistogram.computeIfAbsent(date, key -> new ConcurrentHashMap<>()))
      .filter(map -> !map.isEmpty()).map(Map::entrySet).flatMap(Set::stream).collect(Collectors.toList());
    sets.forEach(entry -> {
      Integer result = mergedValues.computeIfAbsent(entry.getKey(), key -> 0);
      result = result + entry.getValue();
      mergedValues.put(entry.getKey(), result);
    });

    aggregation.setBuckets(mergedValues.entrySet().stream().map(entry -> computeBucket(entry.getKey(), entry.getValue())).collect(
      Collectors.toList()));

    return aggregation;
  }

  private Bucket computeBucket(Object timestamp, int docCount) {
    Bucket bucket = new Bucket();
    bucket.setKey(timestamp);
    bucket.setDoc_count(docCount);
    return bucket;
  }

}
