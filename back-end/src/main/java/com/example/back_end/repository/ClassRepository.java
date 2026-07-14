package com.example.back_end.repository;

import com.example.back_end.entity.Class;
import com.example.back_end.enums.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {
    @Query("SELECT DISTINCT c FROM Class c " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH c.topikLevel " +
           "LEFT JOIN FETCH c.course " +
           "LEFT JOIN FETCH c.students " +
           "WHERE c.teacher.id = :teacherId")
    List<Class> findByTeacherIdWithRelations(@Param("teacherId") Long teacherId);

    @Query("SELECT DISTINCT c FROM Class c " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH c.topikLevel " +
           "LEFT JOIN FETCH c.course " +
           "LEFT JOIN FETCH c.students " +
           "WHERE EXISTS (SELECT s FROM c.students s WHERE s.id = :studentId)")
    List<Class> findByStudentsIdWithRelations(@Param("studentId") Long studentId);

    List<Class> findByStudentsId(Long studentId);
    List<Class> findByTeacherId(Long teacherId);
    List<Class> findByStudentsIdAndStatus(Long studentId, ClassStatus status);
}
