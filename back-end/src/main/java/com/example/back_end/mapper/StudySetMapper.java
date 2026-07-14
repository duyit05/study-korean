package com.example.back_end.mapper;

import com.example.back_end.dto.response.StudySetResponse;
import com.example.back_end.entity.Card;
import com.example.back_end.entity.StudySet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import com.example.back_end.repository.CardRepository;
import com.example.back_end.repository.CardProgressRepository;

@Component
@RequiredArgsConstructor
public class StudySetMapper {

    private final CardRepository cardRepository;
    private final CardProgressRepository cardProgressRepository;

    public List<StudySetResponse> toResponses(List<StudySet> sets) {
        return toResponses(sets, null);
    }

    public List<StudySetResponse> toResponses(List<StudySet> sets, Long studentId) {
        if (sets == null || sets.isEmpty()) return List.of();

        List<Long> setIds = sets.stream().map(StudySet::getId).collect(Collectors.toList());
        List<Object[]> counts = cardRepository.countCardsByStudySetIds(setIds);
        java.util.Map<Long, Long> countMap = counts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        java.util.Map<Long, Long> learnedCountMap = java.util.Map.of();
        if (studentId != null) {
            List<Object[]> learnedCounts = cardProgressRepository.countLearnedCardsByStudySetIds(studentId, setIds);
            learnedCountMap = learnedCounts.stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],
                            row -> (Long) row[1]
                    ));
        }

        final java.util.Map<Long, Long> finalLearnedCountMap = learnedCountMap;
        return sets.stream()
                .map(set -> {
                    StudySetResponse res = toResponse(set, null);
                    res.setWordCount(countMap.getOrDefault(set.getId(), 0L).intValue());
                    res.setLearnedCount(finalLearnedCountMap.getOrDefault(set.getId(), 0L).intValue());
                    return res;
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
                .wordCount(cards != null ? cards.size() : 0)
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
