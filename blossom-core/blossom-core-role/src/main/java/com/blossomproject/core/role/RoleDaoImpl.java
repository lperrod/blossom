package com.blossomproject.core.role;

import com.blossomproject.core.common.dao.GenericCrudDaoImpl;
import com.querydsl.core.types.Predicate;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
public class RoleDaoImpl extends GenericCrudDaoImpl<Role> implements RoleDao {
  public RoleDaoImpl(RoleRepository repository) {
    super(repository);
  }

  @Override
  protected Role updateEntity(Role originalEntity, Role modifiedEntity) {
    originalEntity.setName(modifiedEntity.getName());
    originalEntity.setDescription(modifiedEntity.getDescription());
    originalEntity.getPrivileges().clear();
    originalEntity.getPrivileges().addAll(modifiedEntity.getPrivileges());
    return originalEntity;
  }


  @Override
  protected Predicate computeSearchPredicate(String query) {
    QRole role = QRole.role;
    return role.name.containsIgnoreCase(query);
  }
}
