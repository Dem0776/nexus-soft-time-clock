package com.condor.nexussoft.timeclock.hr.domain;

import java.util.List;
import java.util.UUID;

/**
 * Política de vacaciones por empresa: escalera de días por antigüedad (tramos configurables)
 * más una regla para las antigüedades posteriores al último tramo.
 */
public record VacationPolicy(
        UUID tenantId,
        List<VacationTier> tiers,
        BeyondMode beyondMode,
        int beyondIncrementDays,
        int beyondEveryYears,
        boolean requireApproval,
        boolean countBusinessDaysOnly
) {
    /** Escalera por defecto tipo LFT (MX): 12 el 1er año, +2/año hasta 20 al 5º, luego +2 cada 5 años. */
    public static VacationPolicy defaults(UUID tenantId) {
        return new VacationPolicy(tenantId,
                List.of(new VacationTier(1, 12), new VacationTier(2, 14), new VacationTier(3, 16),
                        new VacationTier(4, 18), new VacationTier(5, 20)),
                BeyondMode.INCREMENT, 2, 5, true, true);
    }

    /** Días de derecho para una antigüedad de `years` años completos. */
    public int entitlement(int years) {
        if (years < 1 || tiers == null || tiers.isEmpty()) {
            return 0;
        }
        VacationTier applicable = null; // tramo más alto con year <= years
        VacationTier last = tiers.get(0); // tramo con el año más grande
        for (VacationTier t : tiers) {
            if (t.year() <= years && (applicable == null || t.year() > applicable.year())) {
                applicable = t;
            }
            if (t.year() > last.year()) {
                last = t;
            }
        }
        if (applicable == null) {
            return 0; // antigüedad menor al primer tramo definido
        }
        if (years <= last.year()) {
            return applicable.days();
        }
        if (beyondMode == BeyondMode.INCREMENT && beyondEveryYears > 0) {
            int extraBlocks = (years - last.year()) / beyondEveryYears;
            return last.days() + extraBlocks * beyondIncrementDays;
        }
        return last.days(); // FLAT
    }
}
