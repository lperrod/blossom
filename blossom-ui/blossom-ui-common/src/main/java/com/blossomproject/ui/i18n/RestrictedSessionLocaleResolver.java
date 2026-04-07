package com.blossomproject.ui.i18n;

import com.google.common.base.Preconditions;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

public class RestrictedSessionLocaleResolver extends SessionLocaleResolver {

  private final Set<Locale> availableLocales;

  public RestrictedSessionLocaleResolver(Set<Locale> availableLocales) {
    Preconditions.checkArgument(availableLocales != null && !availableLocales.isEmpty());
    this.availableLocales = availableLocales;
    setDefaultLocaleFunction(this::resolveDefaultLocale);
  }

  @Override
  public void setLocaleContext(HttpServletRequest request, HttpServletResponse response, LocaleContext localeContext) {
    Locale locale = localeContext != null ? localeContext.getLocale() : null;
    if (locale == null) {
      super.setLocaleContext(request, response, localeContext);
      return;
    }

    Locale resolvedLocale = resolveToAvailableLocale(locale);
    super.setLocaleContext(request, response, new SimpleLocaleContext(resolvedLocale));
  }

  private Locale resolveDefaultLocale(HttpServletRequest request) {
    Locale requestLocale = request.getLocale();
    if (requestLocale != null) {
      if (availableLocales.contains(requestLocale)) {
        return requestLocale;
      }
      Optional<Locale> closest = availableLocales.stream()
        .filter(aLocale -> aLocale.getLanguage().equals(requestLocale.getLanguage())).findFirst();
      if (closest.isPresent()) {
        return closest.get();
      }
    }
    Locale defaultLocale = getDefaultLocale();
    return defaultLocale != null ? defaultLocale : availableLocales.iterator().next();
  }

  private Locale resolveToAvailableLocale(Locale locale) {
    if (availableLocales.contains(locale)) {
      return locale;
    }
    Optional<Locale> closest = availableLocales.stream()
      .filter(aLocale -> aLocale.getLanguage().equals(locale.getLanguage())).findFirst();
    if (closest.isPresent()) {
      return closest.get();
    }
    Locale defaultLocale = getDefaultLocale();
    return defaultLocale != null ? defaultLocale : availableLocales.iterator().next();
  }
}
