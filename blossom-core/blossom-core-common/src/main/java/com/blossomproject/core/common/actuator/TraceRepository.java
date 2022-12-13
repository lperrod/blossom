package com.blossomproject.core.common.actuator;

import java.time.Instant;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;

public interface TraceRepository extends HttpExchangeRepository {


  String stats(Instant from, Instant to, String precision);

}
