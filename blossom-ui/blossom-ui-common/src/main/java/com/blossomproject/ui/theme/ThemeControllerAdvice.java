package com.blossomproject.ui.theme;

import com.blossomproject.ui.stereotype.BlossomController;
import java.util.List;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = BlossomController.class)
public class ThemeControllerAdvice {

  private final PluginRegistry<Theme, String> themeRegistry;

  private final String defaultThemeName;

  public ThemeControllerAdvice(PluginRegistry<Theme, String> themeRegistry,
    String defaultThemeName) {
    this.themeRegistry = themeRegistry;
    this.defaultThemeName = defaultThemeName;
  }

  @ModelAttribute("themes")
  public List<Theme> themes() {
    return themeRegistry.getPlugins();
  }

  @ModelAttribute("currentTheme")
  public Theme currentTheme() {
    return themeRegistry.getPluginFor(defaultThemeName)
      .orElse(themeRegistry.getPlugins().isEmpty() ? null : themeRegistry.getPlugins().get(0));
  }

}
