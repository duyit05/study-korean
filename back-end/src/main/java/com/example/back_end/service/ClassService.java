package com.example.back_end.service;

import com.example.back_end.dto.request.ClassRequest;
import com.example.back_end.dto.response.ClassResponse;
import com.example.back_end.entity.Class;
import com.example.back_end.entity.User;
import com.example.back_end.enums.ClassStatus;
import com.example.back_end.mapper.ClassMapper;
import com.example.back_end.repository.ClassRepository;
import com.example.back_end.repository.UserRepository;
import com.example.back_end.repository.TopikLevelRepository;
import com.example.back_end.repository.CourseRepository;
import com.example.back_end.entity.Course;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassService {

    private final ClassRepository classRepository;
    private final UserService userService;
    private final ClassMapper classMapper;
    private final UserRepository userRepository;
    private final TopikLevelRepository topikLevelRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<ClassResponse> getTeacherClasses() {
        User teacher = userService.getCurrentUser();
        List<Class> classes = classRepository.findByTeacherIdWithRelations(teacher.getId());
        return classMapper.toResponses(classes);
    }

    @Transactional(readOnly = true)
    public List<ClassResponse> getStudentClasses() {
        User student = userService.getCurrentUser();
        List<Class> classes = classRepository.findByStudentsIdWithRelations(student.getId());
        return classMapper.toResponses(classes);
    }

    @Transactional
    public ClassResponse createClass(ClassRequest request) {
        User teacher = userService.getCurrentUser();

        com.example.back_end.entity.TopikLevel topikLevel = topikLevelRepository.findById(request.getTopikLevelId())
                .orElseThrow(() -> new com.example.back_end.exception.AppException(
                        com.example.back_end.exception.ErrorCode.RESOURCE_NOT_FOUND));

        Course course = null;
        if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new com.example.back_end.exception.AppException(
                            com.example.back_end.exception.ErrorCode.RESOURCE_NOT_FOUND));
        }

        Class clazz = Class.builder()
                .teacher(teacher)
                .topikLevel(topikLevel)
                .course(course)
                .schedule(request.getSchedule())
                .room(request.getRoom())
                .status(ClassStatus.ACTIVE)
                .startedAt(LocalDate.now())
                .notes(request.getNotes())
                .build();
        Class saved = classRepository.save(clazz);
        return classMapper.toResponse(saved);
    }

    @Transactional
    public ClassResponse enrollStudent(Long classId, Long studentId) {
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new com.example.back_end.exception.AppException(
                        com.example.back_end.exception.ErrorCode.RESOURCE_NOT_FOUND));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new com.example.back_end.exception.AppException(
                        com.example.back_end.exception.ErrorCode.USER_NOT_FOUND));

        if (student.getRole() != com.example.back_end.enums.UserRole.STUDENT) {
            throw new com.example.back_end.exception.AppException(com.example.back_end.exception.ErrorCode.INVALID_KEY);
        }

        // Check if student is already enrolled in this class
        boolean exists = clazz.getStudents().stream().anyMatch(s -> s.getId().equals(studentId));
        if (!exists) {
            clazz.getStudents().add(student);
            classRepository.save(clazz);
        }

        return classMapper.toResponse(clazz);
    }

    @Transactional
    public void deleteClass(Long id) {
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new com.example.back_end.exception.AppException(
                        com.example.back_end.exception.ErrorCode.RESOURCE_NOT_FOUND));

        // Since it is @ManyToMany, clear the list of students to remove rows in the
        // join table
        clazz.getStudents().clear();
        classRepository.delete(clazz);
    }
}
