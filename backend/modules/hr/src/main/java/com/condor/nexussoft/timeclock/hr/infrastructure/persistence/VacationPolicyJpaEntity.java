package com.condor.nexussoft.timeclock.hr.infrastructure.persistence;

import com.condor.nexussoft.timeclock.hr.domain.BeyondMode;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vacation_policies")
@Getter
@Setter
@NoArgsConstructor
public class VacationPolicyJpaEntity {

    @Id
    @Column(name = "tenant_id")
    private UUID tenantId;

    /** Escalera de tramos serializada como JSON (columna text `tiers`). */
    @Column(name = "tiers", columnDefinition = "text")
    private String tiersJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "beyond_mode", nullable = false)
    private BeyondMode beyondMode;

    @Column(nullable = false)
    private int beyondIncrementDays;

    @Column(nullable = false)
    private int beyondEveryYears;

    @Column(nullable = false)
    private boolean requireApproval;

    @Column(nullable = false)
    private boolean countBusinessDaysOnly;
}
