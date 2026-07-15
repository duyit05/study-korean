package com.example.back_end.service;

import com.example.back_end.dto.response.ClassResponse;
import com.example.back_end.dto.response.TeacherStatsResponse;
import com.example.back_end.entity.QuizAttempt;
import com.example.back_end.entity.User;
import com.example.back_end.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherStatsService {

    private final ClassService classService;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public TeacherStatsResponse getTeacherStats() {
        User currentTeacher = userService.getCurrentUser();
        Long teacherId = currentTeacher.getId();

        // 1. Class Average Scores
        List<ClassResponse> classes = classService.getTeacherClasses();
        List<TeacherStatsResponse.ClassAverageScore> classAverageScores = new ArrayList<>();

        for (ClassResponse c : classes) {
            double sum = 0.0;
            int count = 0;
            if (c.getStudents() != null && !c.getStudents().isEmpty()) {
                for (ClassResponse.StudentDto student : c.getStudents()) {
                    if (student.getAvgScore() != null) {
                        sum += student.getAvgScore();
                        count++;
                    }
                }
            }
            double avg = count > 0 ? sum / count : 0.0;
            avg = Math.round(avg * 10.0) / 10.0; // Round to 1 decimal place
            String labelName = c.getName() != null && !c.getName().isEmpty() ? c.getName() : ("Lớp " + c.getId());
            if (c.getRoom() != null && !c.getRoom().isEmpty()) {
                labelName += " (" + c.getRoom() + ")";
            }
            classAverageScores.add(TeacherStatsResponse.ClassAverageScore.builder()
                    .className(labelName)
                    .averageScore(avg)
                    .build());
        }

        // 2. Monthly Submissions over last 6 months
        java.time.LocalDate today = java.time.LocalDate.now();
        List<String> last6Months = new ArrayList<>();
        Map<String, Long> monthlyCounts = new LinkedHashMap<>();

        // Generate list of last 6 months (chronological order)
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate targetDate = today.minusMonths(i);
            String monthLabel = "Tháng " + targetDate.getMonthValue();
            last6Months.add(monthLabel);
            monthlyCounts.put(monthLabel, 0L);
        }

        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizCreatorId(teacherId);
        for (QuizAttempt att : attempts) {
            java.time.LocalDateTime date = att.getSubmittedAt() != null ? att.getSubmittedAt() : att.getStartedAt();
            if (date != null) {
                String monthLabel = "Tháng " + date.getMonthValue();
                if (monthlyCounts.containsKey(monthLabel)) {
                    monthlyCounts.put(monthLabel, monthlyCounts.get(monthLabel) + 1);
                }
            }
        }

        List<TeacherStatsResponse.MonthlySubmission> monthlySubmissions = new ArrayList<>();
        for (String month : last6Months) {
            monthlySubmissions.add(TeacherStatsResponse.MonthlySubmission.builder()
                    .month(month)
                    .count(monthlyCounts.get(month))
                    .build());
        }

        return TeacherStatsResponse.builder()
                .classAverageScores(classAverageScores)
                .monthlySubmissions(monthlySubmissions)
                .build();
    }
}
