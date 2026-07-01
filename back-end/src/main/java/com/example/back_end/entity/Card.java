package com.example.back_end.entity;

import com.example.back_end.enums.CardType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_set_id")
    private StudySet studySet;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", length = 20)
    @Builder.Default
    private CardType cardType = CardType.VOCAB;

    @Column(name = "front_text", nullable = false, length = 500)
    private String frontText;

    @Column(name = "front_audio_url", length = 500)
    private String frontAudioUrl;

    @Column(name = "front_image_url", length = 500)
    private String frontImageUrl;

    @Column(name = "back_text", nullable = false, length = 500)
    private String backText;

    @Column(name = "back_audio_url", length = 500)
    private String backAudioUrl;

    @Column(name = "back_image_url", length = 500)
    private String backImageUrl;

    @Column(length = 500)
    private String romanization;

    @Column(name = "example_sentence", length = 1000)
    private String exampleSentence;

    @Column(name = "example_meaning", length = 1000)
    private String exampleMeaning;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> tags;

    @Builder.Default
    private Integer difficulty = 1;

    @Column(name = "\"order\"")
    @Builder.Default
    private Integer order = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
