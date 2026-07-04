package com.example.back_end.mapper;

import com.example.back_end.dto.response.ClassResponse;
import com.example.back_end.entity.Class;
import com.example.back_end.entity.CardProgress;
import com.example.back_end.entity.QuizAttempt;
import com.example.back_end.repository.CardProgressRepository;
import com.example.back_end.repository.CardRepository;
import com.example.back_end.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClassMapper {

    private final CardProgressRepository cardProgressRepository;
    private final CardRepository cardRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public ClassResponse toResponse(Class c, List<Class> enrollments) {
        if (c == null) return null;

        String name = "";
        String schedule = "";
        String room = "";
        String notes = c.getNotes();

        if (notes != null && notes.contains("|")) {
            String[] parts = notes.split("\\|", -1);
            if (parts.length >= 3) {
                name = parts[0].trim();
                schedule = parts[1].trim();
                room = parts[2].trim();
                notes = parts.length > 3 ? parts[3].trim() : "";
            }
        } else if (notes != null) {
            name = notes;
        }

        String randomCode = "KOR-" + c.getId() + "-" + (100 + c.getId() * 3);

        List<ClassResponse.StudentDto> studentDtos = List.of();
        if (enrollments != null) {
            long totalCards = cardRepository.count();
            studentDtos = enrollments.stream()
                .filter(e -> e.getStudent() != null)
                .map(e -> {
                    Long studentId = e.getStudent().getId();
                    
                    // vocabProgress calculation based on repetitions > 0
                    List<CardProgress> progressList = cardProgressRepository.findByStudentId(studentId);
                    long learnedCount = progressList.stream().filter(p -> p.getRepetitions() > 0).count();
                    int vocabProgress = totalCards > 0 ? (int) (learnedCount * 100 / totalCards) : 0;
                    
                    // avgScore calculation as overall percentage of quiz completion
                    List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(studentId);
                    double averagePercentage = attempts.stream()
                        .filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus()))
                        .mapToDouble(a -> {
                            double earned = a.getScore() != null ? a.getScore().doubleValue() : 0.0;
                            double total = a.getQuiz() != null && a.getQuiz().getTotalScore() != null ? a.getQuiz().getTotalScore() : 10.0;
                            return total > 0 ? (earned * 100.0 / total) : 0.0;
                        })
                        .average()
                        .orElse(0.0);
                    int avgScore = (int) Math.round(averagePercentage);

                    return ClassResponse.StudentDto.builder()
                        .id(studentId)
                        .name(e.getStudent().getFullName() != null ? e.getStudent().getFullName() : e.getStudent().getUsername())
                        .email(e.getStudent().getEmail())
                        .vocabProgress(vocabProgress)
                        .avgScore(avgScore)
                        .build();
                })
                .collect(Collectors.toList());
        }

        return ClassResponse.builder()
                .id(c.getId())
                .name(name)
                .schedule(schedule)
                .room(room)
                .code(randomCode)
                .teacherName(c.getTeacher().getFullName() != null ? c.getTeacher().getFullName() : c.getTeacher().getUsername())
                .studentsCount(studentDtos.size())
                .status(c.getStatus() != null ? c.getStatus().name() : "ACTIVE")
                .startedAt(c.getStartedAt())
                .notes(notes)
                .students(studentDtos)
                .build();
    }
}
