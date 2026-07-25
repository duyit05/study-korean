package com.example.back_end.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class R2PresignService {

    private final S3Presigner presigner;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    public String generatePresignedUrl(String key, Duration expiry) {
        return generatePresignedUrl(key, expiry, null);
    }

    public String generatePresignedUrl(String key, Duration expiry, String filename) {
        GetObjectRequest.Builder builder = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key);

        if (filename != null) {
            builder.responseContentDisposition("attachment; filename=\"" + filename + "\"");
        }

        GetObjectRequest getObjectRequest = builder.build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }
}