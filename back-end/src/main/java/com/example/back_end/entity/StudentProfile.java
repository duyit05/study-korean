package com.example.back_end.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "student_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 500)
    private String goal;

    @Column(name = "current_level", length = 20)
    private String currentLevel;

    @Column(name = "parent_phone", length = 20)
    private String parentPhone;

    @Column(name = "xp")
    @Builder.Default
    private Integer xp = 0;

    @Column(name = "level")
    @Builder.Default
    private Integer level = 1;

    @Column(name = "streak")
    @Builder.Default
    private Integer streak = 0;
}
