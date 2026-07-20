package com.example.back_end.service;

import com.example.back_end.dto.request.StudySetRequest;
import com.example.back_end.dto.request.CardRequest;
import com.example.back_end.dto.response.StudySetResponse;
import com.example.back_end.entity.Card;
import com.example.back_end.entity.StudySet;
import com.example.back_end.entity.TopikLevel;
import com.example.back_end.entity.User;
import com.example.back_end.enums.CardType;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.mapper.StudySetMapper;
import com.example.back_end.repository.CardRepository;
import com.example.back_end.repository.StudySetRepository;
import com.example.back_end.repository.TopikLevelRepository;
import com.example.back_end.repository.CardProgressRepository;
import com.example.back_end.entity.CardProgress;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudySetService {

    private final StudySetRepository studySetRepository;
    private final CardRepository cardRepository;
    private final UserService userService;
    private final StudySetMapper studySetMapper;
    private final TopikLevelRepository topikLevelRepository;
    private final CardProgressRepository cardProgressRepository;

    @Transactional(readOnly = true)
    public List<StudySetResponse> getAllStudySets() {
        List<StudySet> sets = studySetRepository.findAllWithCreatorAndTopikLevel();
        Long studentId = null;
        try {
            studentId = userService.getCurrentUser().getId();
        } catch (Exception e) {
            // anonymous or unauthenticated
        }
        return studySetMapper.toResponses(sets, studentId);
    }

    @Transactional(readOnly = true)
    public List<StudySetResponse.CardResponse> getCardsByStudySet(Long studySetId) {
        List<Card> cards = cardRepository.findByStudySetIdOrderByOrderAsc(studySetId);
        Long studentId = null;
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                studentId = currentUser.getId();
            }
        } catch (Exception e) {
            // anonymous
        }

        java.util.Map<Long, CardProgress> progressMap = new java.util.HashMap<>();
        if (studentId != null) {
            List<CardProgress> progresses = cardProgressRepository.findByStudentId(studentId);
            for (CardProgress cp : progresses) {
                if (cp.getCard() != null) {
                    progressMap.put(cp.getCard().getId(), cp);
                }
            }
        }

        final java.util.Map<Long, CardProgress> finalProgressMap = progressMap;
        return cards.stream()
                .map(c -> {
                    CardProgress cp = finalProgressMap.get(c.getId());
                    String status = "unlearned";
                    if (cp != null) {
                        if (cp.getState() == com.example.back_end.enums.CardState.MASTERED) {
                            status = "learned";
                        } else if (cp.getState() == com.example.back_end.enums.CardState.REVIEW || cp.getState() == com.example.back_end.enums.CardState.LEARNING) {
                            status = "review";
                        }
                    }
                    StudySetResponse.CardResponse res = studySetMapper.toCardResponse(c);
                    res.setPronunciation(c.getRomanization());
                    res.setExampleMeaning(c.getExampleMeaning());
                    res.setStatus(status);
                    return res;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public StudySetResponse createStudySet(StudySetRequest request) {
        User creator = userService.getCurrentUser();

        TopikLevel level = null;
        if (request.getTopikLevelId() != null) {
            level = topikLevelRepository.findById(request.getTopikLevelId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        StudySet studySet = StudySet.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .topikLevel(level)
                .creator(creator)
                .isPublic(true)
                .build();
        StudySet saved = studySetRepository.save(studySet);
        return studySetMapper.toResponse(saved, List.of());
    }

    @Transactional
    public StudySetResponse.CardResponse addCardToStudySet(Long studySetId, CardRequest request) {
        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        List<Card> existingCards = cardRepository.findByStudySetId(studySetId);
        int nextOrder = existingCards.size() + 1;

        Card card = Card.builder()
                .studySet(studySet)
                .cardType(CardType.VOCAB)
                .frontText(request.getFrontText())
                .backText(request.getBackText())
                .romanization(request.getPronunciation())
                .exampleSentence(request.getExampleSentence())
                .order(nextOrder)
                .difficulty(1)
                .build();

        Card saved = cardRepository.save(card);
        return studySetMapper.toCardResponse(saved);
    }

    @Transactional
    public List<StudySetResponse.CardResponse> addCardsToStudySet(Long studySetId, List<CardRequest> requests) {
        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        List<Card> existingCards = cardRepository.findByStudySetId(studySetId);
        int nextOrder = existingCards.size() + 1;

        List<Card> cardsToSave = new java.util.ArrayList<>();
        for (CardRequest request : requests) {
            Card card = Card.builder()
                    .studySet(studySet)
                    .cardType(CardType.VOCAB)
                    .frontText(request.getFrontText())
                    .backText(request.getBackText())
                    .romanization(request.getPronunciation())
                    .exampleSentence(request.getExampleSentence())
                    .order(nextOrder++)
                    .difficulty(1)
                    .build();
            cardsToSave.add(card);
        }

        List<Card> saved = cardRepository.saveAll(cardsToSave);
        return saved.stream()
                .map(studySetMapper::toCardResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCard(Long studySetId, Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (!card.getStudySet().getId().equals(studySetId)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        cardRepository.delete(card);
    }

    @Transactional
    public StudySetResponse updateStudySet(Long id, StudySetRequest request) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        TopikLevel level = null;
        if (request.getTopikLevelId() != null) {
            level = topikLevelRepository.findById(request.getTopikLevelId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        studySet.setTitle(request.getTitle());
        studySet.setDescription(request.getDescription());
        studySet.setTopikLevel(level);
        StudySet saved = studySetRepository.save(studySet);
        List<Card> cards = cardRepository.findByStudySetIdOrderByOrderAsc(id);
        return studySetMapper.toResponse(saved, cards);
    }

    @Transactional
    public void deleteStudySet(Long id) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        List<Card> cards = cardRepository.findByStudySetId(id);
        cardRepository.deleteAll(cards);
        studySetRepository.delete(studySet);
    }
}
