package com.example.back_end.repository;

import com.example.back_end.entity.TopikLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopikLevelRepository extends JpaRepository<TopikLevel, Long> {

    @Query("SELECT t FROM TopikLevel t WHERE " +
           "(:search IS NULL OR LOWER(t.name) LIKE :search OR LOWER(t.code) LIKE :search)")
    Page<TopikLevel> searchLevels(
            @Param("search") String search,
            Pageable pageable
    );
}
