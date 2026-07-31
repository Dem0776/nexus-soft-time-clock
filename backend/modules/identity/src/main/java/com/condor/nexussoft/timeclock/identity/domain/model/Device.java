package com.condor.nexussoft.timeclock.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Dispositivo vinculado a un colaborador (RF-28, RN-27). El enrolamiento sigue TOFU
 * (trust-on-first-use): el primer dispositivo del colaborador se confía automáticamente y
 * los adicionales quedan {@code PENDING} hasta que un administrador los apruebe.
 *
 * <p>El estado físico se guarda en dos columnas ({@code is_trusted} + {@code status}); este
 * modelo las expone de forma unificada como {@link DeviceStatus}.
 */
public class Device {

    private final UUID id;
    private final UUID tenantId;
    private final UUID userId;
    private final String deviceIdentifier;
    private final String platform;
    private final String model;
    private final String osVersion;
    private final Instant createdAt;
    private boolean trusted;
    private boolean active;      // status = active ? ACTIVE : BLOCKED
    private Instant lastSeenAt;

    public Device(UUID id, UUID tenantId, UUID userId, String deviceIdentifier, String platform,
                  String model, String osVersion, boolean trusted, boolean active,
                  Instant lastSeenAt, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.deviceIdentifier = deviceIdentifier;
        this.platform = platform;
        this.model = model;
        this.osVersion = osVersion;
        this.trusted = trusted;
        this.active = active;
        this.lastSeenAt = lastSeenAt;
        this.createdAt = createdAt;
    }

    /** Primer dispositivo del colaborador: se confía en su primer uso (TOFU). */
    public static Device enrollTrusted(UUID tenantId, UUID userId, String deviceIdentifier,
                                       String platform, String model, String osVersion, Instant now) {
        return new Device(UUID.randomUUID(), tenantId, userId, deviceIdentifier, platform,
                model, osVersion, true, true, now, now);
    }

    /** Dispositivo adicional no reconocido: queda a la espera de aprobación de un admin. */
    public static Device enrollPending(UUID tenantId, UUID userId, String deviceIdentifier,
                                       String platform, String model, String osVersion, Instant now) {
        return new Device(UUID.randomUUID(), tenantId, userId, deviceIdentifier, platform,
                model, osVersion, false, true, now, now);
    }

    /** Un administrador confía el dispositivo (lo reactiva si estaba bloqueado). */
    public void approve(Instant now) {
        this.trusted = true;
        this.active = true;
        this.lastSeenAt = now;
    }

    /** Un administrador revoca/bloquea el dispositivo: deja de estar reconocido. */
    public void revoke(Instant now) {
        this.trusted = false;
        this.active = false;
        this.lastSeenAt = now;
    }

    /** Marca la última vez que el dispositivo se presentó (registro de asistencia). */
    public void touch(Instant now) {
        this.lastSeenAt = now;
    }

    public DeviceStatus status() {
        if (!active) {
            return DeviceStatus.BLOCKED;
        }
        return trusted ? DeviceStatus.TRUSTED : DeviceStatus.PENDING;
    }

    /** El dispositivo puede registrar asistencia sin marca (reconocido y confiado). */
    public boolean isRecognized() {
        return active && trusted;
    }

    public UUID id()                { return id; }
    public UUID tenantId()          { return tenantId; }
    public UUID userId()            { return userId; }
    public String deviceIdentifier() { return deviceIdentifier; }
    public String platform()        { return platform; }
    public String model()           { return model; }
    public String osVersion()       { return osVersion; }
    public boolean trusted()        { return trusted; }
    public boolean active()         { return active; }
    public Instant lastSeenAt()     { return lastSeenAt; }
    public Instant createdAt()      { return createdAt; }
}
