package com.condor.nexussoft.timeclock.platform.web;

import java.util.List;

/**
 * Envelope uniforme de paginación para las respuestas de la API (RF: paginación,
 * ordenamiento y filtros estandarizados). Independiente de Spring Data.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}
