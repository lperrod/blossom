package com.blossomproject.core.group;

import com.blossomproject.core.common.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "blossom_group")
public class Group extends AbstractEntity {

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Lob
  @Column(name = "description", nullable = false)
  private String description;


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

  @Override
  public String toString() {
    return "Group{name='" + name + "\', description='" + description + "\'}";
  }
}
