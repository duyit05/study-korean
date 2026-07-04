package com.example.back_end.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassRequest {
    private Long topikLevelId;
    private String schedule;
    private String room;
    private String notes;
}
