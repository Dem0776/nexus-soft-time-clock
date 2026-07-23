package com.condor.nexussoft.timeclock.reporting.application;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Reporte agregado de asistencia por colaborador (RF-11).
 *
 * <p>Deriva todo al vuelo desde las tablas transaccionales (no depende del read-model {@code work_days},
 * que hoy no se puebla). Supuestos de cálculo, acordados con el negocio:
 * <ul>
 *   <li><b>Horas trabajadas</b>: por día, {@code max(SALIDA) - min(ENTRADA)} de marcaciones aceptadas,
 *       menos el descanso del turno asignado.</li>
 *   <li><b>Horas extra</b>: por día, {@code max(0, trabajadas - jornada neta del turno)}; 0 si el
 *       colaborador no tiene turno vigente.</li>
 *   <li><b>Días esperados</b>: días hábiles (lunes-viernes) del rango, acotados a la vigencia del turno
 *       asignado cuando existe. La personalización por {@code schedule.config_json} queda como extensión
 *       futura (hoy ese JSON no almacena la máscara de días).</li>
 *   <li><b>Colaboradores</b>: usuarios del tenant que no son administradores de plataforma; se incluyen
 *       activos e inactivos (el front filtra por estado).</li>
 * </ul>
 */
@Service
public class AttendanceSummaryService {

    private static final int MAX_ROWS = 5000;

    /** Una sola consulta con CTEs; el llamador acota por tenant. */
    private static final String SQL = """
            WITH emp AS (
                SELECT id, employee_code, first_name, last_name, status
                FROM users
                WHERE tenant_id = :tenant AND is_platform_admin = false
            ),
            assign AS (
                SELECT DISTINCT ON (sa.user_id)
                       sa.user_id,
                       sa.work_site_id,
                       sh.break_minutes AS shift_break,
                       GREATEST(0,
                           (EXTRACT(EPOCH FROM (sh.end_time - sh.start_time)) / 60.0
                             + CASE WHEN sh.crosses_midnight THEN 1440 ELSE 0 END)
                           - sh.break_minutes) AS shift_net_minutes,
                       sa.valid_from,
                       sa.valid_to
                FROM shift_assignments sa
                JOIN shifts sh ON sh.id = sa.shift_id
                WHERE sa.tenant_id = :tenant
                  AND sa.valid_from <= :toDate
                  AND (sa.valid_to IS NULL OR sa.valid_to >= :fromDate)
                ORDER BY sa.user_id, sa.valid_from DESC
            ),
            day_punch AS (
                SELECT ar.user_id,
                       (ar.server_time)::date AS d,
                       min(ar.server_time) FILTER (WHERE ar.event_type = 'ENTRADA') AS first_in,
                       max(ar.server_time) FILTER (WHERE ar.event_type = 'SALIDA')  AS last_out
                FROM attendance_records ar
                WHERE ar.tenant_id = :tenant AND ar.status = 'ACCEPTED'
                  AND ar.server_time >= :fromTs AND ar.server_time < :toTsExcl
                GROUP BY ar.user_id, (ar.server_time)::date
            ),
            day_work AS (
                SELECT dp.user_id,
                       (dp.first_in IS NOT NULL) AS attended,
                       CASE WHEN dp.first_in IS NOT NULL AND dp.last_out IS NOT NULL AND dp.last_out > dp.first_in
                            THEN GREATEST(0, EXTRACT(EPOCH FROM (dp.last_out - dp.first_in)) / 60.0
                                             - COALESCE(a.shift_break, 0))
                            ELSE 0 END AS worked_min,
                       a.shift_net_minutes
                FROM day_punch dp
                LEFT JOIN assign a ON a.user_id = dp.user_id
            ),
            worked AS (
                SELECT user_id,
                       count(*) FILTER (WHERE attended) AS attended_days,
                       COALESCE(sum(worked_min), 0) AS worked_minutes,
                       COALESCE(sum(CASE WHEN shift_net_minutes IS NOT NULL
                                         THEN GREATEST(0, worked_min - shift_net_minutes)
                                         ELSE 0 END), 0) AS overtime_minutes
                FROM day_work
                GROUP BY user_id
            ),
            inc AS (
                SELECT user_id,
                       count(*) FILTER (WHERE type = 'RETARDO') AS retardos,
                       count(*) FILTER (WHERE type IN ('PERMISO', 'JUSTIFICACION')
                                           OR (type = 'FALTA' AND status IN ('APPROVED', 'RESOLVED'))) AS justified,
                       count(*) FILTER (WHERE type = 'FALTA' AND status IN ('OPEN', 'REJECTED')) AS unjustified
                FROM incidents
                WHERE tenant_id = :tenant AND incident_date >= :fromDate AND incident_date <= :toDate
                GROUP BY user_id
            ),
            last_site AS (
                SELECT DISTINCT ON (ar.user_id) ar.user_id, ws.name AS site_name
                FROM attendance_records ar
                JOIN work_sites ws ON ws.id = ar.work_site_id
                WHERE ar.tenant_id = :tenant
                  AND ar.server_time >= :fromTs AND ar.server_time < :toTsExcl
                ORDER BY ar.user_id, ar.server_time DESC
            ),
            expected AS (
                SELECT e.id AS user_id,
                       (SELECT count(*) FROM generate_series(
                                GREATEST(:fromDate, COALESCE(a.valid_from, :fromDate)),
                                LEAST(:toDate, COALESCE(a.valid_to, :toDate)),
                                interval '1 day') g
                        WHERE EXTRACT(dow FROM g) BETWEEN 1 AND 5) AS expected_days
                FROM emp e
                LEFT JOIN assign a ON a.user_id = e.id
            )
            SELECT e.employee_code,
                   e.first_name,
                   e.last_name,
                   e.status,
                   COALESCE(ls.site_name, asite.name) AS work_center,
                   COALESCE(x.expected_days, 0)       AS expected_days,
                   COALESCE(w.attended_days, 0)       AS attended_days,
                   COALESCE(i.justified, 0)           AS justified,
                   COALESCE(i.unjustified, 0)         AS unjustified,
                   COALESCE(i.retardos, 0)            AS retardos,
                   COALESCE(w.worked_minutes, 0)      AS worked_minutes,
                   COALESCE(w.overtime_minutes, 0)    AS overtime_minutes
            FROM emp e
            LEFT JOIN worked w      ON w.user_id = e.id
            LEFT JOIN inc i         ON i.user_id = e.id
            LEFT JOIN last_site ls  ON ls.user_id = e.id
            LEFT JOIN expected x    ON x.user_id = e.id
            LEFT JOIN assign a2     ON a2.user_id = e.id
            LEFT JOIN work_sites asite ON asite.id = a2.work_site_id
            ORDER BY e.first_name, e.last_name
            LIMIT %d
            """.formatted(MAX_ROWS);

    private final NamedParameterJdbcTemplate jdbc;

    public AttendanceSummaryService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<AttendanceSummaryRow> summary(UUID tenantId, LocalDate from, LocalDate to) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenant", tenantId)
                .addValue("fromDate", from)
                .addValue("toDate", to)
                .addValue("fromTs", Timestamp.from(from.atStartOfDay().toInstant(ZoneOffset.UTC)))
                .addValue("toTsExcl", Timestamp.from(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)));

        return jdbc.query(SQL, params, (rs, i) -> {
            String code = rs.getString("employee_code");
            String name = (rs.getString("first_name") + " " + rs.getString("last_name")).trim();
            String workCenter = rs.getString("work_center");
            return AttendanceSummaryRow.of(
                    code != null ? code : "—",
                    name,
                    workCenter != null ? workCenter : "—",
                    rs.getInt("expected_days"),
                    rs.getInt("attended_days"),
                    rs.getInt("justified"),
                    rs.getInt("unjustified"),
                    rs.getInt("retardos"),
                    rs.getDouble("worked_minutes"),
                    rs.getDouble("overtime_minutes"),
                    "ACTIVE".equals(rs.getString("status")));
        });
    }
}
