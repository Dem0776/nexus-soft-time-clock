package com.condor.nexussoft.timeclock.identity.domain.port.in;

/** Puerto de entrada de autenticación (login, refresh rotatorio, logout). */
public interface AuthenticationUseCase {

    AuthTokens login(LoginCommand command);

    AuthTokens refresh(String refreshToken);

    void logout(String refreshToken);
}
