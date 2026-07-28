package com.condor.nexussoft.timeclock.hr.infrastructure.web.dto;

import java.time.LocalDate;

/** DTOs del perfil de empleado (datos personales opcionales). */
public final class EmployeeProfileDtos {

    private EmployeeProfileDtos() {}

    public record EmployeeProfileResponse(
            String userId,
            LocalDate birthDate,
            LocalDate hireDate,
            String gender,
            String phone,
            String address,
            String emergencyContactName,
            String emergencyContactPhone
    ) {}

    public record EmployeeProfileRequest(
            LocalDate birthDate,
            LocalDate hireDate,
            String gender,
            String phone,
            String address,
            String emergencyContactName,
            String emergencyContactPhone
    ) {}
}
