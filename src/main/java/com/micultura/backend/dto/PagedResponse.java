package com.micultura.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic wrapper for paginated API responses.
 * Mirrors the structure the frontend expects.
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    /** Convenience factory from a Spring Data {@link Page}. */
    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
