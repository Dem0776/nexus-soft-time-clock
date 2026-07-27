package com.condor.nexussoft.timeclock.attendance.infrastructure.persistence;

import com.condor.nexussoft.timeclock.attendance.domain.port.out.NonceGuardPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Consumo de nonce durable (RN-26). El INSERT con {@code ON CONFLICT DO NOTHING} sobre el
 * índice único (tenant_id, nonce, user_id, consumed_on) devuelve 0 filas si ese usuario ya
 * consumió este nonce ese mismo día → replay. Un QR de vigencia larga sigue permitiendo que
 * otros usuarios (u otros días) lo consuman.
 */
@Repository
public class NonceGuardAdapter implements NonceGuardPort {

    private final JdbcTemplate jdbc;

    public NonceGuardAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean tryConsume(UUID tenantId, UUID workSiteId, String nonce, UUID userId, LocalDate consumedOn, UUID attendanceId) {
        int rows = jdbc.update(
                "INSERT INTO qr_nonce_consumed (id, tenant_id, work_site_id, nonce, user_id, consumed_on, attendance_id) "
                        + "VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ?) "
                        + "ON CONFLICT (tenant_id, nonce, user_id, consumed_on) DO NOTHING",
                tenantId, workSiteId, nonce, userId, Date.valueOf(consumedOn), attendanceId);
        return rows > 0;
    }
}
