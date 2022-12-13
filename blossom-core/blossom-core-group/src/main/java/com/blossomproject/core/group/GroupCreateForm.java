package com.blossomproject.core.group;

import jakarta.validation.constraints.NotBlank;
import java.util.Locale;

@UniqueGroupName
public class GroupCreateForm {

  @NotBlank(message = "{groups.group.validation.name.NotBlank.message}")
  private String name = "";

  private String description = "";

  private Locale locale = Locale.ENGLISH;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

}
