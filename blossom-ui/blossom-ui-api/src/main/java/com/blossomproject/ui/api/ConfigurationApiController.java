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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

  private static final String BLOSSOM_PREFIX = "/blossom";

  private final Menu menu;
  private final Set<Locale> availableLocales;
  private final MessageSource messageSource;

  public ConfigurationApiController(Menu menu, Set<Locale> availableLocales,
    MessageSource messageSource) {
    this.menu = menu;
    this.availableLocales = availableLocales;
    this.messageSource = messageSource;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> configuration() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();

    Map<String, Object> config = new HashMap<>();

    // Current user info
    Map<String, Object> userInfo = new HashMap<>();
    userInfo.put("id", String.valueOf(currentUser.getUser().getId()));
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

    // Menu tree (filtered by current user privileges, with resolved labels and normalized links)
    Locale locale = LocaleContextHolder.getLocale();
    Collection<MenuItem> filteredItems = menu.filteredItems(currentUser);
    config.put("menu", serializeMenuItems(filteredItems, locale));

    // Available locales
    List<String> locales = availableLocales.stream()
      .map(Locale::toLanguageTag)
      .collect(Collectors.toList());
    config.put("locales", locales);

    return new ResponseEntity<>(config, HttpStatus.OK);
  }

  private List<Map<String, Object>> serializeMenuItems(Collection<MenuItem> items, Locale locale) {
    return items.stream().map(item -> {
      Map<String, Object> map = new HashMap<>();
      map.put("key", item.key());
      map.put("label", resolveLabel(item.label(), locale));
      map.put("icon", item.icon());
      map.put("link", normalizeLink(item.link()));
      map.put("level", item.level());
      map.put("order", item.order());
      map.put("privilege", item.privilege());
      map.put("leaf", item.leaf());
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      CurrentUser user = (CurrentUser) auth.getPrincipal();
      Collection<MenuItem> children = item.filteredItems(user);
      if (!children.isEmpty()) {
        map.put("items", serializeMenuItems(children, locale));
      }
      return map;
    }).collect(Collectors.toList());
  }

  private String resolveLabel(String labelKey, Locale locale) {
    if (labelKey == null) {
      return null;
    }
    return messageSource.getMessage(labelKey, null, labelKey, locale);
  }

  private String normalizeLink(String link) {
    if (link == null) {
      return null;
    }
    // Strip the /blossom prefix so Angular routes work directly
    // e.g. /blossom/system/dashboard -> /system/dashboard
    if (link.startsWith(BLOSSOM_PREFIX + "/")) {
      return link.substring(BLOSSOM_PREFIX.length());
    }
    if (link.equals(BLOSSOM_PREFIX)) {
      return "/";
    }
    return link;
  }
}
