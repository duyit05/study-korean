package com.example.back_end.repository;

import com.example.back_end.entity.Class;
import com.example.back_end.enums.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {
    List<Class> findByStudentId(Long studentId);
    List<Class> findByTeacherId(Long teacherId);
    List<Class> findByStudentIdAndStatus(Long studentId, ClassStatus status);
    List<Class> findByTeacherIdAndStudentIsNull(Long teacherId);
    List<Class> findByTeacherIdAndNotesAndStudentIsNotNull(Long teacherId, String notes);
}
