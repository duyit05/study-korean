package com.example.back_end.mapper;

import com.example.back_end.dto.response.StudySetResponse;
import com.example.back_end.entity.Card;
import com.example.back_end.entity.StudySet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import com.example.back_end.repository.CardRepository;

@Component
@RequiredArgsConstructor
public class StudySetMapper {

    private final CardRepository cardRepository;

    public List<StudySetResponse> toResponses(List<StudySet> sets) {
        if (sets == null || sets.isEmpty()) return List.of();

        List<Long> setIds = sets.stream().map(StudySet::getId).collect(Collectors.toList());
        List<Card> allCards = cardRepository.findByStudySetIdInOrderByStudySetIdAscOrderAsc(setIds);
        java.util.Map<Long, List<Card>> cardsBySetMap = allCards.stream()
                .collect(Collectors.groupingBy(card -> card.getStudySet().getId()));

        return sets.stream()
                .map(set -> {
                    List<Card> cards = cardsBySetMap.getOrDefault(set.getId(), List.of());
                    return toResponse(set, cards);
                })
                .collect(Collectors.toList());
    }

    public StudySetResponse toResponse(StudySet s, List<Card> cards) {
        if (s == null) return null;
        return StudySetResponse.builder()
                .id(s.getId())
                .title(s.getTitle())
                .description(s.getDescription())
                .category(s.getTopikLevel() != null ? s.getTopikLevel().getName() : null)
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
