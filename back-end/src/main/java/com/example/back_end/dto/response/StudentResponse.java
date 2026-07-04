package com.example.back_end.dto.response;

import com.example.back_end.enums.UserRole;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String phone;
    private UserRole role;
    private Boolean isActive;
    private String goal;
    private String currentLevel;
    private Integer xp;
    private Integer level;
    private Integer streak;
}
