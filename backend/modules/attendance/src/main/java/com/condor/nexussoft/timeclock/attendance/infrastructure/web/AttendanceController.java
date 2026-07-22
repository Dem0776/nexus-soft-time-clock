package com.condor.nexussoft.timeclock.attendance.infrastructure.web;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.RegisterAttendanceUseCase;
import com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto.AttendanceResponse;
import com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto.AttendanceSummaryResponse;
import com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto.RegisterAttendanceRequest;
import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Registro de asistencia (núcleo, CU-02) e historial propio (RF-05). El registro devuelve
 * 200 con el resultado (ACCEPTED/REJECTED + motivo); un rechazo de negocio no es un error HTTP.
 * El tenant proviene del contexto (JWT) y el usuario del subject del token autenticado.
 */
@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final RegisterAttendanceUseCase attendance;

    public AttendanceController(RegisterAttendanceUseCase attendance) {
        this.attendance = attendance;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('attendance:register')")
    public AttendanceResponse register(@Valid @RequestBody RegisterAttendanceRequest request) {
        return AttendanceResponse.from(
                attendance.register(TenantContext.require(), currentUserId(), request.toCommand()));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<AttendanceSummaryResponse> myHistory(@RequestParam(defaultValue = "50") int limit) {
        return attendance.history(TenantContext.require(), currentUserId(), limit).stream()
                .map(AttendanceSummaryResponse::from)
                .toList();
    }

    /** El subject del JWT (nombre de la autenticación) es el id de usuario. */
    private UUID currentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
