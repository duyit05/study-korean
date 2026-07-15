package com.example.back_end.repository;

import com.example.back_end.entity.User;
import com.example.back_end.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByRole(UserRole role);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u LEFT JOIN StudentProfile p ON p.user.id = u.id " +
            "WHERE u.role = :role " +
            "AND (:search IS NULL OR :search = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:level IS NULL OR :level = '' OR p.currentLevel = :level) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive)")
    List<User> findStudentsWithFilters(
            @org.springframework.data.repository.query.Param("role") UserRole role,
            @org.springframework.data.repository.query.Param("search") String search,
            @org.springframework.data.repository.query.Param("level") String level,
            @org.springframework.data.repository.query.Param("isActive") Boolean isActive
    );

    @org.springframework.data.jpa.repository.Query(value = "SELECT u FROM User u LEFT JOIN StudentProfile p ON p.user.id = u.id " +
            "WHERE u.role = :role " +
            "AND (:search IS NULL OR :search = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:level IS NULL OR :level = '' OR p.currentLevel = :level) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive)",
            countQuery = "SELECT COUNT(u) FROM User u LEFT JOIN StudentProfile p ON p.user.id = u.id " +
            "WHERE u.role = :role " +
            "AND (:search IS NULL OR :search = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:level IS NULL OR :level = '' OR p.currentLevel = :level) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive)")
    org.springframework.data.domain.Page<User> findStudentsWithFiltersPage(
            @org.springframework.data.repository.query.Param("role") UserRole role,
            @org.springframework.data.repository.query.Param("search") String search,
            @org.springframework.data.repository.query.Param("level") String level,
            @org.springframework.data.repository.query.Param("isActive") Boolean isActive,
            org.springframework.data.domain.Pageable pageable
    );
}
