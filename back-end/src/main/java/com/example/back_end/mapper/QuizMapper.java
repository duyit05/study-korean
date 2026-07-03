package com.example.back_end.mapper;

import com.example.back_end.dto.response.QuizResponse;
import com.example.back_end.entity.Quiz;
import com.example.back_end.entity.QuizQuestion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuizMapper {

    public QuizResponse toResponse(Quiz quiz, List<QuizQuestion> questions) {
        if (quiz == null) return null;
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .quizType(quiz.getQuizType() != null ? quiz.getQuizType().name() : null)
                .topikLevel(quiz.getTopikLevel())
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
