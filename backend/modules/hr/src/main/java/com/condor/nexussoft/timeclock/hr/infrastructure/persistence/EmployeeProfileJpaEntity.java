package com.condor.nexussoft.timeclock.hr.infrastructure.persistence;

import com.condor.nexussoft.timeclock.hr.domain.Gender;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee_profiles")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeProfileJpaEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    private LocalDate birthDate;
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String phone;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
}
