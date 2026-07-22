package com.condor.nexussoft.timeclock.attendance.infrastructure.persistence;

import com.condor.nexussoft.timeclock.attendance.domain.AttendanceRecord;
import com.condor.nexussoft.timeclock.attendance.domain.Evidence;
import com.condor.nexussoft.timeclock.attendance.domain.port.in.AttendanceSummary;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.AttendanceRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AttendancePersistenceAdapter implements AttendanceRepositoryPort {

    private final AttendanceRecordJpaRepository jpa;

    public AttendancePersistenceAdapter(AttendanceRecordJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(AttendanceRecord r) {
        Evidence e = r.evidence();
        jpa.save(new AttendanceRecordJpaEntity(
                r.id(), r.tenantId(), r.serverTime(), r.userId(), r.workSiteId(),
                r.eventType().name(), r.status().name(),
                r.rejectionReason() == null ? null : r.rejectionReason().name(),
                GeoSupport.point(r.gps().latitude(), r.gps().longitude()),
                r.gps().accuracyM(), r.distanceToSiteM(),
                null,                       // device_id (uuid) — no resuelto en esta iteración
                r.deviceTime(), r.timeSkewSeconds(), r.operationUuid(), r.source(),
                r.biometricVerified(),
                e == null ? null : e.bucket(), e == null ? null : e.key(), e == null ? null : e.hash(),
                r.validationsJson()));
    }

    @Override
    public List<AttendanceSummary> findRecentByUser(UUID tenantId, UUID userId, int limit) {
        return jpa.findByTenantIdAndUserIdOrderByServerTimeDesc(tenantId, userId, PageRequest.of(0, limit))
                .stream()
                .map(e -> new AttendanceSummary(
                        e.getId(), e.getEventType(), e.getStatus(), e.getRejectionReason(),
                        e.getServerTime(), e.getLocation().getY(), e.getLocation().getX()))
                .toList();
    }
}
