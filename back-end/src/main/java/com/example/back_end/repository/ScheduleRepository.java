package com.example.back_end.repository;

import com.example.back_end.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByClazzId(Long clazzId);
    List<Schedule> findByClazzIdAndIsActiveTrue(Long clazzId);
}
