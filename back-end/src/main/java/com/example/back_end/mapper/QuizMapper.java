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
        List<Object[]> counts = quizQuestionRepository.countQuestionsByQuizIds(quizIds);
        java.util.Map<Long, Long> countMap = counts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return quizzes.stream()
                .map(quiz -> {
                    QuizResponse res = toResponse(quiz, null);
                    res.setQuestionCount(countMap.getOrDefault(quiz.getId(), 0L).intValue());
                    return res;
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
                .topikLevelId(quiz.getTopikLevel() != null ? quiz.getTopikLevel().getId() : null)
                .timeLimitMins(quiz.getTimeLimitMins())
                .totalScore(quiz.getTotalScore())
                .dueDate(quiz.getDueDate())
                .createdAt(quiz.getCreatedAt())
                .questions(questions != null ? questions.stream().map(this::toQuestionResponse).collect(Collectors.toList()) : List.of())
                .questionCount(questions != null ? questions.size() : 0)
                .classId(quiz.getClazz() != null ? quiz.getClazz().getId() : null)
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
