package com.condor.nexussoft.timeclock.attendance.infrastructure.persistence;

import com.condor.nexussoft.timeclock.attendance.domain.AttendanceEventType;
import com.condor.nexussoft.timeclock.attendance.domain.EventTypeSetting;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.EventTypeConfigPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class EventTypeConfigPersistenceAdapter implements EventTypeConfigPort {

    private final EventTypeConfigJpaRepository jpa;

    public EventTypeConfigPersistenceAdapter(EventTypeConfigJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Map<AttendanceEventType, EventTypeSetting> findByTenant(UUID tenantId) {
        return jpa.findByTenantId(tenantId).stream()
                .collect(Collectors.toMap(
                        e -> AttendanceEventType.valueOf(e.getEventType()),
                        e -> new EventTypeSetting(AttendanceEventType.valueOf(e.getEventType()),
                                e.isEnabled(), e.getLabel())));
    }

    @Override
    public void replaceAll(UUID tenantId, List<EventTypeSetting> settings) {
        jpa.deleteByTenantId(tenantId);
        List<EventTypeConfigJpaEntity> entities = settings.stream()
                .map(s -> new EventTypeConfigJpaEntity(UUID.randomUUID(), tenantId,
                        s.eventType().name(), s.enabled(), s.label()))
                .toList();
        jpa.saveAll(entities);
    }
}
