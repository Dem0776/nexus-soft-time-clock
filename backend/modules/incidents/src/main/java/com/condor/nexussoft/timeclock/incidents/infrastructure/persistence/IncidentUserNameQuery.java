package com.condor.nexussoft.timeclock.incidents.infrastructure.persistence;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Resuelve el nombre visible ({@code first_name + " " + last_name}) de un conjunto de colaboradores
 * leyendo la tabla {@code users} directamente. Sigue el precedente del módulo reporting
 * ({@code AttendanceSummaryService}): join en el read-side sin dependencia Java al módulo identity,
 * ya que ambos módulos comparten la base y el {@code tenant_id}.
 */
@Component
public class IncidentUserNameQuery {

    private static final String SQL = """
            SELECT id, first_name, last_name
            FROM users
            WHERE tenant_id = :tenant AND id IN (:ids)
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public IncidentUserNameQuery(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Mapa {@code userId -> "Nombre Apellido"} para los ids indicados; vacío si no hay ids. */
    public Map<UUID, String> namesByUserId(UUID tenantId, Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenant", tenantId)
                .addValue("ids", userIds);
        Map<UUID, String> names = new HashMap<>();
        jdbc.query(SQL, params, rs -> {
            UUID id = rs.getObject("id", UUID.class);
            String name = (rs.getString("first_name") + " " + rs.getString("last_name")).trim();
            names.put(id, name);
        });
        return names;
    }
}
