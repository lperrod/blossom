package com.blossomproject.ui.web;

import com.blossomproject.ui.stereotype.BlossomController;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@BlossomController
@RequestMapping("/login")
public class LoginController {

  @GetMapping
  public ModelAndView getLoginPage(@RequestParam Optional<String> error) {
    return new ModelAndView("blossom/login/login", "error", error);
  }

}
