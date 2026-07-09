package com.example.back_end.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private int pageNo;
    private int pageSize;
    private int totalPage;
    private long totalElements;
    private List<T> items;
}
