package com.example.back_end.dto.response;

import com.example.back_end.enums.UserRole;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String phone;
    private UserRole role;
}
