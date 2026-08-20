package com.condor.nexussoft.timeclock.attendance.infrastructure.persistence;

import com.condor.nexussoft.timeclock.attendance.domain.port.out.AttendanceEvidenceLookupPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AttendanceEvidenceLookupAdapter implements AttendanceEvidenceLookupPort {

    private final NamedParameterJdbcTemplate jdbc;

    public AttendanceEvidenceLookupAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<String> findEvidenceKey(UUID tenantId, UUID recordId, Instant serverTime) {
        StringBuilder sql = new StringBuilder(
                "SELECT evidence_key FROM attendance_records "
                        + "WHERE tenant_id = :tenant AND id = :id AND evidence_key IS NOT NULL");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenant", tenantId)
                .addValue("id", recordId);
        if (serverTime != null) {
            // Poda de particiones: sin esta cota, PostgreSQL recorre todas las particiones de la tabla.
            sql.append(" AND server_time = :serverTime");
            params.addValue("serverTime", Timestamp.from(serverTime));
        }
        sql.append(" LIMIT 1");

        List<String> keys = jdbc.queryForList(sql.toString(), params, String.class);
        return keys.isEmpty() ? Optional.empty() : Optional.of(keys.get(0));
    }
}
