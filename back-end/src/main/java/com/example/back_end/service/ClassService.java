package com.example.back_end.service;

import com.example.back_end.dto.request.ClassRequest;
import com.example.back_end.dto.response.ClassResponse;
import com.example.back_end.entity.Class;
import com.example.back_end.entity.User;
import com.example.back_end.enums.ClassStatus;
import com.example.back_end.mapper.ClassMapper;
import com.example.back_end.repository.ClassRepository;
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

    @Transactional(readOnly = true)
    public List<ClassResponse> getTeacherClasses() {
        User teacher = userService.getCurrentUser();
        return classRepository.findByTeacherId(teacher.getId()).stream()
                .map(classMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassResponse> getStudentClasses() {
        User student = userService.getCurrentUser();
        return classRepository.findByStudentId(student.getId()).stream()
                .map(classMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClassResponse createClass(ClassRequest request) {
        User teacher = userService.getCurrentUser();
        log.info("Creating class: {} for teacher: {}", request.getName(), teacher.getUsername());
        Class clazz = Class.builder()
                .teacher(teacher)
                .status(ClassStatus.ACTIVE)
                .startedAt(LocalDate.now())
                .notes(request.getName() + " | " + request.getSchedule() + " | " + request.getRoom() + " | " + request.getNotes())
                .build();
        Class saved = classRepository.save(clazz);
        return classMapper.toResponse(saved);
    }

    @Transactional
    public void deleteClass(Long id) {
        log.info("Deleting class: {}", id);
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new com.example.back_end.exception.AppException(com.example.back_end.exception.ErrorCode.RESOURCE_NOT_FOUND));
        classRepository.delete(clazz);
    }
}
