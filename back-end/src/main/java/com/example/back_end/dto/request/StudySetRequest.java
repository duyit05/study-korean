package com.example.back_end.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySetRequest {
    private String title;
    private String description;
    private String category;
}
