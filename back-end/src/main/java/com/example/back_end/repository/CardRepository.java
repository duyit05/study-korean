package com.example.back_end.repository;

import com.example.back_end.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByStudySetId(Long studySetId);
    List<Card> findByStudySetIdOrderByOrderAsc(Long studySetId);

    @Query("SELECT c FROM Card c WHERE c.studySet.id IN :studySetIds ORDER BY c.studySet.id ASC, c.order ASC")
    List<Card> findByStudySetIdInOrderByStudySetIdAscOrderAsc(@Param("studySetIds") List<Long> studySetIds);

    @Query("SELECT c.studySet.id, COUNT(c) FROM Card c WHERE c.studySet.id IN :studySetIds GROUP BY c.studySet.id")
    List<Object[]> countCardsByStudySetIds(@Param("studySetIds") List<Long> studySetIds);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.studySet.id IN :studySetIds")
    long countByStudySetIdIn(@Param("studySetIds") List<Long> studySetIds);
}
