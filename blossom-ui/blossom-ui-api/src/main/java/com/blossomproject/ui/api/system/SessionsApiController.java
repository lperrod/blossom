package com.blossomproject.ui.api.system;

import com.blossomproject.ui.security.LoginAttemptsService;
import com.blossomproject.ui.stereotype.BlossomApiController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@BlossomApiController
@RequestMapping("/system/sessions")
@PreAuthorize("hasAuthority('system:sessions:manager')")
public class SessionsApiController {

  private final SessionRegistry sessionRegistry;
  private final LoginAttemptsService loginAttemptsService;

  public SessionsApiController(SessionRegistry sessionRegistry, LoginAttemptsService loginAttemptsService) {
    this.sessionRegistry = sessionRegistry;
    this.loginAttemptsService = loginAttemptsService;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> sessions() {
    Map<String, Object> result = new HashMap<>();
    List<Object> principals = sessionRegistry.getAllPrincipals();
    List<Map<String, Object>> sessionsData = principals.stream().flatMap(principal -> {
      List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
      return sessions.stream().map(session -> {
        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("sessionId", session.getSessionId());
        sessionMap.put("principal", principal.toString());
        sessionMap.put("lastRequest", session.getLastRequest());
        sessionMap.put("expired", session.isExpired());
        return sessionMap;
      });
    }).collect(Collectors.toList());
    result.put("sessions", sessionsData);
    result.put("loginAttempts", loginAttemptsService.get());
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @PostMapping("/{sessionId}/_invalidate")
  public ResponseEntity<Void> invalidate(@PathVariable String sessionId) {
    SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionId);
    if (sessionInformation != null) {
      sessionInformation.expireNow();
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
