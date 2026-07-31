package com.condor.nexussoft.timeclock.identity.infrastructure.persistence;

import com.condor.nexussoft.timeclock.identity.domain.model.DeviceAction;
import com.condor.nexussoft.timeclock.identity.domain.port.out.DeviceBindingPolicyPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Lee la política de device binding del tenant desde {@code company_settings} (columnas añadidas en
 * V20) vía consulta directa. Sigue el mismo precedente que {@code CompanyDirectoryAdapter}
 * (lectura provisional hasta que Tenancy exponga su servicio).
 */
@Repository
public class DeviceBindingPolicyAdapter implements DeviceBindingPolicyPort {

    private final JdbcTemplate jdbc;

    public DeviceBindingPolicyAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public DeviceBindingPolicy find(UUID tenantId) {
        if (tenantId == null) {
            return DeviceBindingPolicy.defaults();
        }
        return jdbc.query(
                "SELECT device_binding_enabled, device_binding_action FROM company_settings "
                        + "WHERE company_id = ? LIMIT 1",
                rs -> {
                    if (!rs.next()) {
                        return DeviceBindingPolicy.defaults();
                    }
                    boolean enabled = rs.getBoolean("device_binding_enabled");
                    DeviceAction action = "FLAG".equalsIgnoreCase(rs.getString("device_binding_action"))
                            ? DeviceAction.FLAG : DeviceAction.REJECT;
                    return new DeviceBindingPolicy(enabled, action);
                },
                tenantId);
    }
}
