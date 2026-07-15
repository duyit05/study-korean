package com.example.back_end.service;

import com.example.back_end.dto.request.AssignStudySetRequest;
import com.example.back_end.dto.response.ClassResponse;
import com.example.back_end.entity.AssignedStudySet;
import com.example.back_end.entity.Class;
import com.example.back_end.entity.StudySet;
import com.example.back_end.entity.User;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.AssignedStudySetRepository;
import com.example.back_end.repository.ClassRepository;
import com.example.back_end.repository.StudySetRepository;
import com.example.back_end.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignedStudySetService {

    private final AssignedStudySetRepository assignedStudySetRepository;
    private final ClassRepository classRepository;
    private final StudySetRepository studySetRepository;
    private final CardRepository cardRepository;
    private final UserService userService;

    @Transactional
    public ClassResponse.AssignedStudySetDto assignStudySet(AssignStudySetRequest request) {
        Class clazz = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        StudySet studySet = studySetRepository.findById(request.getStudySetId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User currentUser = userService.getCurrentUser();

        AssignedStudySet assignment = AssignedStudySet.builder()
                .clazz(clazz)
                .studySet(studySet)
                .dueDate(request.getDueDate())
                .note(request.getNote())
                .assignedBy(currentUser)
                .build();

        AssignedStudySet saved = assignedStudySetRepository.save(assignment);
        long wordCount = cardRepository.countByStudySetId(studySet.getId());

        return ClassResponse.AssignedStudySetDto.builder()
                .id(saved.getId())
                .studySetId(studySet.getId())
                .studySetTitle(studySet.getTitle())
                .studySetDescription(studySet.getDescription())
                .wordCount((int) wordCount)
                .dueDate(saved.getDueDate() != null ? saved.getDueDate().toString() : null)
                .note(saved.getNote())
                .assignedAt(java.time.LocalDateTime.now().toString())
                .build();
    }

    @Transactional
    public void unassignStudySet(Long id) {
        AssignedStudySet assignment = assignedStudySetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        assignedStudySetRepository.delete(assignment);
    }
}
