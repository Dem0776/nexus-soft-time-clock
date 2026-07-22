package com.condor.nexussoft.timeclock.geofencing.domain.port.out;

import com.condor.nexussoft.timeclock.geofencing.domain.QrPayload;

import java.util.Optional;

/** Firma y verifica el contenido del QR de centro (HMAC en el adaptador). */
public interface QrTokenSignerPort {

    String sign(QrPayload payload);

    /** Devuelve el payload si la firma es válida; vacío si la firma es inválida o el formato es incorrecto. */
    Optional<QrPayload> verify(String token);
}
