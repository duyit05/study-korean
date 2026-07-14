package com.example.back_end.dto.request;

import jakarta.validation.constraints.Email;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateRequest {
    private String fullName;

    @Email(message = "INVALID_EMAIL")
    private String email;

    private String avatarUrl;
}
