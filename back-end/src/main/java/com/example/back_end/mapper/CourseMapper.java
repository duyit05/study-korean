package com.example.back_end.mapper;

import com.example.back_end.dto.response.CourseResponse;
import com.example.back_end.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseResponse toResponse(Course course) {
        if (course == null) return null;

        Long teacherId = null;
        String teacherName = null;

        if (course.getTeacher() != null) {
            teacherId = course.getTeacher().getId();
            teacherName = course.getTeacher().getFullName();
        }

        return CourseResponse.builder()
                .id(course.getId())
                .teacherId(teacherId)
                .teacherName(teacherName)
                .title(course.getTitle())
                .description(course.getDescription())
                .level(course.getLevel())
                .totalSessions(course.getTotalSessions())
                .price(course.getPrice())
                .thumbnailUrl(course.getThumbnailUrl())
                .isPublished(course.getIsPublished())
                .createdAt(course.getCreatedAt())
                .build();
    }
}
