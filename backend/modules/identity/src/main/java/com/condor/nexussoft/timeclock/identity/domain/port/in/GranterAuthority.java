package com.condor.nexussoft.timeclock.identity.domain.port.in;

import java.util.Set;

/**
 * Identidad del operador que ejecuta una acción administrativa de usuarios, derivada del
 * token de seguridad. Permite a la capa de aplicación aplicar la política de delegación de
 * roles ({@link com.condor.nexussoft.timeclock.identity.domain.model.RoleGrantPolicy}).
 *
 * @param platformAdmin true si es SUPER_ADMIN de plataforma (sin restricción de delegación)
 * @param roleCodes     códigos de rol que posee el operador
 */
public record GranterAuthority(boolean platformAdmin, Set<String> roleCodes) {
}
