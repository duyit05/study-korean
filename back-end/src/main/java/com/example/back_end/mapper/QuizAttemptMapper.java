package com.example.back_end.mapper;

import com.example.back_end.dto.response.QuizAttemptResponse;
import com.example.back_end.entity.QuizAttempt;
import com.example.back_end.entity.QuizAnswer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import com.example.back_end.repository.QuizAnswerRepository;

@Component
@RequiredArgsConstructor
public class QuizAttemptMapper {

    private final QuizAnswerRepository quizAnswerRepository;

    public List<QuizAttemptResponse> toResponses(List<QuizAttempt> attempts) {
        if (attempts == null || attempts.isEmpty()) return List.of();

        List<Long> attemptIds = attempts.stream().map(QuizAttempt::getId).collect(Collectors.toList());
        List<QuizAnswer> allAnswers = quizAnswerRepository.findByAttemptIdInWithQuestion(attemptIds);
        java.util.Map<Long, List<QuizAnswer>> answersByAttemptMap = allAnswers.stream()
                .collect(Collectors.groupingBy(ans -> ans.getAttempt().getId()));

        return attempts.stream()
                .map(attempt -> {
                    List<QuizAnswer> answers = answersByAttemptMap.getOrDefault(attempt.getId(), List.of());
                    return toResponse(attempt, answers);
                })
                .collect(Collectors.toList());
    }

    public QuizAttemptResponse toResponse(QuizAttempt attempt, List<QuizAnswer> answers) {
        if (attempt == null) return null;
        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .studentName(attempt.getStudent().getFullName() != null ? attempt.getStudent().getFullName() : attempt.getStudent().getUsername())
                .score(attempt.getScore())
                .totalQuestions(attempt.getTotalQuestions())
                .correctCount(attempt.getCorrectCount())
                .timeTakenSecs(attempt.getTimeTakenSecs())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .listeningScore(attempt.getListeningScore())
                .readingScore(attempt.getReadingScore())
                .writingScore(attempt.getWritingScore())
                .status(attempt.getStatus())
                .answers(answers != null ? answers.stream().map(this::toAnswerResponse).collect(Collectors.toList()) : List.of())
                .build();
    }

    public QuizAttemptResponse.AnswerResponse toAnswerResponse(QuizAnswer ans) {
        if (ans == null) return null;
        return QuizAttemptResponse.AnswerResponse.builder()
                .id(ans.getId())
                .questionId(ans.getQuestion().getId())
                .questionText(ans.getQuestion().getQuestionText())
                .questionType(ans.getQuestion().getQuestionType())
                .studentAnswer(ans.getStudentAnswer())
                .isCorrect(ans.getIsCorrect())
                .timeTakenMs(ans.getTimeTakenMs())
                .pointsEarned(ans.getPointsEarned())
                .feedback(ans.getFeedback())
                .build();
    }
}
