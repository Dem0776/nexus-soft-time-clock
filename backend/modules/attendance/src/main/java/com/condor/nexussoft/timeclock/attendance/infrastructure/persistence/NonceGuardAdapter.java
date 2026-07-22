package com.condor.nexussoft.timeclock.attendance.infrastructure.persistence;

import com.condor.nexussoft.timeclock.attendance.domain.port.out.NonceGuardPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Consumo de nonce durable (RN-26). El INSERT con {@code ON CONFLICT DO NOTHING} sobre el
 * índice único (tenant_id, nonce) devuelve 0 filas si el nonce ya fue consumido → replay.
 */
@Repository
public class NonceGuardAdapter implements NonceGuardPort {

    private final JdbcTemplate jdbc;

    public NonceGuardAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean tryConsume(UUID tenantId, UUID workSiteId, String nonce, UUID attendanceId) {
        int rows = jdbc.update(
                "INSERT INTO qr_nonce_consumed (id, tenant_id, work_site_id, nonce, attendance_id) "
                        + "VALUES (gen_random_uuid(), ?, ?, ?, ?) "
                        + "ON CONFLICT (tenant_id, nonce) DO NOTHING",
                tenantId, workSiteId, nonce, attendanceId);
        return rows > 0;
    }
}
