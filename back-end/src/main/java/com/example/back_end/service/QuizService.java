package com.example.back_end.service;

import com.example.back_end.dto.request.QuizRequest;
import com.example.back_end.dto.request.QuestionRequest;
import com.example.back_end.dto.response.QuizResponse;
import com.example.back_end.entity.Class;
import com.example.back_end.entity.Quiz;
import com.example.back_end.entity.QuizQuestion;
import com.example.back_end.entity.User;
import com.example.back_end.enums.QuizType;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.ClassRepository;
import com.example.back_end.repository.QuizQuestionRepository;
import com.example.back_end.repository.QuizRepository;
import com.example.back_end.repository.UserRepository;
import com.example.back_end.repository.QuizAttemptRepository;
import com.example.back_end.repository.QuizAnswerRepository;
import com.example.back_end.entity.QuizAttempt;
import com.example.back_end.entity.QuizAnswer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    private User getCurrentUser() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public QuizResponse createQuiz(QuizRequest request) {
        log.info("Creating quiz: {}", request.getTitle());
        User creator = getCurrentUser();
        Class clazz = null;
        if (request.getClassId() != null) {
            clazz = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .topikLevel(request.getTopikLevel() != null ? request.getTopikLevel() : "NORMAL")
                .quizType(QuizType.MIXED)
                .timeLimitMins(request.getTimeLimitMins())
                .totalScore(request.getTotalScore())
                .dueDate(request.getDueDate())
                .clazz(clazz)
                .creator(creator)
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);
        return mapToQuizResponse(savedQuiz, List.of());
    }

    @Transactional
    public QuizResponse.QuestionResponse addQuestionToQuiz(Long quizId, QuestionRequest request) {
        log.info("Adding question to quiz: {}", quizId);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        List<QuizQuestion> existingQuestions = quizQuestionRepository.findByQuizId(quizId);
        int nextOrder = existingQuestions.size() + 1;

        QuizQuestion question = QuizQuestion.builder()
                .quiz(quiz)
                .questionText(request.getQuestionText())
                .questionType(request.getQuestionType() != null ? request.getQuestionType() : "MULTIPLE_CHOICE")
                .points(request.getPoints())
                .section(request.getSection())
                .audioUrl(request.getAudioUrl())
                .audioSource(request.getAudioUrl() != null && !request.getAudioUrl().isEmpty() ? "UPLOAD" : "TTS")
                .correctAnswer(request.getCorrectAnswer())
                .wrongAnswers(request.getWrongAnswers())
                .order(nextOrder)
                .build();

        QuizQuestion savedQuestion = quizQuestionRepository.save(question);
        return mapToQuestionResponse(savedQuestion);
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizDetails(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByOrderAsc(quizId);
        return mapToQuizResponse(quiz, questions);
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByClass(Long classId) {
        return quizRepository.findByClazzId(classId).stream()
                .map(quiz -> {
                    List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByOrderAsc(quiz.getId());
                    return mapToQuizResponse(quiz, questions);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getMyCreatedQuizzes() {
        User creator = getCurrentUser();
        return quizRepository.findByCreatorId(creator.getId()).stream()
                .map(quiz -> {
                    List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByOrderAsc(quiz.getId());
                    return mapToQuizResponse(quiz, questions);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteQuestion(Long quizId, Long questionId) {
        QuizQuestion question = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (!question.getQuiz().getId().equals(quizId)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        quizQuestionRepository.delete(question);
    }

    @Transactional
    public QuizResponse updateQuiz(Long id, QuizRequest request) {
        log.info("Updating quiz: {}", id);
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Class clazz = null;
        if (request.getClassId() != null) {
            clazz = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        quiz.setTitle(request.getTitle());
        quiz.setTopikLevel(request.getTopikLevel() != null ? request.getTopikLevel() : "NORMAL");
        quiz.setTimeLimitMins(request.getTimeLimitMins());
        quiz.setTotalScore(request.getTotalScore());
        quiz.setDueDate(request.getDueDate());
        quiz.setClazz(clazz);

        Quiz savedQuiz = quizRepository.save(quiz);
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByOrderAsc(id);
        return mapToQuizResponse(savedQuiz, questions);
    }

    @Transactional
    public void deleteQuiz(Long id) {
        log.info("Deleting quiz: {}", id);
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Delete student answers for attempts of this quiz
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(id);
        for (QuizAttempt attempt : attempts) {
            List<QuizAnswer> answers = quizAnswerRepository.findByAttemptId(attempt.getId());
            quizAnswerRepository.deleteAll(answers);
        }

        // Delete attempts
        quizAttemptRepository.deleteAll(attempts);

        // Delete all questions
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(id);
        quizQuestionRepository.deleteAll(questions);

        // Delete quiz
        quizRepository.delete(quiz);
    }

    private QuizResponse mapToQuizResponse(Quiz quiz, List<QuizQuestion> questions) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .quizType(quiz.getQuizType() != null ? quiz.getQuizType().name() : null)
                .topikLevel(quiz.getTopikLevel())
                .timeLimitMins(quiz.getTimeLimitMins())
                .totalScore(quiz.getTotalScore())
                .dueDate(quiz.getDueDate())
                .createdAt(quiz.getCreatedAt())
                .questions(questions.stream().map(this::mapToQuestionResponse).collect(Collectors.toList()))
                .build();
    }

    private QuizResponse.QuestionResponse mapToQuestionResponse(QuizQuestion q) {
        return QuizResponse.QuestionResponse.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .questionType(q.getQuestionType())
                .order(q.getOrder())
                .koreanText(q.getKoreanText())
                .audioUrl(q.getAudioUrl())
                .audioSource(q.getAudioSource())
                .section(q.getSection())
                .points(q.getPoints())
                .explanation(q.getExplanation())
                .correctAnswer(q.getCorrectAnswer())
                .wrongAnswers(q.getWrongAnswers())
                .build();
    }
}
