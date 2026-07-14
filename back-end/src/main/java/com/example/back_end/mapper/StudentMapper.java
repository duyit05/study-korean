package com.example.back_end.mapper;

import com.example.back_end.dto.response.StudentResponse;
import com.example.back_end.entity.StudentProfile;
import com.example.back_end.entity.User;
import com.example.back_end.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentMapper {

    private final StudentProfileRepository studentProfileRepository;

    public StudentResponse toResponse(User user) {
        if (user == null) return null;
        StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
        return toResponse(user, profile);
    }

    public StudentResponse toResponse(User user, StudentProfile profile) {
        if (user == null) return null;

        StudentResponse.StudentResponseBuilder builder = StudentResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive() != null ? user.getIsActive() : true);

        if (profile != null) {
            builder.goal(profile.getGoal())
                   .currentLevel(profile.getCurrentLevel())
                   .xp(profile.getXp() != null ? profile.getXp() : 0)
                   .level(profile.getLevel() != null ? profile.getLevel() : 1)
                   .streak(profile.getStreak() != null ? profile.getStreak() : 0);
        }

        return builder.build();
    }
}
