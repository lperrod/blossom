package com.blossomproject.core.group;

import com.blossomproject.core.common.dao.GenericCrudDaoImpl;
import com.querydsl.core.types.Predicate;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
public class GroupDaoImpl extends GenericCrudDaoImpl<Group> implements GroupDao {
  public GroupDaoImpl(GroupRepository repository) {
    super(repository);
  }

  @Override
  protected Group updateEntity(Group originalEntity, Group modifiedEntity) {
    originalEntity.setName(modifiedEntity.getName());
    originalEntity.setDescription(modifiedEntity.getDescription());
    return originalEntity;
  }

  @Override
  protected Predicate computeSearchPredicate(String query) {
    QGroup group = QGroup.group;
    return group.name.containsIgnoreCase(query);
  }
}
