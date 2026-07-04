package com.example.back_end.repository;

import com.example.back_end.entity.TopikLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopikLevelRepository extends JpaRepository<TopikLevel, Long> {
    List<TopikLevel> findByGroupTypeOrGroupType(String groupType1, String groupType2);
}
