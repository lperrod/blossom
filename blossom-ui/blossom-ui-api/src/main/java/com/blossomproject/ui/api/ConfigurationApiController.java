package com.blossomproject.ui.api;

import com.blossomproject.ui.current_user.CurrentUser;
import com.blossomproject.ui.menu.Menu;
import com.blossomproject.ui.menu.MenuItem;
import com.blossomproject.ui.stereotype.BlossomApiController;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@BlossomApiController
@RequestMapping("/configuration")
public class ConfigurationApiController {

  private final Menu menu;
  private final Set<Locale> availableLocales;

  public ConfigurationApiController(Menu menu, Set<Locale> availableLocales) {
    this.menu = menu;
    this.availableLocales = availableLocales;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> configuration() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();

    Map<String, Object> config = new HashMap<>();

    // Current user info
    Map<String, Object> userInfo = new HashMap<>();
    userInfo.put("id", currentUser.getUser().getId());
    userInfo.put("identifier", currentUser.getUser().getIdentifier());
    userInfo.put("firstname", currentUser.getUser().getFirstname());
    userInfo.put("lastname", currentUser.getUser().getLastname());
    userInfo.put("email", currentUser.getUser().getEmail());
    userInfo.put("locale", currentUser.getUser().getLocale());
    userInfo.put("function", currentUser.getUser().getFunction());
    userInfo.put("company", currentUser.getUser().getCompany());
    userInfo.put("phone", currentUser.getUser().getPhone());
    config.put("user", userInfo);

    // Authorities
    List<String> authorities = currentUser.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.toList());
    config.put("authorities", authorities);

    // Impersonation status
    boolean impersonating = authentication.getAuthorities().stream()
      .anyMatch(a -> a instanceof SwitchUserGrantedAuthority);
    config.put("impersonating", impersonating);

    // Menu tree (filtered by current user privileges)
    Collection<MenuItem> filteredItems = menu.filteredItems(currentUser);
    config.put("menu", serializeMenuItems(filteredItems));

    // Available locales
    List<String> locales = availableLocales.stream()
      .map(Locale::toLanguageTag)
      .collect(Collectors.toList());
    config.put("locales", locales);

    return new ResponseEntity<>(config, HttpStatus.OK);
  }

  private List<Map<String, Object>> serializeMenuItems(Collection<MenuItem> items) {
    return items.stream().map(item -> {
      Map<String, Object> map = new HashMap<>();
      map.put("key", item.key());
      map.put("label", item.label());
      map.put("icon", item.icon());
      map.put("link", item.link());
      map.put("level", item.level());
      map.put("order", item.order());
      map.put("privilege", item.privilege());
      map.put("leaf", item.leaf());
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      CurrentUser user = (CurrentUser) auth.getPrincipal();
      Collection<MenuItem> children = item.filteredItems(user);
      if (!children.isEmpty()) {
        map.put("items", serializeMenuItems(children));
      }
      return map;
    }).collect(Collectors.toList());
  }
}
