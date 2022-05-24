package com.blossomproject.core.group;

import com.blossomproject.core.common.service.SearchAndCrudService;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
public interface GroupService extends SearchAndCrudService<GroupDTO> {

  GroupDTO create(GroupCreateForm groupCreateForm) throws Exception;

  GroupDTO update(Long groupId, GroupUpdateForm groupUpdateForm);
}
