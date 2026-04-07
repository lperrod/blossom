package com.blossomproject.ui.web.content.filemanager;

import com.blossomproject.module.filemanager.FileDTO;
import com.blossomproject.module.filemanager.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/files")
public class FileController {

  private final FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @GetMapping("/{id}")
  public void serve(@PathVariable("id") Long fileId, HttpServletResponse res) {
    FileDTO fileDTO = fileService.getOne(fileId);
    if (fileDTO != null) {
      res.setHeader(HttpHeaders.CONTENT_TYPE, fileDTO.getContentType());
      res.setHeader(HttpHeaders.CONTENT_LENGTH, fileDTO.getSize() + "");
      res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "Content-Disposition: inline; filename=\"" + fileDTO.getName() + "\"");
      res.setStatus(HttpStatus.NO_CONTENT.value());
      return;
    }

    res.setStatus(HttpStatus.NOT_FOUND.value());
  }
}
