package com.condor.nexussoft.timeclock.identity.domain.port.out;

/** Genera refresh tokens opacos aleatorios y los hashea para almacenamiento seguro. */
public interface OpaqueTokenServicePort {

    /** Nuevo token aleatorio criptográficamente seguro (valor en claro para el cliente). */
    String generate();

    /** Hash determinístico del token para persistir/buscar (nunca se guarda el claro). */
    String hash(String rawToken);
}
