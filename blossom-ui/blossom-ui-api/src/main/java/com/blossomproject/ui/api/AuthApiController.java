package com.blossomproject.ui.api;

import com.blossomproject.ui.current_user.CurrentUser;
import com.blossomproject.ui.stereotype.BlossomApiController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@BlossomApiController
@RequestMapping("/auth")
public class AuthApiController {

  private final AuthenticationManager authenticationManager;

  public AuthApiController(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials,
    HttpServletRequest request) {
    String username = credentials.get("username");
    String password = credentials.get("password");

    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(username, password));

    SecurityContext securityContext = SecurityContextHolder.getContext();
    securityContext.setAuthentication(authentication);

    HttpSession session = request.getSession(true);
    session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

    CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
    return new ResponseEntity<>(buildUserResponse(currentUser), HttpStatus.OK);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    SecurityContextHolder.clearContext();
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("/current-user")
  public ResponseEntity<Map<String, Object>> currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser)) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
    return new ResponseEntity<>(buildUserResponse(currentUser), HttpStatus.OK);
  }

  private Map<String, Object> buildUserResponse(CurrentUser currentUser) {
    Map<String, Object> userInfo = new HashMap<>();
    userInfo.put("id", currentUser.getUser().getId());
    userInfo.put("identifier", currentUser.getUser().getIdentifier());
    userInfo.put("firstname", currentUser.getUser().getFirstname());
    userInfo.put("lastname", currentUser.getUser().getLastname());
    userInfo.put("email", currentUser.getUser().getEmail());
    userInfo.put("locale", currentUser.getUser().getLocale());
    List<String> authorities = currentUser.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.toList());
    userInfo.put("authorities", authorities);
    return userInfo;
  }
}
