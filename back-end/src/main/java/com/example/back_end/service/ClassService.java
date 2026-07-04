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

    @Transactional(readOnly = true)
    public List<ClassResponse> getTeacherClasses() {
        User teacher = userService.getCurrentUser();
        // Return only classes where student is null (master class rows)
        return classRepository.findByTeacherIdAndStudentIsNull(teacher.getId()).stream()
                .map(clazz -> {
                    // Fetch all actual student enrollment rows for this class code/notes
                    List<Class> enrollments;
                    if (clazz.getTopikLevel() != null) {
                        enrollments = classRepository.findByTeacherIdAndTopikLevelIdAndScheduleAndRoomAndStudentIsNotNull(
                            teacher.getId(), clazz.getTopikLevel().getId(), clazz.getSchedule(), clazz.getRoom());
                    } else {
                        enrollments = classRepository.findByTeacherIdAndNotesAndStudentIsNotNull(teacher.getId(), clazz.getNotes());
                    }
                    return classMapper.toResponse(clazz, enrollments);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassResponse> getStudentClasses() {
        User student = userService.getCurrentUser();
        return classRepository.findByStudentId(student.getId()).stream()
                .map(clazz -> {
                    List<Class> enrollments;
                    if (clazz.getTopikLevel() != null) {
                        enrollments = classRepository.findByTeacherIdAndTopikLevelIdAndScheduleAndRoomAndStudentIsNotNull(
                            clazz.getTeacher().getId(), clazz.getTopikLevel().getId(), clazz.getSchedule(), clazz.getRoom());
                    } else {
                        enrollments = classRepository.findByTeacherIdAndNotesAndStudentIsNotNull(clazz.getTeacher().getId(), clazz.getNotes());
                    }
                    return classMapper.toResponse(clazz, enrollments);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ClassResponse createClass(ClassRequest request) {
        User teacher = userService.getCurrentUser();
        log.info("Creating class with topikLevelId: {} for teacher: {}", request.getTopikLevelId(), teacher.getUsername());
        
        com.example.back_end.entity.TopikLevel topikLevel = topikLevelRepository.findById(request.getTopikLevelId())
                .orElseThrow(() -> new com.example.back_end.exception.AppException(com.example.back_end.exception.ErrorCode.RESOURCE_NOT_FOUND));

        Class clazz = Class.builder()
                .teacher(teacher)
                .topikLevel(topikLevel)
                .schedule(request.getSchedule())
                .room(request.getRoom())
                .status(ClassStatus.ACTIVE)
                .startedAt(LocalDate.now())
                .notes(request.getNotes())
                .build();
        Class saved = classRepository.save(clazz);
        return classMapper.toResponse(saved, List.of());
    }

    @Transactional
    public ClassResponse enrollStudent(Long classId, Long studentId) {
        Class masterClass = classRepository.findById(classId)
                .orElseThrow(() -> new com.example.back_end.exception.AppException(com.example.back_end.exception.ErrorCode.RESOURCE_NOT_FOUND));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new com.example.back_end.exception.AppException(com.example.back_end.exception.ErrorCode.USER_NOT_FOUND));

        if (student.getRole() != com.example.back_end.enums.UserRole.STUDENT) {
            throw new com.example.back_end.exception.AppException(com.example.back_end.exception.ErrorCode.INVALID_KEY);
        }

        // Check if student is already enrolled in this class
        boolean exists;
        List<Class> enrollments;
        if (masterClass.getTopikLevel() != null) {
            enrollments = classRepository.findByTeacherIdAndTopikLevelIdAndScheduleAndRoomAndStudentIsNotNull(
                masterClass.getTeacher().getId(), masterClass.getTopikLevel().getId(), masterClass.getSchedule(), masterClass.getRoom());
            exists = enrollments.stream().anyMatch(c -> c.getStudent().getId().equals(studentId));
        } else {
            enrollments = classRepository.findByTeacherIdAndNotesAndStudentIsNotNull(masterClass.getTeacher().getId(), masterClass.getNotes());
            exists = enrollments.stream().anyMatch(c -> c.getStudent().getId().equals(studentId));
        }

        if (exists) {
            return classMapper.toResponse(masterClass, enrollments);
        }

        Class enrollment = Class.builder()
                .teacher(masterClass.getTeacher())
                .student(student)
                .topikLevel(masterClass.getTopikLevel())
                .schedule(masterClass.getSchedule())
                .room(masterClass.getRoom())
                .status(ClassStatus.ACTIVE)
                .startedAt(LocalDate.now())
                .notes(masterClass.getNotes())
                .build();

        classRepository.save(enrollment);

        List<Class> updatedEnrollments;
        if (masterClass.getTopikLevel() != null) {
            updatedEnrollments = classRepository.findByTeacherIdAndTopikLevelIdAndScheduleAndRoomAndStudentIsNotNull(
                masterClass.getTeacher().getId(), masterClass.getTopikLevel().getId(), masterClass.getSchedule(), masterClass.getRoom());
        } else {
            updatedEnrollments = classRepository.findByTeacherIdAndNotesAndStudentIsNotNull(masterClass.getTeacher().getId(), masterClass.getNotes());
        }
        return classMapper.toResponse(masterClass, updatedEnrollments);
    }

    @Transactional
    public void deleteClass(Long id) {
        log.info("Deleting class: {}", id);
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new com.example.back_end.exception.AppException(com.example.back_end.exception.ErrorCode.RESOURCE_NOT_FOUND));
        
        // Also delete any student enrollment records that have the same teacher and matching identifiers
        List<Class> enrollments;
        if (clazz.getTopikLevel() != null) {
            enrollments = classRepository.findByTeacherIdAndTopikLevelIdAndScheduleAndRoomAndStudentIsNotNull(
                clazz.getTeacher().getId(), clazz.getTopikLevel().getId(), clazz.getSchedule(), clazz.getRoom());
        } else {
            enrollments = classRepository.findByTeacherIdAndNotesAndStudentIsNotNull(clazz.getTeacher().getId(), clazz.getNotes());
        }
        classRepository.deleteAll(enrollments);
        
        classRepository.delete(clazz);
    }
}
