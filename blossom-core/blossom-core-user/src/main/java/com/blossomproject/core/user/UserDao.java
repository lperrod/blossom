package com.blossomproject.core.user;

import com.blossomproject.core.common.dao.SearchAndCrudDao;
import java.util.Date;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
public interface UserDao extends SearchAndCrudDao<User> {

  User getByIdentifier(String identifier);

  User getByEmail(String email);

  User updateActivation(long id, boolean activated);

  User updatePassword(Long id, String encodedPassword);

  User updateLastConnection(Long id, Date lastConnection);

  User updateAvatar(Long id, byte[] avatar);
}
