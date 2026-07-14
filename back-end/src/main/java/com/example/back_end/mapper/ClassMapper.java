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

    public ClassResponse toResponse(Class c) {
        if (c == null) return null;

        java.util.Map<Long, List<CardProgress>> progressMap = java.util.Map.of();
        java.util.Map<Long, List<QuizAttempt>> attemptsMap = java.util.Map.of();
        long totalCards = 0;

        if (c.getStudents() != null && !c.getStudents().isEmpty()) {
            List<Long> studentIds = c.getStudents().stream().map(student -> student.getId()).collect(Collectors.toList());
            List<CardProgress> progressList = cardProgressRepository.findByStudentIdIn(studentIds);
            List<QuizAttempt> attemptsList = quizAttemptRepository.findByStudentIdInWithQuiz(studentIds);

            progressMap = progressList.stream().collect(Collectors.groupingBy(p -> p.getStudent().getId()));
            attemptsMap = attemptsList.stream().collect(Collectors.groupingBy(a -> a.getStudent().getId()));
            totalCards = cardRepository.count();
        }

        return toResponse(c, progressMap, attemptsMap, totalCards);
    }

    public ClassResponse toResponse(
            Class c,
            java.util.Map<Long, List<CardProgress>> progressMap,
            java.util.Map<Long, List<QuizAttempt>> attemptsMap,
            long totalCards
    ) {
        if (c == null) return null;

        String name = "";
        String schedule = "";
        String room = "";
        String notes = c.getNotes();

        if (c.getTopikLevel() != null) {
            name = c.getTopikLevel().getName();
            schedule = c.getSchedule() != null ? c.getSchedule() : "";
            room = c.getRoom() != null ? c.getRoom() : "";
        } else {
            // Backward compatibility fallback for legacy notes-encoded classes
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
        }

        String randomCode = "KOR-" + c.getId() + "-" + (100 + c.getId() * 3);

        List<ClassResponse.StudentDto> studentDtos = List.of();
        if (c.getStudents() != null) {
            studentDtos = c.getStudents().stream()
                .map(student -> {
                    Long studentId = student.getId();
                    
                    // vocabProgress calculation based on repetitions > 0
                    List<CardProgress> progressList = progressMap.getOrDefault(studentId, List.of());
                    long learnedCount = progressList.stream().filter(p -> p.getRepetitions() > 0).count();
                    int vocabProgress = totalCards > 0 ? (int) (learnedCount * 100 / totalCards) : 0;
                    
                    // avgScore calculation as overall percentage of quiz completion
                    List<QuizAttempt> attempts = attemptsMap.getOrDefault(studentId, List.of());
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
                        .name(student.getFullName() != null ? student.getFullName() : student.getUsername())
                        .email(student.getEmail())
                        .vocabProgress(vocabProgress)
                        .avgScore(avgScore)
                        .build();
                })
                .collect(Collectors.toList());
        }

        Long courseId = c.getCourse() != null ? c.getCourse().getId() : null;
        String courseTitle = c.getCourse() != null ? c.getCourse().getTitle() : null;

        return ClassResponse.builder()
                .id(c.getId())
                .courseId(courseId)
                .courseTitle(courseTitle)
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

    public List<ClassResponse> toResponses(List<Class> classes) {
        if (classes == null) return List.of();

        List<Long> studentIds = classes.stream()
                .flatMap(c -> c.getStudents() != null ? c.getStudents().stream() : java.util.stream.Stream.empty())
                .map(student -> student.getId())
                .distinct()
                .collect(Collectors.toList());

        java.util.Map<Long, List<CardProgress>> progressMap = java.util.Map.of();
        java.util.Map<Long, List<QuizAttempt>> attemptsMap = java.util.Map.of();
        long totalCards = 0;

        if (!studentIds.isEmpty()) {
            List<CardProgress> allProgress = cardProgressRepository.findByStudentIdIn(studentIds);
            List<QuizAttempt> allAttempts = quizAttemptRepository.findByStudentIdInWithQuiz(studentIds);

            progressMap = allProgress.stream().collect(Collectors.groupingBy(p -> p.getStudent().getId()));
            attemptsMap = allAttempts.stream().collect(Collectors.groupingBy(a -> a.getStudent().getId()));
            totalCards = cardRepository.count();
        }

        final java.util.Map<Long, List<CardProgress>> finalProgressMap = progressMap;
        final java.util.Map<Long, List<QuizAttempt>> finalAttemptsMap = attemptsMap;
        final long finalTotalCards = totalCards;

        return classes.stream()
                .map(c -> toResponse(c, finalProgressMap, finalAttemptsMap, finalTotalCards))
                .collect(Collectors.toList());
    }
}
