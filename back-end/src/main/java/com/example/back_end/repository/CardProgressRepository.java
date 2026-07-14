package com.example.back_end.repository;

import com.example.back_end.entity.CardProgress;
import com.example.back_end.enums.CardState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CardProgressRepository extends JpaRepository<CardProgress, Long> {
    Optional<CardProgress> findByStudentIdAndCardId(Long studentId, Long cardId);
    List<CardProgress> findByStudentId(Long studentId);
    List<CardProgress> findByStudentIdAndState(Long studentId, CardState state);
    List<CardProgress> findByStudentIdAndNextReviewAtBefore(Long studentId, LocalDateTime dateTime);

    @Query("SELECT cp FROM CardProgress cp JOIN FETCH cp.student s WHERE s.id IN :studentIds")
    List<CardProgress> findByStudentIdIn(@Param("studentIds") List<Long> studentIds);
}
