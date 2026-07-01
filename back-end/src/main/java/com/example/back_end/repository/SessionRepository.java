package com.example.back_end.repository;

import com.example.back_end.entity.Session;
import com.example.back_end.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByClazzId(Long clazzId);
    List<Session> findByClazzIdOrderBySessionNumberAsc(Long clazzId);
    List<Session> findByClazzIdAndScheduledAtBetween(Long clazzId, LocalDateTime start, LocalDateTime end);
    List<Session> findByClazzIdAndStatus(Long clazzId, SessionStatus status);
}
