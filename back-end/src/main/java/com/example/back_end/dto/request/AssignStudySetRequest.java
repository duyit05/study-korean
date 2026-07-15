package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignStudySetRequest {
    @NotNull(message = "CLASS_ID_REQUIRED")
    private Long classId;

    @NotNull(message = "STUDY_SET_ID_REQUIRED")
    private Long studySetId;

    private LocalDate dueDate;
    private String note;
}
