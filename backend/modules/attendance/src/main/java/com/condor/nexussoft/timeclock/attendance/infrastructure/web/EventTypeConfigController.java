package com.condor.nexussoft.timeclock.attendance.infrastructure.web;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.EventTypeCatalogUseCase;
import com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto.EventTypeSettingDto;
import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Catálogo de tipos de evento por empresa (HU-12 CA1). La lectura está disponible para cualquier
 * usuario autenticado (la app móvil arma su UI con los tipos habilitados); la edición exige
 * {@code schedule:manage} (rol administrador de la empresa).
 */
@RestController
@RequestMapping("/api/v1/attendance/event-types")
public class EventTypeConfigController {

    private final EventTypeCatalogUseCase catalog;

    public EventTypeConfigController(EventTypeCatalogUseCase catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<EventTypeSettingDto> list() {
        return catalog.list(TenantContext.require()).stream().map(EventTypeSettingDto::from).toList();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('schedule:manage')")
    public List<EventTypeSettingDto> update(@Valid @RequestBody List<EventTypeSettingDto> settings) {
        List<EventTypeSettingDto> result = catalog.update(TenantContext.require(),
                        settings.stream().map(EventTypeSettingDto::toDomain).toList())
                .stream().map(EventTypeSettingDto::from).toList();
        return result;
    }
}
