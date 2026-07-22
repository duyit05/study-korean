package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopikLevelRequest {
    @NotBlank(message = "TOPIK_NAME_REQUIRED")
    private String name;

    @NotBlank(message = "TOPIK_CODE_REQUIRED")
    private String code;

}
