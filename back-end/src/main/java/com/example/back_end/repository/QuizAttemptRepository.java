package com.example.back_end.repository;

import com.example.back_end.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    @Query("SELECT qa FROM QuizAttempt qa " +
           "JOIN FETCH qa.quiz q " +
           "JOIN FETCH qa.student s " +
           "WHERE qa.quiz.creator.id = :creatorId AND qa.status = :status")
    List<QuizAttempt> findByQuizCreatorIdAndStatusWithRelations(@Param("creatorId") Long creatorId, @Param("status") String status);

    @Query("SELECT qa FROM QuizAttempt qa " +
           "JOIN FETCH qa.quiz q " +
           "JOIN FETCH qa.student s " +
           "WHERE qa.student.id = :studentId")
    List<QuizAttempt> findByStudentIdWithRelations(@Param("studentId") Long studentId);

    @Query("SELECT qa FROM QuizAttempt qa " +
           "JOIN FETCH qa.quiz q " +
           "JOIN FETCH qa.student s " +
           "WHERE qa.student.id IN :studentIds")
    List<QuizAttempt> findByStudentIdInWithQuiz(@Param("studentIds") List<Long> studentIds);

    List<QuizAttempt> findByStudentId(Long studentId);
    List<QuizAttempt> findByQuizId(Long quizId);
    List<QuizAttempt> findByStudentIdAndQuizId(Long studentId, Long quizId);
    List<QuizAttempt> findByStatus(String status);
    List<QuizAttempt> findByQuizCreatorIdAndStatus(Long creatorId, String status);

    @Query("SELECT qa FROM QuizAttempt qa JOIN FETCH qa.quiz q WHERE q.creator.id = :creatorId")
    List<QuizAttempt> findByQuizCreatorId(@Param("creatorId") Long creatorId);

    @Query(value = "SELECT qa FROM QuizAttempt qa " +
           "JOIN FETCH qa.quiz q " +
           "JOIN FETCH qa.student s " +
           "WHERE q.creator.id = :creatorId " +
           "AND (:status IS NULL OR qa.status = :status) " +
           "AND (CAST(:search AS string) IS NULL OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(s.username) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "AND qa.submittedAt >= :startDate " +
           "AND qa.submittedAt <= :endDate",
           countQuery = "SELECT COUNT(qa) FROM QuizAttempt qa " +
                        "JOIN qa.quiz q " +
                        "JOIN qa.student s " +
                        "WHERE q.creator.id = :creatorId " +
                        "AND (:status IS NULL OR qa.status = :status) " +
                        "AND (CAST(:search AS string) IS NULL OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(s.username) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
                        "AND qa.submittedAt >= :startDate " +
                        "AND qa.submittedAt <= :endDate")
    Page<QuizAttempt> findAttemptsForTeacherWithFilters(
            @Param("creatorId") Long creatorId,
            @Param("status") String status,
            @Param("search") String search,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
