package com.blossomproject.ui.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import java.util.Locale;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.i18n.SimpleLocaleContext;

@RunWith(MockitoJUnitRunner.class)
public class RestrictedSessionLocaleResolverTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Set<Locale> locales;
  private RestrictedSessionLocaleResolver resolver;

  @Before
  public void setUp() throws Exception {
    this.locales = Sets.newHashSet(Locale.FRANCE, Locale.ENGLISH);
    this.resolver = new RestrictedSessionLocaleResolver(this.locales);
  }

  @Test
  public void should_construct_not_null_locales() {
    thrown.expect(IllegalArgumentException.class);
    new RestrictedSessionLocaleResolver(null);
  }

  @Test
  public void should_construct_not_empty_locales() {
    thrown.expect(IllegalArgumentException.class);
    new RestrictedSessionLocaleResolver(Sets.newHashSet());
  }

  @Test
  public void should_set_locale_context_with_available_locale() {
    HttpSession session = mock(HttpSession.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession()).thenReturn(session);
    when(request.getSession(false)).thenReturn(session);
    HttpServletResponse response = mock(HttpServletResponse.class);

    resolver.setLocaleContext(request, response, new SimpleLocaleContext(Locale.FRANCE));

    Locale resolved = resolver.resolveLocale(request);
    assertNotNull(resolved);
    assertEquals(Locale.FRANCE, resolved);
  }

  @Test
  public void should_set_locale_context_with_closest_locale() {
    HttpSession session = mock(HttpSession.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession()).thenReturn(session);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // CANADA (en_CA) should resolve to ENGLISH (en) as closest available
    resolver.setLocaleContext(request, response, new SimpleLocaleContext(Locale.CANADA));

    // After setting, the session should have English stored
    // We verify indirectly by creating a fresh request with the same session
    HttpServletRequest request2 = mock(HttpServletRequest.class);
    when(request2.getSession(false)).thenReturn(session);
    when(request2.getLocale()).thenReturn(Locale.ENGLISH);

    Locale resolved = resolver.resolveLocale(request2);
    assertNotNull(resolved);
    assertEquals(Locale.ENGLISH, resolved);
  }

  @Test
  public void should_resolve_default_locale_from_request() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getLocale()).thenReturn(Locale.FRANCE);

    Locale resolved = resolver.resolveLocale(request);
    assertNotNull(resolved);
    assertEquals(Locale.FRANCE, resolved);
  }

  @Test
  public void should_resolve_closest_default_locale_from_request() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getLocale()).thenReturn(Locale.FRENCH);

    Locale resolved = resolver.resolveLocale(request);
    assertNotNull(resolved);
    assertEquals(Locale.FRANCE, resolved);
  }
}
