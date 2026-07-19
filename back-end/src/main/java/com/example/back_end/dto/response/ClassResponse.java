package com.example.back_end.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long topikLevelId;
    private String name;
    private String schedule;
    private String room;
    private String code;
    private String teacherName;
    private Integer studentsCount;
    private String status;
    private LocalDate startedAt;
    private String notes;
    private List<StudentDto> students;
    private List<AssignedStudySetDto> assignedStudySets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentDto {
        private Long id;
        private String name;
        private String email;
        private Integer vocabProgress;
        private Integer avgScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignedStudySetDto {
        private Long id;
        private Long studySetId;
        private String studySetTitle;
        private String studySetDescription;
        private Integer wordCount;
        private Integer learnedCount;
        private String dueDate;
        private String note;
        private String assignedAt;
    }
}