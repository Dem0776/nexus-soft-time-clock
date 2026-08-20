package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import java.util.UUID;

/**
 * Política de registro por defecto de la empresa ({@code company_settings}). Es la base sobre la
 * que cada centro puede aplicar overrides: una columna a {@code NULL} en {@code work_sites}
 * significa «heredar de aquí», tal como documenta la migración V3.
 */
public interface CompanyPolicyPort {

    CompanyPolicy find(UUID tenantId);

    /**
     * @param defaultGpsAccuracyMaxM umbral de precisión GPS por defecto del tenant.
     * @param requirePhoto           la empresa exige evidencia fotográfica (HU-13 CA1).
     * @param requireBiometric       la empresa exige verificación biométrica (HU-14 CA1).
     */
    record CompanyPolicy(Integer defaultGpsAccuracyMaxM, boolean requirePhoto, boolean requireBiometric) {

        /** Tenant sin fila de configuración: se asume la política más permisiva. */
        public static CompanyPolicy defaults() {
            return new CompanyPolicy(null, false, false);
        }
    }
}
