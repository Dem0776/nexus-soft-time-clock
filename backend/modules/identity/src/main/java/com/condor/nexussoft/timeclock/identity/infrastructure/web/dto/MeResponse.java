package com.condor.nexussoft.timeclock.identity.infrastructure.web.dto;

import java.util.List;

/** Identidad del usuario autenticado, derivada de las claims del access token. */
public record MeResponse(String userId, String tenantId, boolean platformAdmin,
                         List<String> roles, List<String> permissions) {
}
