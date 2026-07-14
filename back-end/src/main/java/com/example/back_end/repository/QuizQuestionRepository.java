package com.example.back_end.repository;

import com.example.back_end.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizId(Long quizId);
    List<QuizQuestion> findByQuizIdOrderByOrderAsc(Long quizId);

    @Query("SELECT q FROM QuizQuestion q WHERE q.quiz.id IN :quizIds ORDER BY q.quiz.id ASC, q.order ASC")
    List<QuizQuestion> findByQuizIdInOrderByQuizIdAscOrderAsc(@Param("quizIds") List<Long> quizIds);
}
