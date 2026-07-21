package com.example.back_end.controller;

import com.example.back_end.service.R2PresignService;
import com.example.back_end.service.R2StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final R2StorageService storageService;
    private final R2PresignService presignService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prefix", defaultValue = "audio") String prefix) throws IOException {
        String key = storageService.uploadFile(file, prefix);
        return ResponseEntity.ok(Map.of("key", key));
    }

    @GetMapping("/{key}/url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPresignedUrl(@PathVariable String key) {
        String url = presignService.generatePresignedUrl(key, Duration.ofMinutes(15));
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/download/{*key}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String key) {
        try {
            if (key != null && key.startsWith("/")) {
                key = key.substring(1);
            }
            byte[] bytes = storageService.getFileBytes(key);
            String contentType = "application/octet-stream";
            String lowerKey = key.toLowerCase();
            if (lowerKey.endsWith(".png"))
                contentType = "image/png";
            else if (lowerKey.endsWith(".jpg") || lowerKey.endsWith(".jpeg"))
                contentType = "image/jpeg";
            else if (lowerKey.endsWith(".gif"))
                contentType = "image/gif";
            else if (lowerKey.endsWith(".pdf"))
                contentType = "application/pdf";
            else if (lowerKey.endsWith(".mp3"))
                contentType = "audio/mpeg";
            else if (lowerKey.endsWith(".mp4"))
                contentType = "video/mp4";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
