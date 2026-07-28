package com.condor.nexussoft.timeclock.hr.domain.port.in;

import com.condor.nexussoft.timeclock.hr.domain.EmployeeProfile;
import com.condor.nexussoft.timeclock.hr.domain.Gender;
import java.time.LocalDate;
import java.util.UUID;

public interface EmployeeProfileUseCase {
    /** Devuelve el perfil o uno vacío (solo con ids) si aún no se ha capturado. */
    EmployeeProfile get(UUID tenantId, UUID userId);

    EmployeeProfile upsert(UpsertProfileCommand command);

    record UpsertProfileCommand(
            UUID tenantId,
            UUID userId,
            LocalDate birthDate,
            LocalDate hireDate,
            Gender gender,
            String phone,
            String address,
            String emergencyContactName,
            String emergencyContactPhone
    ) {}
}
