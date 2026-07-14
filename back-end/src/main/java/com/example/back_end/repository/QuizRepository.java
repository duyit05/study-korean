package com.example.back_end.repository;

import com.example.back_end.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.topikLevel WHERE q.clazz.id = :classId")
    List<Quiz> findByClazzIdWithTopikLevel(@Param("classId") Long classId);

    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.topikLevel WHERE q.creator.id = :creatorId")
    List<Quiz> findByCreatorIdWithTopikLevel(@Param("creatorId") Long creatorId);

    List<Quiz> findByClazzId(Long clazzId);
    List<Quiz> findByCreatorId(Long creatorId);
}
