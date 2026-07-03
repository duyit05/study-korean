package com.example.back_end.mapper;

import com.example.back_end.dto.response.ClassResponse;
import com.example.back_end.entity.Class;
import org.springframework.stereotype.Component;

@Component
public class ClassMapper {

    public ClassResponse toResponse(Class c) {
        if (c == null) return null;
        String name = "Lớp học";
        String schedule = "Thứ 2, 4, 6";
        String room = "Room Zoom";
        String notes = c.getNotes();
        if (notes != null && notes.contains("|")) {
            String[] parts = notes.split("\\|", -1);
            if (parts.length >= 3) {
                name = parts[0].trim();
                schedule = parts[1].trim();
                room = parts[2].trim();
                notes = parts.length > 3 ? parts[3].trim() : "";
            }
        } else if (notes != null) {
            name = notes;
        }

        String randomCode = "KOR-" + c.getId() + "-" + (100 + c.getId() * 3);

        return ClassResponse.builder()
                .id(c.getId())
                .name(name)
                .schedule(schedule)
                .room(room)
                .code(randomCode)
                .teacherName(c.getTeacher().getFullName() != null ? c.getTeacher().getFullName() : c.getTeacher().getUsername())
                .studentsCount(12) // Mock student count for UX
                .status(c.getStatus() != null ? c.getStatus().name() : "ACTIVE")
                .startedAt(c.getStartedAt())
                .notes(notes)
                .build();
    }
}
