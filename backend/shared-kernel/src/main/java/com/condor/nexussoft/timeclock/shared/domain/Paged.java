package com.condor.nexussoft.timeclock.shared.domain;

import java.util.List;

/** Resultado paginado de dominio (independiente de framework). */
public record Paged<T>(List<T> items, int page, int size, long total) {
}
