package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "USERNAME_REQUIRED")
    private String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    private String password;
}
