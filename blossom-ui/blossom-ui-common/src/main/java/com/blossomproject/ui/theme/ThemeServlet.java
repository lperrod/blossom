package com.blossomproject.ui.theme;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.plugin.core.PluginRegistry;


public class ThemeServlet extends HttpServlet {
  public final static String BLOSSOM_THEME_SCSS_SERVLET = "/blossom/public/theme/style.css";
  public final static String BLOSSOM_THEME_MAIL_SCSS_SERVLET = "/blossom/public/theme/style_mail.css";
  private final Pattern cssPattern = Pattern.compile(".*/(?<name>[^_]\\w+)\\.css");
  private final PluginRegistry<Theme, String> themeRegistry;
  private final ThemeCompiler themeCompiler;
  private final String defaultThemeName;

  public ThemeServlet(PluginRegistry<Theme, String> themeRegistry,
    String defaultThemeName, ThemeCompiler themeCompiler) {
    this.themeRegistry = themeRegistry;
    this.defaultThemeName = defaultThemeName;
    this.themeCompiler = themeCompiler;
  }

  @Override
  protected void doGet(
    final HttpServletRequest request,
    final HttpServletResponse response
  ) throws ServletException, IOException {
    String requestURI = request.getRequestURI();
    final Matcher matcher = cssPattern.matcher(requestURI);
    if (!matcher.matches()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
      return;
    }

    String themeName = defaultThemeName;
    if (themeName == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
      return;
    }
    Theme theme = themeRegistry.getPluginFor(themeName).orElse(null);
    if (theme == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
      return;
    }

    response.setContentType("text/css");
    themeCompiler.getCss(theme.getName(), matcher.group("name"), response.getOutputStream());
  }
}
