package com.example.back_end.repository;

import com.example.back_end.entity.StudySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudySetRepository extends JpaRepository<StudySet, Long> {
    List<StudySet> findByCreatorId(Long creatorId);
    List<StudySet> findByIsPublicTrue();
}
