package com.example.back_end.service;

import com.example.back_end.dto.response.ClassMaterialResponse;
import com.example.back_end.entity.Class;
import com.example.back_end.entity.ClassMaterial;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.ClassMaterialRepository;
import com.example.back_end.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassMaterialService {

    private final ClassMaterialRepository classMaterialRepository;
    private final ClassRepository classRepository;
    private final R2StorageService r2StorageService;
    private final R2PresignService r2PresignService;

    @Transactional(readOnly = true)
    public List<ClassMaterialResponse> getMaterialsByClass(Long classId) {
        // Verify class exists
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return classMaterialRepository.findByClazzIdOrderByCreatedAtDesc(classId).stream()
                .map(material -> {
                    String downloadUrl = "";
                    try {
                        // Generate a presigned URL valid for 30 minutes
                        downloadUrl = r2PresignService.generatePresignedUrl(material.getFileKey(),
                                Duration.ofMinutes(30));
                    } catch (Exception e) {
                        log.error("Failed to generate presigned URL for file: {}", material.getFileKey(), e);
                    }

                    return ClassMaterialResponse.builder()
                            .id(material.getId())
                            .title(material.getTitle())
                            .fileSize(material.getFileSize())
                            .contentType(material.getContentType())
                            .createdAt(material.getCreatedAt())
                            .downloadUrl(downloadUrl)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ClassMaterialResponse uploadMaterial(Long classId, MultipartFile file) throws IOException {
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Upload file to R2 under materials prefix
        String fileKey = r2StorageService.uploadFile(file, "materials");

        ClassMaterial material = ClassMaterial.builder()
                .clazz(clazz)
                .title(file.getOriginalFilename())
                .fileKey(fileKey)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        ClassMaterial saved = classMaterialRepository.save(material);

        String downloadUrl = "";
        try {
            downloadUrl = r2PresignService.generatePresignedUrl(saved.getFileKey(), Duration.ofMinutes(30));
        } catch (Exception e) {
            log.error("Failed to generate presigned URL", e);
        }

        return ClassMaterialResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .fileSize(saved.getFileSize())
                .contentType(saved.getContentType())
                .createdAt(saved.getCreatedAt())
                .downloadUrl(downloadUrl)
                .build();
    }

    @Transactional
    public void deleteMaterial(Long classId, Long materialId) {
        // Verify class exists
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        ClassMaterial material = classMaterialRepository.findById(materialId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!material.getClazz().getId().equals(classId)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        // Delete from R2
        try {
            r2StorageService.deleteFile(material.getFileKey());
        } catch (Exception e) {
            log.error("Failed to delete file from Cloudflare R2: {}", material.getFileKey(), e);
        }

        // Delete from DB
        classMaterialRepository.delete(material);
    }
}
