package com.condor.nexussoft.timeclock.reporting.application;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Consulta las filas del reporte de asistencia con filtros (rango, estado). */
@Service
public class AttendanceReportService {

    private static final int MAX_ROWS = 5000;

    private final JdbcTemplate jdbc;

    public AttendanceReportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ReportRow> rows(UUID tenantId, Instant from, Instant to, String status) {
        StringBuilder sql = new StringBuilder(
                "SELECT server_time, user_id, event_type, status, rejection_reason, "
                        + "ST_Y(location::geometry) AS lat, ST_X(location::geometry) AS lng "
                        + "FROM attendance_records WHERE tenant_id = ? AND server_time BETWEEN ? AND ?");
        List<Object> args = new ArrayList<>();
        args.add(tenantId);
        args.add(Timestamp.from(from));
        args.add(Timestamp.from(to));
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            args.add(status);
        }
        sql.append(" ORDER BY server_time DESC LIMIT ").append(MAX_ROWS);

        return jdbc.query(sql.toString(), (rs, i) -> new ReportRow(
                rs.getTimestamp("server_time").toInstant(),
                rs.getObject("user_id", UUID.class),
                rs.getString("event_type"),
                rs.getString("status"),
                rs.getString("rejection_reason"),
                rs.getDouble("lat"),
                rs.getDouble("lng")), args.toArray());
    }
}
