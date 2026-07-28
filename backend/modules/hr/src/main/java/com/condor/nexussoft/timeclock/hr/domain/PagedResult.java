package com.condor.nexussoft.timeclock.hr.domain;

import java.util.List;

/** Resultado paginado del dominio (sin dependencia de Spring Data). */
public record PagedResult<T>(List<T> content, long totalElements, int page, int size) {}
