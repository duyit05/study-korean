package com.example.back_end.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassRequest {
    private String name;
    private String schedule;
    private String room;
    private String notes;
}
