package com.example.back_end.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {
    private Long id;
    private Long classId;
    private String className;
    private Long scheduleId;
    private Integer sessionNumber;
    private LocalDateTime scheduledAt;
    private LocalDateTime actualStartAt;
    private LocalDateTime actualEndAt;
    private String status;
    private String topic;
    private String homework;
    private String teacherNotes;
    private String studentNotes;
    private String meetingUrl;
    private String cancelledReason;
}
