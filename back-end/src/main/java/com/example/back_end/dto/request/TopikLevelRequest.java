package com.example.back_end.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopikLevelRequest {
    private String name;
    private String code;
    private String groupType;
}
