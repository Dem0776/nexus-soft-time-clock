package com.condor.nexussoft.timeclock.identity.infrastructure.web.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AssignRolesRequest(@NotEmpty Set<String> roleCodes) {
}
