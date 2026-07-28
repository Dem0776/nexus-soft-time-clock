package com.condor.nexussoft.timeclock.hr.infrastructure.web;

import com.condor.nexussoft.timeclock.hr.domain.EmployeeProfile;
import com.condor.nexussoft.timeclock.hr.domain.Gender;
import com.condor.nexussoft.timeclock.hr.domain.port.in.EmployeeProfileUseCase;
import com.condor.nexussoft.timeclock.hr.infrastructure.web.dto.EmployeeProfileDtos.EmployeeProfileRequest;
import com.condor.nexussoft.timeclock.hr.infrastructure.web.dto.EmployeeProfileDtos.EmployeeProfileResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Perfil de empleado como sub-recurso de un usuario ({@code /users/{id}/profile}).
 * Se mantiene fuera del módulo identity para no acoplarse a él. Requiere {@code user:manage}.
 *
 * NOTA: si tus controllers usan un context-path (server.servlet.context-path=/api/v1) con
 * mappings cortos, elimina el prefijo "/api/v1" de @RequestMapping.
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/profile")
@PreAuthorize("hasAuthority('user:manage')")
public class EmployeeProfileController {

    private final EmployeeProfileUseCase useCase;

    public EmployeeProfileController(EmployeeProfileUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public EmployeeProfileResponse get(@PathVariable UUID userId, Authentication auth) {
        return toResponse(useCase.get(CurrentUser.tenantId(auth), userId));
    }

    @PutMapping
    public EmployeeProfileResponse upsert(@PathVariable UUID userId,
                                          @Valid @RequestBody EmployeeProfileRequest body,
                                          Authentication auth) {
        EmployeeProfile saved = useCase.upsert(new EmployeeProfileUseCase.UpsertProfileCommand(
                CurrentUser.tenantId(auth), userId, body.birthDate(), body.hireDate(),
                parseGender(body.gender()), body.phone(), body.address(),
                body.emergencyContactName(), body.emergencyContactPhone()));
        return toResponse(saved);
    }

    private static Gender parseGender(String value) {
        if (value == null || value.isBlank()) return null;
        return Gender.valueOf(value.trim().toUpperCase());
    }

    private static EmployeeProfileResponse toResponse(EmployeeProfile p) {
        return new EmployeeProfileResponse(
                p.userId() == null ? null : p.userId().toString(),
                p.birthDate(), p.hireDate(),
                p.gender() == null ? null : p.gender().name(),
                p.phone(), p.address(), p.emergencyContactName(), p.emergencyContactPhone());
    }
}
