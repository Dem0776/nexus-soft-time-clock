package com.condor.nexussoft.timeclock.identity.domain.port.in;

import java.util.UUID;

/** Puerto de entrada de autenticación (login, refresh rotatorio, logout, cambio de contraseña). */
public interface AuthenticationUseCase {

    AuthTokens login(LoginCommand command);

    AuthTokens refresh(String refreshToken);

    void logout(String refreshToken);

    /** Cambio de contraseña por el propio usuario; verifica la contraseña actual. */
    void changePassword(UUID userId, String currentPassword, String newPassword);
}
