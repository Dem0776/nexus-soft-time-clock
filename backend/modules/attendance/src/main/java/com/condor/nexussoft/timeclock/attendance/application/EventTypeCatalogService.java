package com.condor.nexussoft.timeclock.attendance.application;

import com.condor.nexussoft.timeclock.attendance.domain.EventTypeCatalog;
import com.condor.nexussoft.timeclock.attendance.domain.EventTypeSetting;
import com.condor.nexussoft.timeclock.attendance.domain.port.in.EventTypeCatalogUseCase;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.EventTypeConfigPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EventTypeCatalogService implements EventTypeCatalogUseCase {

    private final EventTypeConfigPort config;

    public EventTypeCatalogService(EventTypeConfigPort config) {
        this.config = config;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTypeSetting> list(UUID tenantId) {
        return EventTypeCatalog.merge(config.findByTenant(tenantId));
    }

    @Override
    @Transactional
    public List<EventTypeSetting> update(UUID tenantId, List<EventTypeSetting> settings) {
        // ENTRADA/SALIDA son núcleo: se ignoran; solo se persisten los tipos configurables.
        List<EventTypeSetting> configurable = settings.stream()
                .filter(s -> EventTypeCatalog.isConfigurable(s.eventType()))
                .toList();
        config.replaceAll(tenantId, configurable);
        return list(tenantId);
    }
}
