package com.blossomproject.core.association_user_group;

import com.blossomproject.core.common.entity.AbstractAssociationEntity;
import com.blossomproject.core.group.Group;
import com.blossomproject.core.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "blossom_association_user_group")
public class AssociationUserGroup extends AbstractAssociationEntity<User, Group> {

  @ManyToOne(fetch = FetchType.LAZY)
  @Fetch(FetchMode.JOIN)
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User a;

  @ManyToOne(fetch = FetchType.LAZY)
  @Fetch(FetchMode.JOIN)
  @JoinColumn(name = "group_id", referencedColumnName = "id")
  private Group b;

  @Override
  public User getA() {
    return this.a;
  }

  @Override
  public void setA(User user) {
    this.a = user;
  }

  @Override
  public Group getB() {
    return this.b;
  }

  @Override
  public void setB(Group group) {
    this.b = group;
  }
}
