package com.example.back_end.entity;

import com.example.back_end.enums.QuizType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_set_id")
    private StudySet studySet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Class clazz;

    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type", length = 20)
    private QuizType quizType;

    @Column(name = "time_limit_mins")
    private Integer timeLimitMins;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topik_level_id")
    private TopikLevel topikLevel;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
