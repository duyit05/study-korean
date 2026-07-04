package com.example.back_end.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topik_levels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopikLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "group_type", length = 20)
    private String groupType; // "VOCAB", "QUIZ", or "ALL"
}
