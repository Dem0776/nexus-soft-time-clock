package com.condor.nexussoft.timeclock.reporting.application;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Lado de lectura (CQRS): consulta indicadores agregados. En esta iteración consulta las
 * tablas transaccionales directamente; las proyecciones dedicadas por eventos se optimizan después.
 */
@Service
public class DashboardService {

    private final JdbcTemplate jdbc;

    public DashboardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Cacheable(value = "dashboardSummary", key = "#tenantId")
    public DashboardSummary summary(UUID tenantId) {
        return new DashboardSummary(
                count("SELECT count(*) FROM attendance_records WHERE tenant_id = ? "
                        + "AND status = 'ACCEPTED' AND server_time >= date_trunc('day', now())", tenantId),
                count("SELECT count(*) FROM attendance_records WHERE tenant_id = ? "
                        + "AND status = 'REJECTED' AND server_time >= date_trunc('day', now())", tenantId),
                count("SELECT count(*) FROM incidents WHERE tenant_id = ? AND status = 'OPEN'", tenantId),
                count("SELECT count(*) FROM users WHERE tenant_id = ? AND status = 'ACTIVE'", tenantId),
                count("SELECT count(*) FROM work_sites WHERE tenant_id = ? AND status = 'ACTIVE'", tenantId));
    }

    private long count(String sql, UUID tenantId) {
        Long value = jdbc.queryForObject(sql, Long.class, tenantId);
        return value == null ? 0L : value;
    }
}
