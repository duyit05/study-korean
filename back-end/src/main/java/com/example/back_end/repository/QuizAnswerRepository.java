package com.example.back_end.repository;

import com.example.back_end.entity.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    List<QuizAnswer> findByAttemptId(Long attemptId);

    @Query("SELECT qa FROM QuizAnswer qa " +
           "JOIN FETCH qa.question q " +
           "WHERE qa.attempt.id IN :attemptIds")
    List<QuizAnswer> findByAttemptIdInWithQuestion(@Param("attemptIds") List<Long> attemptIds);
}
