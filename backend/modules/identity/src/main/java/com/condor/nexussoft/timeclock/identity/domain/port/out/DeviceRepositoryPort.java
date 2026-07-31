package com.condor.nexussoft.timeclock.identity.domain.port.out;

import com.condor.nexussoft.timeclock.identity.domain.model.Device;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Persistencia de dispositivos vinculados (tabla {@code devices}). */
public interface DeviceRepositoryPort {

    Optional<Device> findByIdentifier(UUID tenantId, UUID userId, String deviceIdentifier);

    /** ¿El colaborador ya tiene al menos un dispositivo registrado? (decide TOFU). */
    boolean existsForUser(UUID tenantId, UUID userId);

    Device save(Device device);

    List<Device> listByUser(UUID tenantId, UUID userId);

    Optional<Device> findById(UUID tenantId, UUID deviceId);
}
