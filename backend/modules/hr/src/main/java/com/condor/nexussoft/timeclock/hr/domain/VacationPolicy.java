package com.condor.nexussoft.timeclock.hr.domain;

import java.util.UUID;

/** Política de vacaciones por empresa (días base/año, aprobación, conteo de días hábiles). */
public record VacationPolicy(
        UUID tenantId,
        int daysPerYear,
        boolean requireApproval,
        boolean countBusinessDaysOnly
) {
    public static VacationPolicy defaults(UUID tenantId) {
        return new VacationPolicy(tenantId, 12, true, true);
    }
}
