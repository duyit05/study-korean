package com.example.back_end.service;

import com.example.back_end.dto.request.CourseRequest;
import com.example.back_end.dto.response.CourseResponse;
import com.example.back_end.entity.Course;
import com.example.back_end.entity.User;
import com.example.back_end.enums.UserRole;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.mapper.CourseMapper;
import com.example.back_end.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final UserService userService;

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        User user = userService.getCurrentUser();
        if (user.getRole() != UserRole.TEACHER && user.getRole() != UserRole.ADMIN) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .level(request.getLevel())
                .totalSessions(request.getTotalSessions())
                .price(request.getPrice())
                .thumbnailUrl(request.getThumbnailUrl())
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : false)
                .teacher(user)
                .build();

        Course saved = courseRepository.save(course);
        return courseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return courseMapper.toResponse(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        User user = userService.getCurrentUser();
        List<Course> courses;

        if (user.getRole() == UserRole.ADMIN) {
            courses = courseRepository.findAllWithTeacher();
        } else if (user.getRole() == UserRole.TEACHER) {
            courses = courseRepository.findByTeacherIdWithTeacher(user.getId());
        } else { // STUDENT or other
            courses = courseRepository.findByIsPublishedTrueWithTeacher();
        }

        return courses.stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacherIdWithTeacher(teacherId).stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        User user = userService.getCurrentUser();
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Check if the user is the teacher who created the course, or is an Admin
        if (user.getRole() != UserRole.ADMIN && !course.getTeacher().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setLevel(request.getLevel());
        course.setTotalSessions(request.getTotalSessions());
        course.setPrice(request.getPrice());
        course.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getIsPublished() != null) {
            course.setIsPublished(request.getIsPublished());
        }

        Course updated = courseRepository.save(course);
        return courseMapper.toResponse(updated);
    }

    @Transactional
    public void deleteCourse(Long id) {
        User user = userService.getCurrentUser();
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Check if the user is the teacher who created the course, or is an Admin
        if (user.getRole() != UserRole.ADMIN && !course.getTeacher().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        courseRepository.delete(course);
    }
}
