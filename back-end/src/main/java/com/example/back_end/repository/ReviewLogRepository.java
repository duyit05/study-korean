package com.example.back_end.repository;

import com.example.back_end.entity.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {
    List<ReviewLog> findByStudentId(Long studentId);
    List<ReviewLog> findByStudentIdAndCardId(Long studentId, Long cardId);

    @Modifying
    @Query("DELETE FROM ReviewLog r WHERE r.studySet.id = :studySetId")
    void deleteByStudySetId(@Param("studySetId") Long studySetId);
}
