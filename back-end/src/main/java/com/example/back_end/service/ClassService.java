package com.example.back_end.service;

import com.example.back_end.dto.request.ClassRequest;
import com.example.back_end.dto.response.ClassResponse;
import com.example.back_end.entity.Class;
import com.example.back_end.entity.User;
import com.example.back_end.enums.ClassStatus;
import com.example.back_end.enums.UserRole;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.mapper.ClassMapper;
import com.example.back_end.repository.ClassRepository;
import com.example.back_end.repository.UserRepository;
import com.example.back_end.repository.TopikLevelRepository;
import com.example.back_end.repository.CourseRepository;
import com.example.back_end.entity.Course;
import com.example.back_end.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public PageResponse<ClassResponse> getTeacherClassesPaginated(int pageNo, int pageSize, String search,
            String level) {
        User teacher = userService.getCurrentUser();
        int page = Math.max(0, pageNo - 1);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").descending());

        Page<Class> pageResult = classRepository.findByTeacherIdWithFiltersPage(teacher.getId(), search, level,
                pageable);

        List<Class> classes = pageResult.getContent();
        if (!classes.isEmpty()) {
            List<Long> classIds = classes.stream().map(Class::getId).collect(Collectors.toList());
            classRepository.fetchStudentsForClasses(classIds);
        }

        List<ClassResponse> items = classMapper.toResponses(classes);

        return PageResponse.<ClassResponse>builder()
                .pageNo(page + 1)
                .pageSize(pageSize)
                .totalPage(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .items(items)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ClassResponse> getTeacherClassesWithFilters(String search, String level) {
        User teacher = userService.getCurrentUser();
        List<Class> classes = classRepository.findByTeacherIdWithFilters(teacher.getId(), search, level);
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
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND));

        Course course = null;
        if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new AppException(
                            ErrorCode.RESOURCE_NOT_FOUND));
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
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.USER_NOT_FOUND));

        if (student.getRole() != UserRole.STUDENT) {
            throw new AppException(ErrorCode.INVALID_KEY);
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
    public ClassResponse updateClass(Long id, ClassRequest request) {
        User teacher = userService.getCurrentUser();
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Validate authority: Only class teacher or ADMIN can update the class
        if (teacher.getRole() != UserRole.TEACHER && !clazz.getTeacher().getId().equals(teacher.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        com.example.back_end.entity.TopikLevel topikLevel = topikLevelRepository.findById(request.getTopikLevelId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Course course = null;
        if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        clazz.setTopikLevel(topikLevel);
        clazz.setCourse(course);
        clazz.setSchedule(request.getSchedule());
        clazz.setRoom(request.getRoom());
        clazz.setNotes(request.getNotes());

        if (request.getStatus() != null) {
            try {
                clazz.setStatus(ClassStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Keep default or current status if invalid status is sent
            }
        }

        Class updated = classRepository.save(clazz);
        return classMapper.toResponse(updated);
    }

    @Transactional
    public void deleteClass(Long id) {
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND));

        // Since it is @ManyToMany, clear the list of students to remove rows in the
        // join table
        clazz.getStudents().clear();
        classRepository.delete(clazz);
    }
}
