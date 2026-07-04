package com.example.back_end.service;

import com.example.back_end.dto.request.StudentCreateRequest;
import com.example.back_end.dto.request.StudentUpdateRequest;
import com.example.back_end.dto.response.StudentResponse;
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
    public List<StudentResponse> getAllStudents() {
        return userRepository.findByRole(UserRole.STUDENT).stream()
                .map(studentMapper::toResponse)
                .collect(Collectors.toList());
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
        savedUser.setStudentProfile(profile);

        log.info("Student account created successfully: {}", savedUser.getEmail());
        return studentMapper.toResponse(savedUser);
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

        StudentProfile profile = user.getStudentProfile();
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
        user.setStudentProfile(profile);

        User saved = userRepository.save(user);
        log.info("Student account updated successfully: {}", saved.getEmail());
        return studentMapper.toResponse(saved);
    }

    @Transactional
    public void deleteStudent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != UserRole.STUDENT) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        log.info("Deleting student account and cascading dependencies: {}", user.getEmail());

        // 1. Delete review logs
        reviewLogRepository.deleteAll(reviewLogRepository.findByStudentId(user.getId()));

        // 2. Delete card progresses
        cardProgressRepository.deleteAll(cardProgressRepository.findByStudentId(user.getId()));

        // 3. Delete assigned study sets
        assignedStudySetRepository.deleteAll(assignedStudySetRepository.findByStudentId(user.getId()));

        // 4. Delete payments
        paymentRepository.deleteAll(paymentRepository.findByStudentId(user.getId()));

        // 5. Delete student classes
        classRepository.deleteAll(classRepository.findByStudentId(user.getId()));

        // 6. Delete quiz attempts and their quiz answers
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(user.getId());
        for (QuizAttempt attempt : attempts) {
            quizAnswerRepository.deleteAll(quizAnswerRepository.findByAttemptId(attempt.getId()));
        }
        quizAttemptRepository.deleteAll(attempts);

        // 7. Finally, delete the User (which cascades and deletes StudentProfile)
        userRepository.delete(user);
    }
}
