package com.condor.nexussoft.timeclock.identity.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.identity.domain.port.in.UserView;

import java.util.List;
import java.util.UUID;

public record UserResponse(UUID id, String email, String firstName, String lastName,
                           String employeeCode, String status, List<String> roles) {

    public static UserResponse from(UserView v) {
        return new UserResponse(v.id(), v.email(), v.firstName(), v.lastName(),
                v.employeeCode(), v.status(), v.roles());
    }
}
