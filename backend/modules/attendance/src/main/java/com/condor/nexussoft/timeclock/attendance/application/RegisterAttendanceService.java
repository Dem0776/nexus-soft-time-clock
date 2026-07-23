package com.condor.nexussoft.timeclock.attendance.application;

import com.condor.nexussoft.timeclock.attendance.domain.*;
import com.condor.nexussoft.timeclock.attendance.domain.event.AttendanceRegistered;
import com.condor.nexussoft.timeclock.attendance.domain.event.AttendanceRejected;
import com.condor.nexussoft.timeclock.attendance.domain.port.in.*;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Núcleo del sistema (CU-02). Aplica, en orden, las validaciones de:
 * idempotencia → QR firmado → antifraude → geocerca/precisión → anti-replay (nonce),
 * fija la hora de servidor (RN-11), persiste el registro (aceptado o rechazado con motivo)
 * y publica el evento de dominio.
 */
@Service
public class RegisterAttendanceService implements RegisterAttendanceUseCase {

    private final AttendanceRepositoryPort attendance;
    private final IdempotencyStorePort idempotency;
    private final NonceGuardPort nonceGuard;
    private final QrValidationPort qrValidation;
    private final GeofenceCheckPort geofenceCheck;
    private final FraudCheckPort fraudCheck;
    private final WorkSitePolicyPort sitePolicy;
    private final SchedulePolicyPort schedulePolicy;
    private final EventTypeConfigPort eventTypeConfig;
    private final AttendanceEventPublisherPort events;
    private final Clock clock;

    public RegisterAttendanceService(AttendanceRepositoryPort attendance, IdempotencyStorePort idempotency,
                                     NonceGuardPort nonceGuard, QrValidationPort qrValidation,
                                     GeofenceCheckPort geofenceCheck, FraudCheckPort fraudCheck,
                                     WorkSitePolicyPort sitePolicy, SchedulePolicyPort schedulePolicy,
                                     EventTypeConfigPort eventTypeConfig, AttendanceEventPublisherPort events,
                                     Clock clock) {
        this.attendance = attendance;
        this.idempotency = idempotency;
        this.nonceGuard = nonceGuard;
        this.qrValidation = qrValidation;
        this.geofenceCheck = geofenceCheck;
        this.fraudCheck = fraudCheck;
        this.sitePolicy = sitePolicy;
        this.schedulePolicy = schedulePolicy;
        this.eventTypeConfig = eventTypeConfig;
        this.events = events;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AttendanceResult register(UUID tenantId, UUID userId, RegisterAttendanceCommand cmd) {
        // 0) Idempotencia: reenvío del mismo UUID → devuelve el resultado previo (RN-51).
        var previous = idempotency.find(tenantId, cmd.operationUuid());
        if (previous.isPresent()) {
            return previous.get();
        }

        Instant now = clock.instant();              // hora de servidor autoritativa (RN-11)
        UUID recordId = UUID.randomUUID();
        AttendanceEventType eventType = AttendanceEventType.valueOf(cmd.eventType());
        List<String> flags = new ArrayList<>();
        RejectionReason reason = null;

        // 1) QR firmado + vigencia + coincidencia de tenant/centro (RN-25).
        QrValidationPort.QrCheck qr = qrValidation.verify(cmd.qrToken());
        boolean qrOk = qr.valid() && !qr.expired()
                && tenantId.equals(qr.tenantId()) && cmd.workSiteId().equals(qr.workSiteId());
        if (!qrOk) {
            reason = RejectionReason.INVALID_QR;
        }

        // 1.5) Tipo de evento habilitado para la empresa (HU-12 CA1). Solo se consulta para tipos
        //       intermedios; ENTRADA/SALIDA son núcleo y siempre están habilitados.
        if (reason == null && EventTypeCatalog.isConfigurable(eventType)
                && !EventTypeCatalog.isEnabled(eventType, eventTypeConfig.findByTenant(tenantId))) {
            reason = RejectionReason.EVENT_TYPE_DISABLED;
        }

        // 2) Antifraude (mock, root, spoofing, GPS) — RN-20..RN-28.
        FraudCheckPort.FraudCheckResult fraud = fraudCheck.evaluate(cmd.mockLocation(),
                cmd.rootedOrJailbroken(), cmd.gpsSpoofApp(), cmd.gpsDisabled(), cmd.deviceTrusted());
        flags.addAll(fraud.flagTypes());
        if (reason == null && fraud.blocked()) {
            reason = RejectionReason.valueOf(fraud.blockingReason());
        }

        // 3) Geocerca + precisión GPS (RN-13, RN-14). El umbral de precisión es por-centro (HU-10);
        //    si el centro no lo define, se usa el default de plataforma que expone la geocerca.
        GeofenceCheckPort.GeofenceCheck geo = geofenceCheck.check(tenantId, cmd.workSiteId(),
                cmd.latitude(), cmd.longitude());
        Double distance = geo.exists() ? geo.distanceM() : null;
        WorkSitePolicyPort.SitePolicy policy = sitePolicy.find(tenantId, cmd.workSiteId());
        double accuracyMax = policy.gpsAccuracyMaxM() != null ? policy.gpsAccuracyMaxM() : geo.accuracyMaxM();
        if (reason == null) {
            if (cmd.accuracyM() > accuracyMax) {
                reason = RejectionReason.LOW_GPS_ACCURACY;
            } else if (!geo.exists() || !geo.withinRadius()) {
                reason = RejectionReason.OUT_OF_GEOFENCE;
            }
        }

        // 3.4) Ventana de horario del turno asignado (RN-15, HU-10 CA1). Sin turno vigente no restringe.
        if (reason == null && schedulePolicy.check(tenantId, userId, cmd.workSiteId(), now)
                == SchedulePolicyPort.ScheduleCheck.OUT_OF_WINDOW) {
            reason = RejectionReason.OUT_OF_SCHEDULE;
        }

        // 3.5) Secuencia coherente de la jornada (RN-12): entrada/salida emparejadas en el mismo
        //       centro (HU-11 CA1), sin dobles descansos ni eventos fuera de orden (HU-12 CA2).
        if (reason == null) {
            reason = AttendanceSequenceValidator
                    .validate(attendance.findLastAcceptedEvent(tenantId, userId), eventType, cmd.workSiteId())
                    .orElse(null);
        }

        // 3.6) Políticas obligatorias del centro: evidencia fotográfica (HU-13 CA1) y biometría (HU-14 CA1).
        if (reason == null && policy.requirePhoto() && cmd.evidenceKey() == null) {
            reason = RejectionReason.PHOTO_REQUIRED;
        }
        if (reason == null && policy.requireBiometric() && !cmd.biometricVerified()) {
            reason = RejectionReason.BIOMETRIC_REQUIRED;
        }

        // 4) Anti-replay: consumir el nonce del QR (RN-26).
        if (reason == null) {
            boolean consumed = nonceGuard.tryConsume(tenantId, cmd.workSiteId(), qr.nonce(), recordId);
            if (!consumed) {
                reason = RejectionReason.REPLAY_DETECTED;
            }
        }

        AttendanceStatus status = reason == null ? AttendanceStatus.ACCEPTED : AttendanceStatus.REJECTED;

        AttendanceRecord record = buildRecord(recordId, tenantId, userId, cmd, now, status, reason,
                qrOk ? qr.nonce() : null, distance, flags);
        attendance.save(record);

        AttendanceResult result = new AttendanceResult(recordId, status.name(),
                reason == null ? null : reason.name(), now, distance, flags);
        idempotency.save(tenantId, cmd.operationUuid(), result);

        publishEvent(record, reason, now);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSummary> history(UUID tenantId, UUID userId, int limit) {
        return attendance.findRecentByUser(tenantId, userId, Math.min(Math.max(limit, 1), 200));
    }

    private AttendanceRecord buildRecord(UUID recordId, UUID tenantId, UUID userId, RegisterAttendanceCommand cmd,
                                         Instant now, AttendanceStatus status, RejectionReason reason,
                                         String nonce, Double distance, List<String> flags) {
        Instant deviceTime = cmd.deviceTimeEpochMs() == null ? null : Instant.ofEpochMilli(cmd.deviceTimeEpochMs());
        Integer skew = deviceTime == null ? null : (int) (now.getEpochSecond() - deviceTime.getEpochSecond());
        Evidence evidence = cmd.evidenceKey() == null ? null
                : new Evidence(cmd.evidenceBucket(), cmd.evidenceKey(), cmd.evidenceHash());
        GpsFix gps = new GpsFix(cmd.latitude(), cmd.longitude(), cmd.accuracyM());

        return new AttendanceRecord(recordId, tenantId, now, userId, cmd.workSiteId(),
                AttendanceEventType.valueOf(cmd.eventType()), status, reason, gps, distance,
                cmd.deviceId(), deviceTime, skew, nonce, cmd.operationUuid(),
                normalizeSource(cmd.source()), cmd.biometricVerified(), evidence,
                buildValidationsJson(flags, reason, distance));
    }

    private void publishEvent(AttendanceRecord record, RejectionReason reason, Instant now) {
        if (record.isAccepted()) {
            events.publish(AttendanceRegistered.of(record.tenantId(), record.id(), record.userId(),
                    record.workSiteId(), record.eventType().name(), now));
        } else {
            events.publish(AttendanceRejected.of(record.tenantId(), record.id(), record.userId(),
                    reason.name(), now));
        }
    }

    private String normalizeSource(String source) {
        return "OFFLINE_SYNC".equalsIgnoreCase(source) ? "OFFLINE_SYNC" : "ONLINE";
    }

    private String buildValidationsJson(List<String> flags, RejectionReason reason, Double distance) {
        String flagsArray = flags.stream().map(f -> "\"" + f + "\"").collect(Collectors.joining(",", "[", "]"));
        String reasonJson = reason == null ? "null" : "\"" + reason.name() + "\"";
        String distanceJson = distance == null ? "null" : distance.toString();
        return "{\"flags\":" + flagsArray + ",\"rejectionReason\":" + reasonJson
                + ",\"distanceToSiteM\":" + distanceJson + "}";
    }
}
