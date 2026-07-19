package com.example.back_end.service;

import com.example.back_end.entity.Card;
import com.example.back_end.entity.CardProgress;
import com.example.back_end.entity.StudentProfile;
import com.example.back_end.entity.User;
import com.example.back_end.enums.CardState;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.CardProgressRepository;
import com.example.back_end.repository.CardRepository;
import com.example.back_end.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardProgressService {

    private final CardProgressRepository cardProgressRepository;
    private final CardRepository cardRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserService userService;

    @Transactional
    public void updateCardProgress(Long cardId, String status) {
        User student = userService.getCurrentUser();
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        CardProgress progress = cardProgressRepository.findByStudentIdAndCardId(student.getId(), cardId)
                .orElseGet(() -> CardProgress.builder()
                        .student(student)
                        .card(card)
                        .build());

        CardState oldState = progress.getState();
        CardState newState;
        int xpChange = 0;

        if ("learned".equalsIgnoreCase(status)) {
            newState = CardState.MASTERED;
            if (oldState != CardState.MASTERED) {
                progress.setRepetitions(1);
                xpChange = 10;
            }
        } else if ("review".equalsIgnoreCase(status)) {
            newState = CardState.REVIEW;
            progress.setRepetitions(0);
        } else {
            newState = CardState.NEW;
            progress.setRepetitions(0);
        }

        progress.setState(newState);
        cardProgressRepository.save(progress);

        if (xpChange > 0) {
            StudentProfile profile = studentProfileRepository.findByUserId(student.getId())
                    .orElseGet(() -> StudentProfile.builder()
                            .user(student)
                            .xp(0)
                            .level(1)
                            .streak(0)
                            .build());
            profile.setXp(profile.getXp() + xpChange);
            profile.setLevel((profile.getXp() / 1000) + 1);
            studentProfileRepository.save(profile);
            log.info("Student {} earned {} XP for mastering card {}. Total XP: {}, Level: {}",
                    student.getUsername(), xpChange, cardId, profile.getXp(), profile.getLevel());
        }
    }
}
