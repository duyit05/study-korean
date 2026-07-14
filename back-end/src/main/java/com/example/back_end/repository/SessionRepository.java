package com.example.back_end.repository;

import com.example.back_end.entity.Session;
import com.example.back_end.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByClazzId(Long clazzId);

    @Query("SELECT s FROM Session s " +
           "JOIN FETCH s.clazz c " +
           "LEFT JOIN FETCH c.topikLevel " +
           "WHERE s.clazz.id = :clazzId " +
           "ORDER BY s.sessionNumber ASC")
    List<Session> findByClazzIdOrderBySessionNumberAscWithClazzAndTopikLevel(@Param("clazzId") Long clazzId);

    List<Session> findByClazzIdOrderBySessionNumberAsc(Long clazzId);
    List<Session> findByClazzIdAndScheduledAtBetween(Long clazzId, LocalDateTime start, LocalDateTime end);
    List<Session> findByClazzIdAndStatus(Long clazzId, SessionStatus status);
}
