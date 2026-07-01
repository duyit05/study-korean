package com.example.back_end.repository;

import com.example.back_end.entity.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {
    List<ReviewLog> findByStudentId(Long studentId);
    List<ReviewLog> findByStudentIdAndCardId(Long studentId, Long cardId);
}
