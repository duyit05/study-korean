package com.example.back_end.repository;

import com.example.back_end.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.teacher")
    List<Course> findAllWithTeacher();

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.teacher WHERE c.teacher.id = :teacherId")
    List<Course> findByTeacherIdWithTeacher(@Param("teacherId") Long teacherId);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.teacher WHERE c.isPublished = true")
    List<Course> findByIsPublishedTrueWithTeacher();

    List<Course> findByTeacherId(Long teacherId);
    List<Course> findByIsPublishedTrue();
}
