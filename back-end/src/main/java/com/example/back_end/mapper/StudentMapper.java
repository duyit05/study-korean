package com.example.back_end.mapper;

import com.example.back_end.dto.response.StudentResponse;
import com.example.back_end.entity.StudentProfile;
import com.example.back_end.entity.User;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public StudentResponse toResponse(User user) {
        if (user == null) return null;

        StudentResponse.StudentResponseBuilder builder = StudentResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive() != null ? user.getIsActive() : true);

        StudentProfile profile = user.getStudentProfile();
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
