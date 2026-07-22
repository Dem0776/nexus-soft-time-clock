package com.condor.nexussoft.timeclock.identity.infrastructure.web;

import com.condor.nexussoft.timeclock.identity.domain.port.in.AuthenticationUseCase;
import com.condor.nexussoft.timeclock.identity.domain.port.in.LoginCommand;
import com.condor.nexussoft.timeclock.identity.infrastructure.web.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de autenticación (API v1). Login y refresh son públicos; {@code /me}
 * requiere un access token válido.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationUseCase authentication;

    public AuthController(AuthenticationUseCase authentication) {
        this.authentication = authentication;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        LoginCommand command = new LoginCommand(
                request.companyCode(), request.email(), request.password(),
                clientIp(http), http.getHeader("User-Agent"));
        return TokenResponse.from(authentication.login(command));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return TokenResponse.from(authentication.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest request) {
        authentication.logout(request.refreshToken());
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return new MeResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("tenant_id"),
                Boolean.TRUE.equals(jwt.getClaimAsBoolean("platform_admin")),
                orEmpty(jwt.getClaimAsStringList("roles")),
                orEmpty(jwt.getClaimAsStringList("permissions")));
    }

    private static List<String> orEmpty(List<String> values) {
        return values == null ? List.of() : values;
    }

    private static String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return http.getRemoteAddr();
    }
}
