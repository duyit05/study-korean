package com.example.back_end.repository;

import com.example.back_end.entity.StudySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface StudySetRepository extends JpaRepository<StudySet, Long> {
    @Query("SELECT s FROM StudySet s LEFT JOIN FETCH s.creator LEFT JOIN FETCH s.topikLevel")
    List<StudySet> findAllWithCreatorAndTopikLevel();

    List<StudySet> findByCreatorId(Long creatorId);
    List<StudySet> findByIsPublicTrue();
}
