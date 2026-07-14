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
import com.example.back_end.repository.TopikLevelRepository;
import com.example.back_end.entity.TopikLevel;
import com.example.back_end.entity.QuizAttempt;
import com.example.back_end.entity.QuizAnswer;
import com.example.back_end.mapper.QuizMapper;
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
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final UserService userService;
    private final QuizMapper quizMapper;
    private final TopikLevelRepository topikLevelRepository;

    @Transactional
    public QuizResponse createQuiz(QuizRequest request) {
        User creator = userService.getCurrentUser();
        Class clazz = null;
        if (request.getClassId() != null) {
            clazz = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        TopikLevel level = null;
        if (request.getTopikLevelId() != null) {
            level = topikLevelRepository.findById(request.getTopikLevelId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .topikLevel(level)
                .quizType(QuizType.MIXED)
                .timeLimitMins(request.getTimeLimitMins())
                .totalScore(request.getTotalScore())
                .dueDate(request.getDueDate())
                .clazz(clazz)
                .creator(creator)
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);
        return quizMapper.toResponse(savedQuiz, List.of());
    }

    @Transactional
    public QuizResponse.QuestionResponse addQuestionToQuiz(Long quizId, QuestionRequest request) {
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
        return quizMapper.toQuestionResponse(savedQuestion);
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizDetails(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByOrderAsc(quizId);
        return quizMapper.toResponse(quiz, questions);
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByClass(Long classId) {
        List<Quiz> quizzes = quizRepository.findByClazzIdWithTopikLevel(classId);
        return quizMapper.toResponses(quizzes);
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getMyCreatedQuizzes() {
        User creator = userService.getCurrentUser();
        List<Quiz> quizzes = quizRepository.findByCreatorIdWithTopikLevel(creator.getId());
        return quizMapper.toResponses(quizzes);
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
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Class clazz = null;
        if (request.getClassId() != null) {
            clazz = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        TopikLevel level = null;
        if (request.getTopikLevelId() != null) {
            level = topikLevelRepository.findById(request.getTopikLevelId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        quiz.setTitle(request.getTitle());
        quiz.setTopikLevel(level);
        quiz.setTimeLimitMins(request.getTimeLimitMins());
        quiz.setTotalScore(request.getTotalScore());
        quiz.setDueDate(request.getDueDate());
        quiz.setClazz(clazz);

        Quiz savedQuiz = quizRepository.save(quiz);
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByOrderAsc(id);
        return quizMapper.toResponse(savedQuiz, questions);
    }

    @Transactional
    public void deleteQuiz(Long id) {
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

}
