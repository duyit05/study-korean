package com.example.back_end.repository;

import com.example.back_end.entity.ClassMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassMaterialRepository extends JpaRepository<ClassMaterial, Long> {
    List<ClassMaterial> findByClazzIdOrderByCreatedAtDesc(Long classId);
}
