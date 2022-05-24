package com.blossomproject.core.common.actuator;

import java.time.Instant;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;

public interface TraceRepository extends HttpTraceRepository {


  String stats(Instant from, Instant to, String precision);

}
