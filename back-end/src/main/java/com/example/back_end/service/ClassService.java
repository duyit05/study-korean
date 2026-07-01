package com.example.back_end.service;

import com.example.back_end.entity.Class;
import com.example.back_end.entity.User;
import com.example.back_end.enums.ClassStatus;
import com.example.back_end.repository.ClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassService {

    private final ClassRepository classRepository;

    @Transactional(readOnly = true)
    public List<Class> getClassesByTeacher(Long teacherId) {
        return classRepository.findByTeacherId(teacherId);
    }

    @Transactional(readOnly = true)
    public List<Class> getClassesByStudent(Long studentId) {
        return classRepository.findByStudentId(studentId);
    }

    @Transactional
    public Class createClass(String name, String schedule, String room, String notes, User teacher) {
        log.info("Creating class: {} for teacher: {}", name, teacher.getUsername());
        // Since the Class entity has a student field but models simple classes, we can support notes / name in notes
        Class clazz = Class.builder()
                .teacher(teacher)
                .status(ClassStatus.ACTIVE)
                .startedAt(LocalDate.now())
                .notes(name + " | " + schedule + " | " + room + " | " + notes)
                .build();
        return classRepository.save(clazz);
    }

    @Transactional
    public void deleteClass(Long id) {
        log.info("Deleting class: {}", id);
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new com.example.back_end.exception.AppException(com.example.back_end.exception.ErrorCode.RESOURCE_NOT_FOUND));
        classRepository.delete(clazz);
    }
}
