package com.example.back_end.repository;

import com.example.back_end.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByClazzId(Long clazzId);
    List<Quiz> findByCreatorId(Long creatorId);
}
