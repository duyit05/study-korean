package com.example.back_end.service;

import com.example.back_end.dto.request.TopikLevelRequest;
import com.example.back_end.entity.TopikLevel;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.TopikLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopikLevelService {

    private final TopikLevelRepository topikLevelRepository;

    @Transactional(readOnly = true)
    public List<TopikLevel> getAllLevels() {
        return topikLevelRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TopikLevel getLevelById(Long id) {
        return topikLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional
    public TopikLevel createLevel(TopikLevelRequest request) {
        TopikLevel level = TopikLevel.builder()
                .name(request.getName())
                .code(request.getCode())
                .groupType(request.getGroupType())
                .build();
        return topikLevelRepository.save(level);
    }

    @Transactional
    public TopikLevel updateLevel(Long id, TopikLevelRequest request) {
        TopikLevel level = topikLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        
        level.setName(request.getName());
        level.setCode(request.getCode());
        level.setGroupType(request.getGroupType());
        return topikLevelRepository.save(level);
    }

    @Transactional
    public void deleteLevel(Long id) {
        TopikLevel level = topikLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        topikLevelRepository.delete(level);
    }
}
