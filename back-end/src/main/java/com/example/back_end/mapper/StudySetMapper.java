package com.example.back_end.mapper;

import com.example.back_end.dto.response.StudySetResponse;
import com.example.back_end.entity.Card;
import com.example.back_end.entity.StudySet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StudySetMapper {

    public StudySetResponse toResponse(StudySet s, List<Card> cards) {
        if (s == null) return null;
        return StudySetResponse.builder()
                .id(s.getId())
                .title(s.getTitle())
                .description(s.getDescription())
                .category(s.getCategory())
                .creatorName(s.getCreator().getFullName() != null ? s.getCreator().getFullName() : s.getCreator().getUsername())
                .words(cards != null ? cards.stream().map(this::toCardResponse).collect(Collectors.toList()) : List.of())
                .build();
    }

    public StudySetResponse.CardResponse toCardResponse(Card c) {
        if (c == null) return null;
        return StudySetResponse.CardResponse.builder()
                .id(c.getId())
                .korean(c.getFrontText())
                .vietnamese(c.getBackText())
                .example(c.getExampleSentence())
                .build();
    }
}
