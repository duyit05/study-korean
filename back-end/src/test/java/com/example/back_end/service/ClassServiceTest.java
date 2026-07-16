package com.example.back_end.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.back_end.dto.request.ClassRequest;
import com.example.back_end.dto.response.ClassResponse;
import com.example.back_end.entity.Class;
import com.example.back_end.entity.Course;
import com.example.back_end.entity.TopikLevel;
import com.example.back_end.entity.User;
import com.example.back_end.enums.ClassStatus;
import com.example.back_end.enums.UserRole;
import com.example.back_end.exception.AppException;
import com.example.back_end.mapper.ClassMapper;
import com.example.back_end.repository.ClassRepository;
import com.example.back_end.repository.CourseRepository;
import com.example.back_end.repository.TopikLevelRepository;

@ExtendWith(MockitoExtension.class)
class ClassServiceTest {

    @Mock
    private ClassRepository classRepository;
    @Mock
    private UserService userService;
    @Mock
    private ClassMapper classMapper;
    @Mock
    private TopikLevelRepository topikLevelRepository;
    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ClassService classService;

    private User teacher;
    private Class clazz;
    private TopikLevel topikLevel;
    private Course course;

    @BeforeEach
    void setUp() {
        teacher = User.builder()
                .id(1L)
                .username("teacher")
                .role(UserRole.TEACHER)
                .build();

        topikLevel = TopikLevel.builder()
                .id(1L)
                .name("Topik 1")
                .build();

        course = Course.builder()
                .id(1L)
                .title("Course 1")
                .build();

        clazz = Class.builder()
                .id(100L)
                .teacher(teacher)
                .topikLevel(topikLevel)
                .course(course)
                .schedule("Mon-Wed")
                .room("Room A")
                .status(ClassStatus.ACTIVE)
                .build();
    }

    @Test
    void updateClass_Success() {
        ClassRequest request = ClassRequest.builder()
                .topikLevelId(1L)
                .courseId(1L)
                .schedule("Tue-Thu")
                .room("Room B")
                .notes("Updated notes")
                .status("COMPLETED")
                .build();

        when(userService.getCurrentUser()).thenReturn(teacher);
        when(classRepository.findById(100L)).thenReturn(Optional.of(clazz));
        when(topikLevelRepository.findById(1L)).thenReturn(Optional.of(topikLevel));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(classRepository.save(any(Class.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(classMapper.toResponse(any(Class.class))).thenAnswer(invocation -> {
            Class savedClass = invocation.getArgument(0);
            return ClassResponse.builder()
                    .id(savedClass.getId())
                    .room(savedClass.getRoom())
                    .status(savedClass.getStatus().name())
                    .build();
        });

        ClassResponse response = classService.updateClass(100L, request);

        assertNotNull(response);
        assertEquals("COMPLETED", clazz.getStatus().name());
        assertEquals("Tue-Thu", clazz.getSchedule());
        assertEquals("Room B", clazz.getRoom());
        assertEquals("Updated notes", clazz.getNotes());
        verify(classRepository, times(1)).save(clazz);
    }

    @Test
    void updateClass_Forbidden() {
        User otherTeacher = User.builder()
                .id(2L)
                .username("other")
                .role(UserRole.TEACHER)
                .build();

        ClassRequest request = ClassRequest.builder()
                .topikLevelId(1L)
                .build();

        when(userService.getCurrentUser()).thenReturn(otherTeacher);
        when(classRepository.findById(100L)).thenReturn(Optional.of(clazz));

        assertThrows(AppException.class, () -> classService.updateClass(100L, request));
        verify(classRepository, never()).save(any(Class.class));
    }
}
