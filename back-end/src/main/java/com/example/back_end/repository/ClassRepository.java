package com.example.back_end.repository;

import com.example.back_end.entity.Class;
import com.example.back_end.enums.ClassStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

       @Query(value = "SELECT DISTINCT c FROM Class c " +
                     "LEFT JOIN FETCH c.teacher " +
                     "LEFT JOIN FETCH c.topikLevel " +
                     "LEFT JOIN FETCH c.course " +
                     "WHERE c.teacher.id = :teacherId " +
                     "AND (:search IS NULL OR :search = '' OR LOWER(c.topikLevel.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                     "AND (:level IS NULL OR :level = '' OR c.topikLevel.name = :level)",
              countQuery = "SELECT COUNT(DISTINCT c) FROM Class c " +
                           "LEFT JOIN c.topikLevel l " +
                           "WHERE c.teacher.id = :teacherId " +
                           "AND (:search IS NULL OR :search = '' OR LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                           "AND (:level IS NULL OR :level = '' OR l.name = :level)")
       Page<Class> findByTeacherIdWithFiltersPage(
                     @Param("teacherId") Long teacherId,
                     @Param("search") String search,
                     @Param("level") String level,
                     Pageable pageable);

       @Query("SELECT DISTINCT c FROM Class c " +
                     "LEFT JOIN FETCH c.teacher " +
                     "LEFT JOIN FETCH c.topikLevel " +
                     "LEFT JOIN FETCH c.course " +
                     "LEFT JOIN FETCH c.students " +
                     "WHERE c.teacher.id = :teacherId " +
                     "AND (:search IS NULL OR :search = '' OR LOWER(c.topikLevel.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                     "AND (:level IS NULL OR :level = '' OR c.topikLevel.name = :level)")
       List<Class> findByTeacherIdWithFilters(
                     @Param("teacherId") Long teacherId,
                     @Param("search") String search,
                     @Param("level") String level);

       @Query("SELECT DISTINCT c FROM Class c " +
              "LEFT JOIN FETCH c.students " +
              "WHERE c.id IN :classIds")
       List<Class> fetchStudentsForClasses(@Param("classIds") List<Long> classIds);
}
