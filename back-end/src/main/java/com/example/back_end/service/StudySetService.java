package com.example.back_end.service;

import com.example.back_end.entity.Card;
import com.example.back_end.entity.StudySet;
import com.example.back_end.entity.User;
import com.example.back_end.enums.CardType;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.CardRepository;
import com.example.back_end.repository.StudySetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudySetService {

    private final StudySetRepository studySetRepository;
    private final CardRepository cardRepository;

    @Transactional(readOnly = true)
    public List<StudySet> getAllStudySets() {
        return studySetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Card> getCardsByStudySet(Long studySetId) {
        return cardRepository.findByStudySetIdOrderByOrderAsc(studySetId);
    }

    @Transactional
    public StudySet createStudySet(String title, String description, String category, User creator) {
        log.info("Creating study set: {}", title);
        StudySet studySet = StudySet.builder()
                .title(title)
                .description(description)
                .category(category)
                .creator(creator)
                .isPublic(true)
                .build();
        return studySetRepository.save(studySet);
    }

    @Transactional
    public Card addCardToStudySet(Long studySetId, String frontText, String backText, String exampleSentence) {
        log.info("Adding card to study set: {}", studySetId);
        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        List<Card> existingCards = cardRepository.findByStudySetId(studySetId);
        int nextOrder = existingCards.size() + 1;

        Card card = Card.builder()
                .studySet(studySet)
                .cardType(CardType.VOCAB)
                .frontText(frontText)
                .backText(backText)
                .exampleSentence(exampleSentence)
                .order(nextOrder)
                .difficulty(1)
                .build();

        return cardRepository.save(card);
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
    public StudySet updateStudySet(Long id, String title, String description, String category) {
        log.info("Updating study set: id={}, title={}", id, title);
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        studySet.setTitle(title);
        studySet.setDescription(description);
        studySet.setCategory(category);
        return studySetRepository.save(studySet);
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
