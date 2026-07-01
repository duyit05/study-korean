package com.example.back_end.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRequest {
    private String frontText;
    private String backText;
    private String exampleSentence;
}
