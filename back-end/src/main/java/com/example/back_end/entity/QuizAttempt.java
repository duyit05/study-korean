package com.example.back_end.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    private BigDecimal score;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "correct_count")
    private Integer correctCount;

    @Column(name = "time_taken_secs")
    private Integer timeTakenSecs;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "listening_score")
    @Builder.Default
    private java.math.BigDecimal listeningScore = java.math.BigDecimal.ZERO;

    @Column(name = "reading_score")
    @Builder.Default
    private java.math.BigDecimal readingScore = java.math.BigDecimal.ZERO;

    @Column(name = "writing_score")
    @Builder.Default
    private java.math.BigDecimal writingScore = java.math.BigDecimal.ZERO;

    @Column(length = 20)
    private String status;
}
