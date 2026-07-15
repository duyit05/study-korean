package com.example.back_end.repository;

import com.example.back_end.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUserId(Long userId);

    @Query("SELECT sp FROM StudentProfile sp JOIN FETCH sp.user u WHERE u.id IN :userIds")
    List<StudentProfile> findByUserIdIn(@Param("userIds") List<Long> userIds);
}
