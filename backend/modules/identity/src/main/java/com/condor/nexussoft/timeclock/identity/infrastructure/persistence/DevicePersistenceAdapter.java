package com.condor.nexussoft.timeclock.identity.infrastructure.persistence;

import com.condor.nexussoft.timeclock.identity.domain.model.Device;
import com.condor.nexussoft.timeclock.identity.domain.port.out.DeviceRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DevicePersistenceAdapter implements DeviceRepositoryPort {

    private final DeviceJpaRepository jpa;

    public DevicePersistenceAdapter(DeviceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Device> findByIdentifier(UUID tenantId, UUID userId, String deviceIdentifier) {
        return jpa.findByTenantIdAndUserIdAndDeviceIdentifier(tenantId, userId, deviceIdentifier)
                .map(this::toDomain);
    }

    @Override
    public boolean existsForUser(UUID tenantId, UUID userId) {
        return jpa.existsByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public Device save(Device device) {
        return toDomain(jpa.save(toEntity(device)));
    }

    @Override
    public List<Device> listByUser(UUID tenantId, UUID userId) {
        return jpa.findByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId, userId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Device> findById(UUID tenantId, UUID deviceId) {
        return jpa.findByTenantIdAndId(tenantId, deviceId).map(this::toDomain);
    }

    private DeviceJpaEntity toEntity(Device d) {
        return new DeviceJpaEntity(d.id(), d.tenantId(), d.userId(), d.deviceIdentifier(), d.platform(),
                d.model(), d.osVersion(), d.trusted(), d.active() ? "ACTIVE" : "BLOCKED", d.lastSeenAt());
    }

    private Device toDomain(DeviceJpaEntity e) {
        return new Device(e.getId(), e.getTenantId(), e.getUserId(), e.getDeviceIdentifier(), e.getPlatform(),
                e.getModel(), e.getOsVersion(), e.isTrusted(), "ACTIVE".equals(e.getStatus()),
                e.getLastSeenAt(), e.getCreatedAt());
    }
}
