package com.example.back_end.repository;

import com.example.back_end.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByStudySetId(Long studySetId);
    List<Card> findByStudySetIdOrderByOrderAsc(Long studySetId);
}
