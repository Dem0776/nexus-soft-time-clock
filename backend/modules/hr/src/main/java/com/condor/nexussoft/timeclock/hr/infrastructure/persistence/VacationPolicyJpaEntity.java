package com.condor.nexussoft.timeclock.hr.infrastructure.persistence;

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

    @Column(nullable = false)
    private int daysPerYear;

    @Column(nullable = false)
    private boolean requireApproval;

    @Column(nullable = false)
    private boolean countBusinessDaysOnly;
}
