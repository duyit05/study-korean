package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRequest {
    @NotBlank(message = "FRONT_TEXT_REQUIRED")
    private String frontText;

    @NotBlank(message = "BACK_TEXT_REQUIRED")
    private String backText;

    private String exampleSentence;
}
