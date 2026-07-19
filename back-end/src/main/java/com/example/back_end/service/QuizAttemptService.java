package com.example.back_end.service;

import com.example.back_end.dto.request.GradeAttemptRequest;
import com.example.back_end.dto.request.QuizSubmitRequest;
import com.example.back_end.dto.response.QuizAttemptResponse;
import com.example.back_end.entity.Quiz;
import com.example.back_end.entity.QuizAnswer;
import com.example.back_end.entity.QuizAttempt;
import com.example.back_end.entity.QuizQuestion;
import com.example.back_end.entity.User;
import com.example.back_end.entity.StudentProfile;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.QuizAnswerRepository;
import com.example.back_end.repository.QuizAttemptRepository;
import com.example.back_end.repository.QuizQuestionRepository;
import com.example.back_end.repository.QuizRepository;
import com.example.back_end.repository.UserRepository;
import com.example.back_end.repository.StudentProfileRepository;
import com.example.back_end.mapper.QuizAttemptMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final UserService userService;
    private final QuizAttemptMapper quizAttemptMapper;
    private final StudentProfileRepository studentProfileRepository;

    @Transactional
    public QuizAttemptResponse submitAttempt(Long quizId, QuizSubmitRequest request) {
        User student = userService.getCurrentUser();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);

        // Map submitted answers by questionId for fast retrieval
        Map<Long, QuizSubmitRequest.AnswerSubmit> submittedMap = request.getAnswers().stream()
                .collect(Collectors.toMap(
                        QuizSubmitRequest.AnswerSubmit::getQuestionId,
                        Function.identity(),
                        (existing, replacement) -> existing));

        // Create initial attempt record
        boolean hasWritingQuestions = questions.stream()
                .anyMatch(q -> "WRITING".equalsIgnoreCase(q.getQuestionType()));

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .startedAt(LocalDateTime.now()
                        .minusMinutes(quiz.getTimeLimitMins() != null ? quiz.getTimeLimitMins() : 10))
                .submittedAt(LocalDateTime.now())
                .totalQuestions(questions.size())
                .correctCount(0)
                .score(BigDecimal.ZERO)
                .listeningScore(BigDecimal.ZERO)
                .readingScore(BigDecimal.ZERO)
                .writingScore(BigDecimal.ZERO)
                .status(hasWritingQuestions ? "PENDING_GRADING" : "COMPLETED")
                .build();

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        BigDecimal listeningScore = BigDecimal.ZERO;
        BigDecimal readingScore = BigDecimal.ZERO;
        BigDecimal writingScore = BigDecimal.ZERO;
        int correctCount = 0;
        int totalTimeTakenMs = 0;

        List<QuizAnswer> answersToSave = new ArrayList<>();

        for (QuizQuestion question : questions) {
            QuizSubmitRequest.AnswerSubmit subAnswer = submittedMap.get(question.getId());
            String studentAnswerText = subAnswer != null ? subAnswer.getStudentAnswer() : "";
            int timeTaken = subAnswer != null ? subAnswer.getTimeTakenMs() : 0;
            totalTimeTakenMs += timeTaken;

            QuizAnswer answer = QuizAnswer.builder()
                    .attempt(savedAttempt)
                    .question(question)
                    .studentAnswer(studentAnswerText)
                    .timeTakenMs(timeTaken)
                    .build();

            if ("MULTIPLE_CHOICE".equalsIgnoreCase(question.getQuestionType())) {
                boolean isCorrect = question.getCorrectAnswer().trim().equalsIgnoreCase(studentAnswerText.trim());
                answer.setIsCorrect(isCorrect);
                BigDecimal points = isCorrect ? question.getPoints() : BigDecimal.ZERO;
                answer.setPointsEarned(points);

                if (isCorrect) {
                    correctCount++;
                }

                // Tallies
                if ("LISTENING".equalsIgnoreCase(question.getSection())) {
                    listeningScore = listeningScore.add(points);
                } else if ("READING".equalsIgnoreCase(question.getSection())) {
                    readingScore = readingScore.add(points);
                } else if ("WRITING".equalsIgnoreCase(question.getSection())) {
                    writingScore = writingScore.add(points);
                }
            } else {
                // Essay needs grading
                answer.setIsCorrect(null);
                answer.setPointsEarned(null);
            }

            answersToSave.add(answer);
        }

        List<QuizAnswer> savedAnswers = quizAnswerRepository.saveAll(answersToSave);

        // Update overall attempt stats
        BigDecimal totalScore = listeningScore.add(readingScore).add(writingScore);
        savedAttempt.setScore(totalScore);
        savedAttempt.setListeningScore(listeningScore);
        savedAttempt.setReadingScore(readingScore);
        savedAttempt.setWritingScore(writingScore);
        savedAttempt.setCorrectCount(correctCount);
        savedAttempt.setTimeTakenSecs(totalTimeTakenMs / 1000);

        QuizAttempt finalAttempt = quizAttemptRepository.save(savedAttempt);

        if ("COMPLETED".equalsIgnoreCase(finalAttempt.getStatus())) {
            int xpReward = totalScore.multiply(BigDecimal.valueOf(10)).intValue();
            if (xpReward > 0) {
                StudentProfile profile = studentProfileRepository.findByUserId(student.getId()).orElse(null);
                if (profile != null) {
                    profile.setXp(profile.getXp() + xpReward);
                    profile.setLevel((profile.getXp() / 1000) + 1);
                    studentProfileRepository.save(profile);
                    log.info("Student {} earned {} XP for completing quiz {}. Total XP: {}, Level: {}",
                            student.getUsername(), xpReward, quizId, profile.getXp(), profile.getLevel());
                }
            }
        }

        return quizAttemptMapper.toResponse(finalAttempt, savedAnswers);
    }

    @Transactional
    public QuizAttemptResponse gradeAttempt(Long attemptId, GradeAttemptRequest request) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Map<Long, QuizAnswer> answersMap = quizAnswerRepository.findByAttemptIdInWithQuestion(List.of(attemptId)).stream()
                .collect(Collectors.toMap(QuizAnswer::getId, Function.identity()));

        for (GradeAttemptRequest.AnswerGrade grade : request.getGrades()) {
            QuizAnswer answer = answersMap.get(grade.getAnswerId());
            if (answer != null) {
                answer.setPointsEarned(grade.getPointsEarned());
                answer.setFeedback(grade.getFeedback());
                // Mark correct if student earned any points
                answer.setIsCorrect(grade.getPointsEarned().compareTo(BigDecimal.ZERO) > 0);
                quizAnswerRepository.save(answer);
            }
        }

        // Recalculate attempt scores
        List<QuizAnswer> answers = quizAnswerRepository.findByAttemptIdInWithQuestion(List.of(attemptId));
        BigDecimal listeningScore = BigDecimal.ZERO;
        BigDecimal readingScore = BigDecimal.ZERO;
        BigDecimal writingScore = BigDecimal.ZERO;
        int correctCount = 0;

        for (QuizAnswer ans : answers) {
            BigDecimal points = ans.getPointsEarned() != null ? ans.getPointsEarned() : BigDecimal.ZERO;
            if (Boolean.TRUE.equals(ans.getIsCorrect())) {
                correctCount++;
            }

            if ("LISTENING".equalsIgnoreCase(ans.getQuestion().getSection())) {
                listeningScore = listeningScore.add(points);
            } else if ("READING".equalsIgnoreCase(ans.getQuestion().getSection())) {
                readingScore = readingScore.add(points);
            } else if ("WRITING".equalsIgnoreCase(ans.getQuestion().getSection())) {
                writingScore = writingScore.add(points);
            }
        }

        attempt.setListeningScore(listeningScore);
        attempt.setReadingScore(readingScore);
        attempt.setWritingScore(writingScore);
        attempt.setScore(listeningScore.add(readingScore).add(writingScore));
        attempt.setCorrectCount(correctCount);
        attempt.setStatus("COMPLETED");

        QuizAttempt gradedAttempt = quizAttemptRepository.save(attempt);

        // Reward student XP
        BigDecimal totalScore = attempt.getScore();
        if (totalScore != null) {
            int xpReward = totalScore.multiply(BigDecimal.valueOf(10)).intValue();
            if (xpReward > 0) {
                StudentProfile profile = studentProfileRepository.findByUserId(attempt.getStudent().getId()).orElse(null);
                if (profile != null) {
                    profile.setXp(profile.getXp() + xpReward);
                    profile.setLevel((profile.getXp() / 1000) + 1);
                    studentProfileRepository.save(profile);
                    log.info("Student {} earned {} XP for completing/grading quiz {}. Total XP: {}, Level: {}",
                            attempt.getStudent().getUsername(), xpReward, attempt.getQuiz().getId(), profile.getXp(), profile.getLevel());
                }
            }
        }

        return quizAttemptMapper.toResponse(gradedAttempt, answers);
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> getPendingAttempts() {
        User teacher = userService.getCurrentUser();
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizCreatorIdAndStatusWithRelations(teacher.getId(),
                "PENDING_GRADING");
        return quizAttemptMapper.toResponses(attempts);
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> getMyAttempts() {
        User student = userService.getCurrentUser();
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentIdWithRelations(student.getId());
        return quizAttemptMapper.toResponses(attempts);
    }

    @Transactional(readOnly = true)
    public QuizAttemptResponse getAttemptDetails(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        List<QuizAnswer> answers = quizAnswerRepository.findByAttemptIdInWithQuestion(List.of(attemptId));
        return quizAttemptMapper.toResponse(attempt, answers);
    }
}
