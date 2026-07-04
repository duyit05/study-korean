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

    @Transactional(readOnly = true)
    public List<StudySetResponse> getAllStudySets() {
        return studySetRepository.findAll().stream()
                .map(set -> {
                    List<Card> cards = cardRepository.findByStudySetIdOrderByOrderAsc(set.getId());
                    return studySetMapper.toResponse(set, cards);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Card> getCardsByStudySet(Long studySetId) {
        return cardRepository.findByStudySetIdOrderByOrderAsc(studySetId);
    }

    @Transactional
    public StudySetResponse createStudySet(StudySetRequest request) {
        User creator = userService.getCurrentUser();
        log.info("Creating study set: {}", request.getTitle());

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
        log.info("Adding card to study set: {}", studySetId);
        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        List<Card> existingCards = cardRepository.findByStudySetId(studySetId);
        int nextOrder = existingCards.size() + 1;

        Card card = Card.builder()
                .studySet(studySet)
                .cardType(CardType.VOCAB)
                .frontText(request.getFrontText())
                .backText(request.getBackText())
                .exampleSentence(request.getExampleSentence())
                .order(nextOrder)
                .difficulty(1)
                .build();

        Card saved = cardRepository.save(card);
        return studySetMapper.toCardResponse(saved);
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
        log.info("Updating study set: id={}, title={}", id, request.getTitle());
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
        log.info("Deleting study set: id={}", id);
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        List<Card> cards = cardRepository.findByStudySetId(id);
        cardRepository.deleteAll(cards);
        studySetRepository.delete(studySet);
    }
}
