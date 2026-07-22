package com.example.back_end.service;

import com.example.back_end.dto.request.TopikLevelRequest;
import com.example.back_end.dto.response.PageResponse;
import com.example.back_end.entity.TopikLevel;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.TopikLevelRepository;
import com.example.back_end.utils.SortUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public PageResponse<TopikLevel> getPaginatedLevels(int pageNo, int pageSize, String search, String... sorts) {
        int page = Math.max(0, pageNo - 1);
        Sort sort = SortUtils.parseSort(sorts);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        String safeSearch = (search != null && !search.trim().isEmpty()) ? "%" + search.trim().toLowerCase() + "%" : null;

        Page<TopikLevel> pageResult = topikLevelRepository.searchLevels(safeSearch, pageable);

        return PageResponse.<TopikLevel>builder()
                .pageNo(page + 1)
                .pageSize(pageSize)
                .totalPage(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .items(pageResult.getContent())
                .build();
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
                .build();
        return topikLevelRepository.save(level);
    }

    @Transactional
    public TopikLevel updateLevel(Long id, TopikLevelRequest request) {
        TopikLevel level = topikLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        
        level.setName(request.getName());
        level.setCode(request.getCode());
        return topikLevelRepository.save(level);
    }

    @Transactional
    public void deleteLevel(Long id) {
        TopikLevel level = topikLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        topikLevelRepository.delete(level);
    }
}
