package com.blossomproject.ui.menu;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Maël Gargadennnec on 08/06/2017.
 */
public class MenuInterceptor implements HandlerInterceptor {

  private final PluginRegistry<MenuItem, String> registry;

  public MenuInterceptor(PluginRegistry<MenuItem, String> registry) {
    this.registry = registry;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
    throws Exception {
    if (handler instanceof HandlerMethod hm && modelAndView != null) {
      Method method = hm.getMethod();

      String menuId = null;
      if (method.isAnnotationPresent(OpenedMenu.class)) {
        menuId = method.getAnnotation(OpenedMenu.class).value();
      } else if (method.getDeclaringClass().isAnnotationPresent(OpenedMenu.class)) {
        menuId = method.getDeclaringClass().getAnnotation(OpenedMenu.class).value();
      }
      List<String> currentMenu = Lists.newArrayList();
      if (!StringUtils.isEmpty(menuId) && registry.hasPluginFor(menuId)) {

        MenuItem menuItem = registry.getPluginFor(menuId).orElse(null);
        while (menuItem != null) {
          currentMenu.add(menuItem.key());
          menuItem = menuItem.parent();
        }
      } else {
        currentMenu.add("home");
      }

      modelAndView.addObject("currentMenu", currentMenu);
    }
  }
}
