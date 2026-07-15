package com.example.back_end.service;

import com.example.back_end.dto.request.StudentCreateRequest;
import com.example.back_end.dto.request.StudentUpdateRequest;
import com.example.back_end.dto.response.StudentResponse;
import com.example.back_end.dto.response.StudentProgressResponse;
import com.example.back_end.entity.Class;
import com.example.back_end.entity.QuizAttempt;
import com.example.back_end.entity.StudentProfile;
import com.example.back_end.entity.User;
import com.example.back_end.enums.UserRole;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.mapper.StudentMapper;
import com.example.back_end.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;

    // Repositories needed for cascade deletion
    private final ClassRepository classRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final CardProgressRepository cardProgressRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final AssignedStudySetRepository assignedStudySetRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents(String search, String level, Boolean isActive) {
        List<User> students = userRepository.findStudentsWithFilters(UserRole.STUDENT, search, level, isActive);
        List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, StudentProfile> profileMap = studentIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : studentProfileRepository.findByUserIdIn(studentIds).stream()
                        .filter(p -> p.getUser() != null)
                        .collect(Collectors.toMap(p -> p.getUser().getId(), p -> p, (p1, p2) -> p1));
        return students.stream()
                .map(student -> studentMapper.toResponse(student, profileMap.get(student.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public com.example.back_end.dto.response.PageResponse<StudentResponse> getPaginatedStudents(
            int pageNo, int pageSize, String search, String level, Boolean isActive) {
        int page = Math.max(0, pageNo - 1);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, pageSize, org.springframework.data.domain.Sort.by("id").descending());

        org.springframework.data.domain.Page<User> pageResult = userRepository.findStudentsWithFiltersPage(
                UserRole.STUDENT, search, level, isActive, pageable);

        List<Long> pageStudentIds = pageResult.getContent().stream()
                .map(User::getId).collect(Collectors.toList());
        Map<Long, StudentProfile> profileMap = pageStudentIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : studentProfileRepository.findByUserIdIn(pageStudentIds).stream()
                        .filter(p -> p.getUser() != null)
                        .collect(Collectors.toMap(p -> p.getUser().getId(), p -> p, (p1, p2) -> p1));

        List<StudentResponse> items = pageResult.getContent().stream()
                .map(student -> studentMapper.toResponse(student, profileMap.get(student.getId())))
                .collect(Collectors.toList());

        return com.example.back_end.dto.response.PageResponse.<StudentResponse>builder()
                .pageNo(page + 1)
                .pageSize(pageSize)
                .totalPage(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .items(items)
                .build();
    }

    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(UserRole.STUDENT)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        StudentProfile profile = StudentProfile.builder()
                .user(savedUser)
                .goal(request.getGoal())
                .currentLevel(request.getCurrentLevel())
                .xp(0)
                .level(1)
                .streak(0)
                .build();

        studentProfileRepository.save(profile);

        return studentMapper.toResponse(savedUser, profile);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != UserRole.STUDENT) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        // Check email uniqueness if changing email
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            profile = StudentProfile.builder()
                    .user(user)
                    .xp(0)
                    .level(1)
                    .streak(0)
                    .build();
        }
        profile.setGoal(request.getGoal());
        profile.setCurrentLevel(request.getCurrentLevel());

        studentProfileRepository.save(profile);

        User saved = userRepository.save(user);
        return studentMapper.toResponse(saved, profile);
    }

    @Transactional
    public void deleteStudent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != UserRole.STUDENT) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        // 1. Delete review logs
        reviewLogRepository.deleteAll(reviewLogRepository.findByStudentId(user.getId()));

        // 2. Delete card progresses
        cardProgressRepository.deleteAll(cardProgressRepository.findByStudentId(user.getId()));

        // 3. Delete assigned study sets
        assignedStudySetRepository.deleteAll(assignedStudySetRepository.findByStudentId(user.getId()));

        // 4. Delete payments
        paymentRepository.deleteAll(paymentRepository.findByStudentId(user.getId()));

        // 5. Remove student from all classes they are enrolled in
        List<Class> studentClasses = classRepository.findByStudentsId(user.getId());
        for (Class sc : studentClasses) {
            sc.getStudents().remove(user);
            classRepository.save(sc);
        }

        // 6. Delete quiz attempts and their quiz answers
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(user.getId());
        for (QuizAttempt attempt : attempts) {
            quizAnswerRepository.deleteAll(quizAnswerRepository.findByAttemptId(attempt.getId()));
        }
        quizAttemptRepository.deleteAll(attempts);

        // 7. Finally, delete the User (which cascades and deletes StudentProfile)
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public StudentProgressResponse getStudentProgress(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Class> classes = classRepository.findByStudentsId(studentId);
        List<StudentProgressResponse.ClassInfo> classInfos = classes.stream()
                .map(c -> {
                    String name = "";
                    if (c.getTopikLevel() != null) {
                        name = c.getTopikLevel().getName();
                    } else if (c.getNotes() != null && c.getNotes().contains("|")) {
                        name = c.getNotes().split("\\|", -1)[0].trim();
                    } else {
                        name = c.getNotes();
                    }
                    return StudentProgressResponse.ClassInfo.builder()
                            .id(c.getId())
                            .name(name)
                            .courseTitle(c.getCourse() != null ? c.getCourse().getTitle() : "Khóa học")
                            .build();
                })
                .collect(Collectors.toList());

        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentIdWithRelations(studentId);
        List<StudentProgressResponse.QuizAttemptInfo> quizAttemptInfos = attempts.stream()
                .map(a -> StudentProgressResponse.QuizAttemptInfo.builder()
                        .id(a.getId())
                        .quizTitle(a.getQuiz() != null ? a.getQuiz().getTitle() : "Bài tập")
                        .score(a.getScore() != null ? a.getScore().doubleValue() : 0.0)
                        .status(a.getStatus())
                        .submittedAt(a.getSubmittedAt() != null ? a.getSubmittedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());

        // Fix N+2: use aggregate DB queries instead of lazy-traversing card -> studySet per row
        List<com.example.back_end.entity.CardProgress> cardProgresses = cardProgressRepository.findByStudentId(studentId);
        int totalLearned = (int) cardProgresses.stream().filter(cp -> cp.getRepetitions() > 0).count();
        int totalReview  = (int) cardProgresses.stream().filter(cp -> cp.getState() == com.example.back_end.enums.CardState.REVIEW).count();
        // Query distinct studySet IDs directly — avoids N+N lazy proxy traversals
        int totalSets = cardProgressRepository.findDistinctStudySetIdsByStudentId(studentId).size();

        return StudentProgressResponse.builder()
                .classes(classInfos)
                .quizAttempts(quizAttemptInfos)
                .vocabProgress(StudentProgressResponse.VocabProgressInfo.builder()
                        .totalLearnedCards(totalLearned)
                        .totalReviewCards(totalReview)
                        .totalStudySets(totalSets)
                        .build())
                .build();
    }
}
