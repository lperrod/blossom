package com.blossomproject.ui.api;

import com.blossomproject.core.user.UserDTO;
import com.blossomproject.core.user.UserService;
import com.blossomproject.ui.current_user.CurrentUser;
import com.blossomproject.ui.stereotype.BlossomApiController;
import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@BlossomApiController
@RequestMapping("/profile")
public class ProfileApiController {

  private final UserService userService;

  public ProfileApiController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> profile() {
    CurrentUser currentUser = getCurrentUser();
    UserDTO user = userService.getOne(currentUser.getUser().getId());
    if (user == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Map<String, Object> profile = new HashMap<>();
    profile.put("id", String.valueOf(user.getId()));
    profile.put("identifier", user.getIdentifier());
    profile.put("firstname", user.getFirstname());
    profile.put("lastname", user.getLastname());
    profile.put("email", user.getEmail());
    profile.put("phone", user.getPhone());
    profile.put("company", user.getCompany());
    profile.put("function", user.getFunction());
    profile.put("description", user.getDescription());
    profile.put("civility", user.getCivility());
    profile.put("locale", user.getLocale());
    profile.put("activated", user.isActivated());
    profile.put("lastConnection", user.getLastConnection());
    return new ResponseEntity<>(profile, HttpStatus.OK);
  }

  @PutMapping("/password")
  public ResponseEntity<Map<String, String>> updatePassword(
    @Valid @RequestBody PasswordUpdateRequest request) {
    if (!request.getPassword().equals(request.getPasswordRepeater())) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    CurrentUser currentUser = getCurrentUser();
    userService.updatePassword(currentUser.getUser().getId(), request.getPassword());
    Map<String, String> response = new HashMap<>();
    response.put("message", "Password updated successfully");
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  private CurrentUser getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (CurrentUser) authentication.getPrincipal();
  }

  public static class PasswordUpdateRequest {
    @NotBlank
    @Size(min = 8)
    @Pattern(regexp = ".*\\d.*", message = "must contain at least one digit")
    @Pattern(regexp = ".*[a-z].*", message = "must contain at least one lowercase letter")
    @Pattern(regexp = ".*[^a-zA-Z0-9].*", message = "must contain at least one special character")
    private String password;

    @NotBlank
    private String passwordRepeater;

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getPasswordRepeater() {
      return passwordRepeater;
    }

    public void setPasswordRepeater(String passwordRepeater) {
      this.passwordRepeater = passwordRepeater;
    }
  }
}
