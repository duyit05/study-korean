package com.example.back_end.repository;

import com.example.back_end.entity.AssignedStudySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignedStudySetRepository extends JpaRepository<AssignedStudySet, Long> {
    List<AssignedStudySet> findByStudentId(Long studentId);
    List<AssignedStudySet> findByClazzId(Long clazzId);
}
