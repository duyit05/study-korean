package com.example.back_end.entity;

import com.example.back_end.enums.CardState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "card_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @Builder.Default
    private Integer repetitions = 0;

    @Column(name = "ease_factor")
    @Builder.Default
    private BigDecimal easeFactor = new BigDecimal("2.5");

    @Column(name = "interval_days")
    @Builder.Default
    private Integer intervalDays = 1;

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "correct_count")
    @Builder.Default
    private Integer correctCount = 0;

    @Column(name = "incorrect_count")
    @Builder.Default
    private Integer incorrectCount = 0;

    @Column(name = "last_quality")
    private Integer lastQuality;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private CardState state = CardState.NEW;

    @Column(name = "first_seen_at")
    private LocalDateTime firstSeenAt;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
