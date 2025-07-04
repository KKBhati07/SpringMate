package com.example.SpringMate.Util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private List<T> items;
    private int currentPage;
    private long totalItems;
    private int totalPages;
}
