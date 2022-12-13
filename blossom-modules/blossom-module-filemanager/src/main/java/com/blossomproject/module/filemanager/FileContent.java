package com.blossomproject.module.filemanager;

import com.blossomproject.core.common.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * Created by Maël Gargadennnec on 22/05/2017.
 */
@Entity
@Table(name = "blossom_file_content")
public class FileContent extends AbstractEntity {

  @Column(name = "file_id")
  private Long fileId;

  @Lob
  @Column(name = "data")
  private byte[] data;

  public Long getFileId() {
    return fileId;
  }

  public void setFileId(Long fileId) {
    this.fileId = fileId;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }
}
