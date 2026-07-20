package com.example.back_end.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "quiz_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Column(name = "correct_answer", nullable = false, length = 500)
    private String correctAnswer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "wrong_answers", columnDefinition = "jsonb")
    private List<String> wrongAnswers;

    @Column(name = "question_type", length = 20)
    private String questionType;

    @Column(name = "\"order\"")
    private Integer order;

    @Column(name = "korean_text", length = 500)
    private String koreanText;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "audio_source", length = 20)
    @Builder.Default
    private String audioSource = "TTS";

    @Column(name = "section", length = 20)
    private String section;

    @Column(name = "points", nullable = false)
    @Builder.Default
    private java.math.BigDecimal points = java.math.BigDecimal.valueOf(2.00);

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "word_type", length = 30)
    private com.example.back_end.enums.WordType wordType;

    @Column(name = "pronunciation", length = 250)
    private String pronunciation;
}
