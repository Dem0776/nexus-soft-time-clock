package com.condor.nexussoft.timeclock.identity.domain.port.in;

import java.util.Set;

public final class UserCommands {

    private UserCommands() {
    }

    public record CreateUserCommand(String email, String firstName, String lastName,
                                    String employeeCode, String password, Set<String> roleCodes) {
    }
}
