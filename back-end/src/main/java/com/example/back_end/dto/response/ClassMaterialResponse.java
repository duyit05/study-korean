package com.example.back_end.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassMaterialResponse {
    private Long id;
    private String title;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createdAt;
    private String downloadUrl;
}
