package com.condor.nexussoft.timeclock.identity.domain.port.out;

/** Verificación y generación de hashes de contraseña (BCrypt/Argon2 en el adaptador). */
public interface PasswordHasherPort {

    boolean matches(String rawPassword, String passwordHash);

    String hash(String rawPassword);
}
