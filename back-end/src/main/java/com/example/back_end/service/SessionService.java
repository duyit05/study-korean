package com.example.back_end.service;

import com.example.back_end.dto.request.SessionRequest;
import com.example.back_end.dto.response.SessionResponse;
import com.example.back_end.entity.Class;
import com.example.back_end.entity.Schedule;
import com.example.back_end.entity.Session;
import com.example.back_end.entity.User;
import com.example.back_end.enums.SessionStatus;
import com.example.back_end.enums.UserRole;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.mapper.SessionMapper;
import com.example.back_end.repository.ClassRepository;
import com.example.back_end.repository.ScheduleRepository;
import com.example.back_end.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ClassRepository classRepository;
    private final ScheduleRepository scheduleRepository;
    private final SessionMapper sessionMapper;
    private final UserService userService;

    @Transactional
    public SessionResponse createSession(SessionRequest request) {
        User user = userService.getCurrentUser();
        Class clazz = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Validate authority: Only class teacher or ADMIN can create sessions
        if (user.getRole() != UserRole.ADMIN && !clazz.getTeacher().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        log.info("Creating session for class: {} by teacher: {}", clazz.getId(), user.getUsername());

        Schedule schedule = null;
        if (request.getScheduleId() != null) {
            schedule = scheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        SessionStatus status = SessionStatus.SCHEDULED;
        if (request.getStatus() != null) {
            try {
                status = SessionStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Keep default SCHEDULED status
            }
        }

        Session session = Session.builder()
                .clazz(clazz)
                .schedule(schedule)
                .sessionNumber(request.getSessionNumber())
                .scheduledAt(request.getScheduledAt())
                .actualStartAt(request.getActualStartAt())
                .actualEndAt(request.getActualEndAt())
                .status(status)
                .topic(request.getTopic())
                .homework(request.getHomework())
                .teacherNotes(request.getTeacherNotes())
                .studentNotes(request.getStudentNotes())
                .meetingUrl(request.getMeetingUrl())
                .cancelledReason(request.getCancelledReason())
                .build();

        Session saved = sessionRepository.save(session);
        return sessionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionById(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return sessionMapper.toResponse(session);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsByClass(Long classId) {
        // Find by class ID and order by session number ascending
        return sessionRepository.findByClazzIdOrderBySessionNumberAsc(classId).stream()
                .map(sessionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SessionResponse updateSession(Long id, SessionRequest request) {
        User user = userService.getCurrentUser();
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Class clazz = session.getClazz();
        // Validate authority: Only class teacher or ADMIN can update sessions
        if (user.getRole() != UserRole.ADMIN && (clazz == null || !clazz.getTeacher().getId().equals(user.getId()))) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        log.info("Updating session id: {} by user: {}", id, user.getUsername());

        session.setSessionNumber(request.getSessionNumber());
        session.setScheduledAt(request.getScheduledAt());
        session.setActualStartAt(request.getActualStartAt());
        session.setActualEndAt(request.getActualEndAt());

        if (request.getStatus() != null) {
            try {
                session.setStatus(SessionStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }

        if (request.getScheduleId() != null) {
            Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            session.setSchedule(schedule);
        }

        session.setTopic(request.getTopic());
        session.setHomework(request.getHomework());
        session.setTeacherNotes(request.getTeacherNotes());
        session.setStudentNotes(request.getStudentNotes());
        session.setMeetingUrl(request.getMeetingUrl());
        session.setCancelledReason(request.getCancelledReason());

        Session updated = sessionRepository.save(session);
        return sessionMapper.toResponse(updated);
    }

    @Transactional
    public void deleteSession(Long id) {
        User user = userService.getCurrentUser();
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Class clazz = session.getClazz();
        // Validate authority: Only class teacher or ADMIN can delete sessions
        if (user.getRole() != UserRole.ADMIN && (clazz == null || !clazz.getTeacher().getId().equals(user.getId()))) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        log.info("Deleting session id: {} by user: {}", id, user.getUsername());
        sessionRepository.delete(session);
    }
}
