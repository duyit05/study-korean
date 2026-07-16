package com.example.back_end.repository;

import com.example.back_end.entity.User;
import com.example.back_end.enums.UserRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

        @Query("SELECT u FROM User u LEFT JOIN StudentProfile p ON p.user.id = u.id " +
                        "WHERE u.role = :role " +
                        "AND (:search IS NULL OR :search = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) "
                        +
                        "AND (:level IS NULL OR :level = '' OR p.currentLevel = :level) " +
                        "AND (:isActive IS NULL OR u.isActive = :isActive)")
        List<User> findStudentsWithFilters(
                        @Param("role") UserRole role,
                        @Param("search") String search,
                        @Param("level") String level,
                        @Param("isActive") Boolean isActive);

        @Query(value = "SELECT u FROM User u LEFT JOIN StudentProfile p ON p.user.id = u.id " +
                        "WHERE u.role = :role " +
                        "AND (:search IS NULL OR :search = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) "
                        +
                        "AND (:level IS NULL OR :level = '' OR p.currentLevel = :level) " +
                        "AND (:isActive IS NULL OR u.isActive = :isActive)", countQuery = "SELECT COUNT(u) FROM User u LEFT JOIN StudentProfile p ON p.user.id = u.id "
                                        +
                                        "WHERE u.role = :role " +
                                        "AND (:search IS NULL OR :search = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) "
                                        +
                                        "AND (:level IS NULL OR :level = '' OR p.currentLevel = :level) " +
                                        "AND (:isActive IS NULL OR u.isActive = :isActive)")
        Page<User> findStudentsWithFiltersPage(
                        @Param("role") UserRole role,
                        @Param("search") String search,
                        @Param("level") String level,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);
}
