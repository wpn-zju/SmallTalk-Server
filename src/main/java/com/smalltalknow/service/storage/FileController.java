package com.smalltalknow.service.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileController {
    private final StorageService storageService;

    @Autowired
    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/download/{subfolder}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(
            @PathVariable String subfolder,
            @PathVariable String filename) {
        Resource file = storageService.loadAsResource(String.format("%s/%s", subfolder, filename));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=\"%s\"", file.getFilename())).body(file);
    }

    @PostMapping("/upload/{subfolder}")
    public ResponseEntity<?> handleFileUpload(
            @PathVariable String subfolder,
            @RequestParam("file") MultipartFile file) {
        storageService.store(subfolder, file);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
