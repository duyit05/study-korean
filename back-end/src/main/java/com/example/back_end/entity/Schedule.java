package com.example.back_end.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Class clazz;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "day_of_week", columnDefinition = "integer[]")
    private List<Integer> dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "duration_mins")
    @Builder.Default
    private Integer durationMins = 60;

    @Column(length = 50)
    private String platform;

    @Column(name = "meeting_url", length = 500)
    private String meetingUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
