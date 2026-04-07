package com.blossomproject.module.filemanager;

import com.blossomproject.core.common.service.SearchAndCrudService;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileService extends SearchAndCrudService<FileDTO> {

  FileDTO upload(MultipartFile multipartFile) throws IOException;

}
