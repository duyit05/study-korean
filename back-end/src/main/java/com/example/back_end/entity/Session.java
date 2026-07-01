package com.example.back_end.entity;

import com.example.back_end.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Class clazz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @Column(name = "session_number")
    private Integer sessionNumber;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "actual_start_at")
    private LocalDateTime actualStartAt;

    @Column(name = "actual_end_at")
    private LocalDateTime actualEndAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(length = 500)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String homework;

    @Column(name = "teacher_notes", columnDefinition = "TEXT")
    private String teacherNotes;

    @Column(name = "student_notes", columnDefinition = "TEXT")
    private String studentNotes;

    @Column(name = "meeting_url", length = 500)
    private String meetingUrl;

    @Column(name = "cancelled_reason", columnDefinition = "TEXT")
    private String cancelledReason;
}
