package com.example.back_end.repository;

import com.example.back_end.entity.AssignedStudySet;

import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignedStudySetRepository extends JpaRepository<AssignedStudySet, Long> {
    List<AssignedStudySet> findByStudentId(Long studentId);

    List<AssignedStudySet> findByClazzId(Long clazzId);

    @Query("SELECT a FROM AssignedStudySet a JOIN FETCH a.studySet WHERE a.clazz.id IN :clazzIds")
    List<AssignedStudySet> findByClazzIdIn(@Param("clazzIds") List<Long> clazzIds);
}
