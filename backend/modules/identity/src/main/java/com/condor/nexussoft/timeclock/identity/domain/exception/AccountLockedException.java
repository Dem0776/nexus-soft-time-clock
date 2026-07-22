package com.condor.nexussoft.timeclock.identity.domain.exception;

import com.condor.nexussoft.timeclock.shared.domain.DomainException;

/** Cuenta bloqueada temporalmente por superar el máximo de intentos (RN-40). */
public class AccountLockedException extends DomainException {
    public AccountLockedException() {
        super("ACCOUNT_LOCKED", "Cuenta bloqueada temporalmente por intentos fallidos");
    }
}
