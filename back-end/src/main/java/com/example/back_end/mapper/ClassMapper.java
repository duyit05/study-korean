package com.example.back_end.mapper;

import com.example.back_end.dto.response.ClassResponse;
import com.example.back_end.entity.Class;
import com.example.back_end.entity.CardProgress;
import com.example.back_end.entity.QuizAttempt;
import com.example.back_end.repository.CardProgressRepository;
import com.example.back_end.repository.CardRepository;
import com.example.back_end.repository.QuizAttemptRepository;
import com.example.back_end.repository.AssignedStudySetRepository;
import com.example.back_end.entity.AssignedStudySet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClassMapper {

    private final CardProgressRepository cardProgressRepository;
    private final CardRepository cardRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AssignedStudySetRepository assignedStudySetRepository;

    public ClassResponse toResponse(Class c) {
        if (c == null)
            return null;

        Map<Long, List<CardProgress>> progressMap = Map.of();
        Map<Long, List<QuizAttempt>> attemptsMap = Map.of();
        Map<Long, Long> cardCountsMap = Map.of();
        List<AssignedStudySet> assigned = List.of();
        long totalCards = 0;

        if (c.getId() != null) {
            assigned = assignedStudySetRepository.findByClazzId(c.getId());
            List<Long> studySetIds = assigned.stream()
                    .map(a -> a.getStudySet().getId())
                    .distinct()
                    .collect(Collectors.toList());
            totalCards = studySetIds.isEmpty() ? 0 : cardRepository.countByStudySetIdIn(studySetIds);

            if (!studySetIds.isEmpty()) {
                List<Object[]> cardCounts = cardRepository.countCardsByStudySetIds(studySetIds);
                cardCountsMap = cardCounts.stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (Long) row[1]));
            }
        }

        if (c.getStudents() != null && !c.getStudents().isEmpty()) {
            List<Long> studentIds = c.getStudents().stream().map(student -> student.getId())
                    .collect(Collectors.toList());
            List<CardProgress> progressList = cardProgressRepository.findByStudentIdIn(studentIds);
            List<QuizAttempt> attemptsList = quizAttemptRepository.findByStudentIdInWithQuiz(studentIds);

            progressMap = progressList.stream().collect(Collectors.groupingBy(p -> p.getStudent().getId()));
            attemptsMap = attemptsList.stream().collect(Collectors.groupingBy(a -> a.getStudent().getId()));
        }

        return toResponse(c, progressMap, attemptsMap, totalCards, assigned, cardCountsMap);
    }

    public ClassResponse toResponse(
            Class c,
            Map<Long, List<CardProgress>> progressMap,
            Map<Long, List<QuizAttempt>> attemptsMap,
            long totalCards,
            List<AssignedStudySet> assigned,
            Map<Long, Long> cardCountsMap) {
        if (c == null)
            return null;

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
                                    double total = a.getQuiz() != null && a.getQuiz().getTotalScore() != null
                                            ? a.getQuiz().getTotalScore()
                                            : 10.0;
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
                .topikLevelId(c.getTopikLevel() != null ? c.getTopikLevel().getId() : null)
                .name(name)
                .schedule(schedule)
                .room(room)
                .code(randomCode)
                .teacherName(c.getTeacher().getFullName() != null ? c.getTeacher().getFullName()
                        : c.getTeacher().getUsername())
                .studentsCount(studentDtos.size())
                .status(c.getStatus() != null ? c.getStatus().name() : "ACTIVE")
                .startedAt(c.getStartedAt())
                .notes(notes)
                .students(studentDtos)
                .assignedStudySets(mapAssignedStudySets(assigned, cardCountsMap))
                .build();
    }

    public List<ClassResponse> toResponses(List<Class> classes) {
        if (classes == null)
            return List.of();

        List<Long> studentIds = classes.stream()
                .flatMap(c -> c.getStudents() != null ? c.getStudents().stream() : java.util.stream.Stream.empty())
                .map(student -> student.getId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, List<CardProgress>> progressMap = Map.of();
        Map<Long, List<QuizAttempt>> attemptsMap = Map.of();
        Map<Long, List<AssignedStudySet>> assignedMap = Map.of();
        Map<Long, Long> studySetCardCounts = Map.of();

        if (!studentIds.isEmpty()) {
            List<CardProgress> allProgress = cardProgressRepository.findByStudentIdIn(studentIds);
            List<QuizAttempt> allAttempts = quizAttemptRepository.findByStudentIdInWithQuiz(studentIds);

            progressMap = allProgress.stream().collect(Collectors.groupingBy(p -> p.getStudent().getId()));
            attemptsMap = allAttempts.stream().collect(Collectors.groupingBy(a -> a.getStudent().getId()));
        }

        List<Long> classIds = classes.stream().map(Class::getId).collect(Collectors.toList());
        if (!classIds.isEmpty()) {
            List<AssignedStudySet> allAssigned = assignedStudySetRepository.findByClazzIdIn(classIds);
            assignedMap = allAssigned.stream().collect(Collectors.groupingBy(a -> a.getClazz().getId()));

            List<Long> allStudySetIds = allAssigned.stream()
                    .map(a -> a.getStudySet().getId())
                    .distinct()
                    .collect(Collectors.toList());
            if (!allStudySetIds.isEmpty()) {
                List<Object[]> cardCounts = cardRepository.countCardsByStudySetIds(allStudySetIds);
                studySetCardCounts = cardCounts.stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (Long) row[1]));
            }
        }

        final Map<Long, List<CardProgress>> finalProgressMap = progressMap;
        final Map<Long, List<QuizAttempt>> finalAttemptsMap = attemptsMap;
        final Map<Long, List<AssignedStudySet>> finalAssignedMap = assignedMap;
        final Map<Long, Long> finalCardCounts = studySetCardCounts;

        return classes.stream()
                .map(c -> {
                    List<AssignedStudySet> assigned = finalAssignedMap.getOrDefault(c.getId(), List.of());
                    long classTotalCards = assigned.stream()
                            .map(a -> a.getStudySet().getId())
                            .distinct()
                            .mapToLong(setId -> finalCardCounts.getOrDefault(setId, 0L))
                            .sum();
                    return toResponse(c, finalProgressMap, finalAttemptsMap, classTotalCards, assigned,
                            finalCardCounts);
                })
                .collect(Collectors.toList());
    }

    private List<ClassResponse.AssignedStudySetDto> mapAssignedStudySets(List<AssignedStudySet> assigned,
            Map<Long, Long> cardCountsMap) {
        if (assigned == null)
            return List.of();
        return assigned.stream()
                .map(a -> {
                    long count = cardCountsMap != null ? cardCountsMap.getOrDefault(a.getStudySet().getId(), 0L)
                            : cardRepository.countByStudySetId(a.getStudySet().getId());
                    return ClassResponse.AssignedStudySetDto.builder()
                            .id(a.getId())
                            .studySetId(a.getStudySet().getId())
                            .studySetTitle(a.getStudySet().getTitle())
                            .studySetDescription(a.getStudySet().getDescription())
                            .wordCount((int) count)
                            .dueDate(a.getDueDate() != null ? a.getDueDate().toString() : null)
                            .note(a.getNote())
                            .assignedAt(a.getAssignedAt() != null ? a.getAssignedAt().toString() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
