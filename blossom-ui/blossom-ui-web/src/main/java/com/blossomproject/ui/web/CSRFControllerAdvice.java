package com.blossomproject.ui.web;

import com.blossomproject.ui.stereotype.BlossomController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = BlossomController.class)
public class CSRFControllerAdvice {


  @ModelAttribute("_csrf")
  public CsrfToken csrfToken(HttpServletRequest request) {
    return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
  }

}
