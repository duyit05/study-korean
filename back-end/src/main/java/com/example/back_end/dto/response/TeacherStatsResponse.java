package com.example.back_end.dto.response;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStatsResponse {
    private List<MonthlySubmission> monthlySubmissions;
    private List<ClassAverageScore> classAverageScores;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySubmission {
        private String month;
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassAverageScore {
        private String className;
        private Double averageScore;
    }
}
