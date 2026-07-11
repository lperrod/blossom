package com.blossomproject.ui.api;

import com.blossomproject.core.common.utils.action_token.ActionToken;
import com.blossomproject.core.common.utils.action_token.ActionTokenService;
import com.blossomproject.core.user.UserDTO;
import com.blossomproject.core.user.UserService;
import com.blossomproject.ui.stereotype.BlossomApiController;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@BlossomApiController
@RequestMapping("/public/activation")
public class ActivationApiController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivationApiController.class);

  private final ActionTokenService tokenService;
  private final UserService userService;

  public ActivationApiController(ActionTokenService tokenService, UserService userService) {
    this.tokenService = tokenService;
    this.userService = userService;
  }

  @PostMapping("/forgotten_password")
  public ResponseEntity<Void> forgottenPassword(@RequestBody Map<String, String> body) {
    String loginOrEmail = body.get("loginOrEmail");
    if (loginOrEmail == null || loginOrEmail.isBlank()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    LOGGER.info("Password reset request for user {}", loginOrEmail);

    UserDTO userDTO = this.userService.getByIdentifier(loginOrEmail)
      .orElse(this.userService.getByEmail(loginOrEmail).orElse(null));
    if (userDTO != null) {
      try {
        this.userService.askPasswordChange(userDTO.getId());
      } catch (Exception e) {
        LOGGER.error("Error sending password reset email for user {}", loginOrEmail, e);
      }
    }

    // Always return OK to avoid leaking user existence
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/validate_token")
  public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> body) {
    String token = body.get("token");
    if (token == null || token.isBlank()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    Map<String, Object> response = new HashMap<>();
    try {
      ActionToken actionToken = this.tokenService.decryptToken(token);
      if (actionToken.isValid()) {
        Optional<UserDTO> user = this.userService.getByActionToken(actionToken);
        if (user.isPresent()) {
          response.put("valid", true);
          response.put("userId", user.get().getId());
          return new ResponseEntity<>(response, HttpStatus.OK);
        }
      }
    } catch (Exception e) {
      LOGGER.error("Cannot decrypt action token", e);
    }

    response.put("valid", false);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PostMapping("/change_password")
  public ResponseEntity<Void> changePassword(@RequestBody Map<String, String> body) {
    String token = body.get("token");
    String password = body.get("password");
    String passwordRepeater = body.get("passwordRepeater");

    if (token == null || token.isBlank() || password == null || passwordRepeater == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    if (!password.equals(passwordRepeater)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    if (password.length() < 8) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    if (!password.matches("(?=.*[0-9]).+") || !password.matches("(?=.*[a-z]).+") || !password.matches("(?=.*[\\p{P}\\p{S}]).+")) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    ActionToken actionToken;
    try {
      actionToken = this.tokenService.decryptToken(token);
    } catch (Exception e) {
      LOGGER.error("Cannot decrypt action token", e);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    if (actionToken.isValid() && actionToken.getAction().equals(UserService.USER_RESET_PASSWORD)) {
      Optional<UserDTO> user = this.userService.getByActionToken(actionToken);
      if (user.isPresent()) {
        this.userService.updatePassword(user.get().getId(), password);
        return new ResponseEntity<>(HttpStatus.OK);
      }
    }

    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }

  @GetMapping("/activate")
  public ResponseEntity<Map<String, String>> activate(@RequestParam("token") String token) {
    ActionToken actionToken;
    try {
      actionToken = this.tokenService.decryptToken(token);
    } catch (Exception e) {
      LOGGER.error("Cannot decrypt action token", e);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    if (actionToken.isValid() && actionToken.getAction().equals(UserService.USER_ACTIVATION)) {
      Optional<UserDTO> user = this.userService.getByActionToken(actionToken);
      if (user.isPresent()) {
        this.userService.updateActivation(user.get().getId(), true);
        String resetToken = this.userService.generatePasswordResetToken(user.get());

        Map<String, String> response = new HashMap<>();
        response.put("resetToken", resetToken);
        return new ResponseEntity<>(response, HttpStatus.OK);
      }
    }

    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }
}
