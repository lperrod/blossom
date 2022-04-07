package com.blossomproject.module.filemanager;

import com.blossomproject.core.common.dao.GenericCrudDaoImpl;
import com.querydsl.core.types.Predicate;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
public class FileDaoImpl extends GenericCrudDaoImpl<File> implements FileDao {
  public FileDaoImpl(FileRepository repository) {
    super(repository);
  }

  @Override
  protected File updateEntity(File originalEntity, File modifiedEntity) {
    originalEntity.setName(modifiedEntity.getName());
    originalEntity.setContentType(modifiedEntity.getContentType());
    originalEntity.setExtension(modifiedEntity.getExtension());
    originalEntity.setSize(modifiedEntity.getSize());
    originalEntity.setTags(modifiedEntity.getTags());
    originalEntity.setHash(modifiedEntity.getHash());
    originalEntity.setHashAlgorithm(modifiedEntity.getHashAlgorithm());

    return originalEntity;
  }

  @Override
  protected Predicate computeSearchPredicate(String query) {
    QFile qFile = QFile.file;
    return qFile.name.containsIgnoreCase(query);
  }
}
