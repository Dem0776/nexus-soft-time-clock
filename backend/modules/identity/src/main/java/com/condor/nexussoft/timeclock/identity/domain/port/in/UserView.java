package com.condor.nexussoft.timeclock.identity.domain.port.in;

import java.util.List;
import java.util.UUID;

/** Proyección de lectura de un usuario para la administración (sin exponer el hash). */
public record UserView(UUID id, String email, String firstName, String lastName,
                       String employeeCode, String status, List<String> roles) {
}
