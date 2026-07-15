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

    @Query("SELECT cp.card.studySet.id, COUNT(cp) FROM CardProgress cp WHERE cp.student.id = :studentId AND cp.repetitions > 0 AND cp.card.studySet.id IN :studySetIds GROUP BY cp.card.studySet.id")
    List<Object[]> countLearnedCardsByStudySetIds(@Param("studentId") Long studentId, @Param("studySetIds") List<Long> studySetIds);

    @Query("SELECT DISTINCT cp.card.studySet.id FROM CardProgress cp WHERE cp.student.id = :studentId")
    List<Long> findDistinctStudySetIdsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(DISTINCT cp.card.studySet.id) FROM CardProgress cp WHERE cp.student.id = :studentId AND cp.repetitions > 0")
    int countDistinctLearnedStudySetsByStudentId(@Param("studentId") Long studentId);
}
