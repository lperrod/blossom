package com.blossomproject.autoconfigure.ui.web;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

public class BlossomInvalidSessionStrategy implements SessionInformationExpiredStrategy {

  private final Logger logger = LoggerFactory.getLogger(BlossomInvalidSessionStrategy.class);

  private final String destinationUrl;

  private final RedirectStrategy redirectStrategy;

  public BlossomInvalidSessionStrategy(String invalidSessionUrl) {
    this(invalidSessionUrl, new DefaultRedirectStrategy());
  }

  public BlossomInvalidSessionStrategy(String invalidSessionUrl,
    RedirectStrategy redirectStrategy) {
    Assert.isTrue(UrlUtils.isValidRedirectUrl(invalidSessionUrl),
      "url must start with '/' or with 'http(s)'");
    this.destinationUrl = invalidSessionUrl;
    this.redirectStrategy = redirectStrategy;
  }

  @Override
  public void onExpiredSessionDetected(SessionInformationExpiredEvent event)
    throws IOException, ServletException {
    if (logger.isDebugEnabled()) {
      logger.debug("Redirecting to '" + destinationUrl + "'");
    }
    String ajaxHeader = event.getRequest().getHeader("X-Requested-With");

    if (ajaxHeader != null && "XMLHttpRequest".equals(ajaxHeader)) {
      logger.info("Ajax call detected, send {} error code", 401);
      event.getResponse().sendError(401);
      return;
    }

    redirectStrategy.sendRedirect(event.getRequest(), event.getResponse(), destinationUrl);
  }
}