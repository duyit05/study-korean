package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionRequest {
    @NotNull(message = "CLASS_ID_REQUIRED")
    private Long classId;

    private Long scheduleId;

    @NotNull(message = "SESSION_NUMBER_REQUIRED")
    private Integer sessionNumber;

    private LocalDateTime scheduledAt;
    
    private LocalDateTime actualStartAt;
    
    private LocalDateTime actualEndAt;

    private String status; // matches SessionStatus enum (e.g. SCHEDULED, ONGOING, COMPLETED, CANCELLED, RESCHEDULED)

    private String topic;

    private String homework;

    private String teacherNotes;

    private String studentNotes;

    private String meetingUrl;

    private String cancelledReason;
}
