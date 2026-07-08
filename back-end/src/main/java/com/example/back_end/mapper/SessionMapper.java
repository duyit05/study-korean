package com.example.back_end.mapper;

import com.example.back_end.dto.response.SessionResponse;
import com.example.back_end.entity.Session;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public SessionResponse toResponse(Session session) {
        if (session == null) return null;

        Long classId = null;
        String className = null;
        if (session.getClazz() != null) {
            classId = session.getClazz().getId();
            if (session.getClazz().getTopikLevel() != null) {
                className = session.getClazz().getTopikLevel().getName();
            } else {
                String notes = session.getClazz().getNotes();
                if (notes != null && notes.contains("|")) {
                    className = notes.split("\\|", -1)[0].trim();
                } else {
                    className = notes;
                }
            }
        }

        Long scheduleId = session.getSchedule() != null ? session.getSchedule().getId() : null;

        return SessionResponse.builder()
                .id(session.getId())
                .classId(classId)
                .className(className)
                .scheduleId(scheduleId)
                .sessionNumber(session.getSessionNumber())
                .scheduledAt(session.getScheduledAt())
                .actualStartAt(session.getActualStartAt())
                .actualEndAt(session.getActualEndAt())
                .status(session.getStatus() != null ? session.getStatus().name() : null)
                .topic(session.getTopic())
                .homework(session.getHomework())
                .teacherNotes(session.getTeacherNotes())
                .studentNotes(session.getStudentNotes())
                .meetingUrl(session.getMeetingUrl())
                .cancelledReason(session.getCancelledReason())
                .build();
    }
}
