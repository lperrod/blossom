package com.blossomproject.ui.api.content;

import com.blossomproject.module.filemanager.FileDTO;
import com.blossomproject.module.filemanager.FileService;
import com.blossomproject.ui.stereotype.BlossomApiController;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@BlossomApiController
@RequestMapping("/files")
public class FileApiController {

    private final FileService fileService;

    public FileApiController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{id}")
    public void serve(@PathVariable("id") Long fileId, HttpServletResponse res) {
        FileDTO fileDTO = fileService.getOne(fileId);
        if (fileDTO != null) {
            res.setHeader(HttpHeaders.CONTENT_TYPE, fileDTO.getContentType());
            res.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileDTO.getSize()));
            String safeName = fileDTO.getName().replaceAll("[\"\\r\\n]", "_");
            res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeName + "\"");
            res.setStatus(HttpStatus.OK.value());
            return;
        }
        res.setStatus(HttpStatus.NOT_FOUND.value());
    }
}
