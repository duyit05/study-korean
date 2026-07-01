package com.example.back_end.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySetResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String creatorName;
    private List<CardResponse> words;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CardResponse {
        private Long id;
        private String korean;
        private String vietnamese;
        private String example;
    }
}
