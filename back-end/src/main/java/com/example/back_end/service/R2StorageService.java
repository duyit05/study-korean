package com.example.back_end.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class R2StorageService {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.endpoint}")
    private String endpoint;

    private static final String UPLOADS_DIR = "uploads";

    private boolean isDummyConfig() {
        return endpoint == null || endpoint.contains("dummy") || endpoint.contains("localhost");
    }

    public String uploadFile(MultipartFile file, String keyPrefix) throws IOException {
        String key = keyPrefix + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        if (isDummyConfig()) {
            log.info("R2 endpoint is dummy. Saving file locally: {}", key);
            File localFile = new File(UPLOADS_DIR, key);
            localFile.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                fos.write(file.getBytes());
            }
            return "local/" + key;
        }

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            return key;
        } catch (Exception e) {
            log.warn("Failed to upload to Cloudflare R2, falling back to local file storage: {}", key, e);
            File localFile = new File(UPLOADS_DIR, key);
            localFile.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                fos.write(file.getBytes());
            }
            return "local/" + key;
        }
    }

    public void deleteFile(String key) {
        if (key != null && key.startsWith("local/")) {
            String relativeKey = key.substring(6);
            File localFile = new File(UPLOADS_DIR, relativeKey);
            if (localFile.exists()) {
                localFile.delete();
            }
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to delete file from S3: {}", key, e);
        }
    }

    public byte[] getFileBytes(String key) throws IOException {
        if (key != null && key.startsWith("local/")) {
            String relativeKey = key.substring(6);
            File localFile = new File(UPLOADS_DIR, relativeKey);
            if (localFile.exists()) {
                try (FileInputStream fis = new FileInputStream(localFile)) {
                    return fis.readAllBytes();
                }
            }
        }

        try {
            software.amazon.awssdk.services.s3.model.GetObjectRequest request = 
                software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            return s3Client.getObjectAsBytes(request).asByteArray();
        } catch (Exception e) {
            log.warn("Failed to download from R2, checking local storage for key: {}", key);
            File localFile = new File(UPLOADS_DIR, key);
            if (localFile.exists()) {
                try (FileInputStream fis = new FileInputStream(localFile)) {
                    return fis.readAllBytes();
                }
            }
            throw new IOException("File not found in S3 or local fallback", e);
        }
    }
}
