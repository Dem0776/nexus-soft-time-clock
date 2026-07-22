package com.condor.nexussoft.timeclock.identity.domain.port.out;

import com.condor.nexussoft.timeclock.identity.domain.model.User;

/** Emite el access token JWT con las claims del usuario (sub, tenant_id, roles, permisos). */
public interface AccessTokenIssuerPort {

    IssuedToken issue(User user);
}
