package com.blossomproject.ui;

import com.blossomproject.core.user.UserService;
import com.blossomproject.ui.current_user.CurrentUser;
import java.io.IOException;
import java.util.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

public class BlossomAuthenticationSuccessHandlerImpl extends SavedRequestAwareAuthenticationSuccessHandler {

  private final UserService userService;

  private final Integer maxInactiveInterval;

  public BlossomAuthenticationSuccessHandlerImpl(UserService userService, Integer maxInactiveInterval) {
    this.userService = userService;
    this.maxInactiveInterval = maxInactiveInterval;

    this.setDefaultTargetUrl("/blossom");
    this.setAlwaysUseDefaultTargetUrl(false);
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    throws IOException, ServletException {

    if (request.getSession(false) != null) {
      request.getSession(false).setMaxInactiveInterval(maxInactiveInterval);
    }

    CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
    userService.updateLastConnection(currentUser.getUser().getId(), new Date(System.currentTimeMillis()));

    super.onAuthenticationSuccess(request, response, authentication);
  }
}
