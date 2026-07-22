package com.condor.nexussoft.timeclock.scheduling.infrastructure.persistence;

import com.condor.nexussoft.timeclock.scheduling.domain.Shift;
import com.condor.nexussoft.timeclock.scheduling.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ShiftPersistenceAdapter implements ShiftRepositoryPort {

    private final ShiftJpaRepository shiftRepo;

    public ShiftPersistenceAdapter(ShiftJpaRepository shiftRepo) {
        this.shiftRepo = shiftRepo;
    }

    @Override
    public Shift save(Shift s) {
        shiftRepo.save(toEntity(s));
        return s;
    }

    @Override
    public Shift update(Shift s) {
        ShiftJpaEntity e = shiftRepo.findByIdAndTenantId(s.id(), s.tenantId()).orElseThrow();
        e.apply(s.name(), s.startTime(), s.endTime(), s.crossesMidnight(), s.breakMinutes(),
                s.lateToleranceMin(), s.earlyToleranceMin(), s.windowBeforeMin(), s.windowAfterMin());
        shiftRepo.save(e);
        return s;
    }

    @Override
    public Optional<Shift> findByIdAndTenant(UUID id, UUID tenantId) {
        return shiftRepo.findByIdAndTenantId(id, tenantId).map(this::toShift);
    }

    @Override
    public List<Shift> findByScheduleAndTenant(UUID scheduleId, UUID tenantId) {
        return shiftRepo.findByScheduleIdAndTenantId(scheduleId, tenantId).stream().map(this::toShift).toList();
    }

    private ShiftJpaEntity toEntity(Shift s) {
        return new ShiftJpaEntity(s.id(), s.tenantId(), s.scheduleId(), s.name(), s.startTime(), s.endTime(),
                s.crossesMidnight(), s.breakMinutes(), s.lateToleranceMin(), s.earlyToleranceMin(),
                s.windowBeforeMin(), s.windowAfterMin());
    }

    private Shift toShift(ShiftJpaEntity e) {
        return new Shift(e.getId(), e.getTenantId(), e.getScheduleId(), e.getName(), e.getStartTime(),
                e.getEndTime(), e.isCrossesMidnight(), e.getBreakMinutes(), e.getLateToleranceMin(),
                e.getEarlyToleranceMin(), e.getWindowBeforeMin(), e.getWindowAfterMin());
    }
}
