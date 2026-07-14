package com.example.back_end.mapper;

import com.example.back_end.dto.response.QuizResponse;
import com.example.back_end.entity.Quiz;
import com.example.back_end.entity.QuizQuestion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import com.example.back_end.repository.QuizQuestionRepository;

@Component
@RequiredArgsConstructor
public class QuizMapper {

    private final QuizQuestionRepository quizQuestionRepository;

    public List<QuizResponse> toResponses(List<Quiz> quizzes) {
        if (quizzes == null || quizzes.isEmpty()) return List.of();

        List<Long> quizIds = quizzes.stream().map(Quiz::getId).collect(Collectors.toList());
        List<QuizQuestion> allQuestions = quizQuestionRepository.findByQuizIdInOrderByQuizIdAscOrderAsc(quizIds);
        java.util.Map<Long, List<QuizQuestion>> questionsByQuizMap = allQuestions.stream()
                .collect(Collectors.groupingBy(q -> q.getQuiz().getId()));

        return quizzes.stream()
                .map(quiz -> {
                    List<QuizQuestion> questions = questionsByQuizMap.getOrDefault(quiz.getId(), List.of());
                    return toResponse(quiz, questions);
                })
                .collect(Collectors.toList());
    }

    public QuizResponse toResponse(Quiz quiz, List<QuizQuestion> questions) {
        if (quiz == null) return null;
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .quizType(quiz.getQuizType() != null ? quiz.getQuizType().name() : null)
                .topikLevel(quiz.getTopikLevel() != null ? quiz.getTopikLevel().getName() : null)
                .timeLimitMins(quiz.getTimeLimitMins())
                .totalScore(quiz.getTotalScore())
                .dueDate(quiz.getDueDate())
                .createdAt(quiz.getCreatedAt())
                .questions(questions != null ? questions.stream().map(this::toQuestionResponse).collect(Collectors.toList()) : List.of())
                .build();
    }

    public QuizResponse.QuestionResponse toQuestionResponse(QuizQuestion q) {
        if (q == null) return null;
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
