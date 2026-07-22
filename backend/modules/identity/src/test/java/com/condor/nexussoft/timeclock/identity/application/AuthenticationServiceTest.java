package com.condor.nexussoft.timeclock.identity.application;

import com.condor.nexussoft.timeclock.identity.domain.exception.InvalidCredentialsException;
import com.condor.nexussoft.timeclock.identity.domain.exception.InvalidRefreshTokenException;
import com.condor.nexussoft.timeclock.identity.domain.model.*;
import com.condor.nexussoft.timeclock.identity.domain.port.in.AuthTokens;
import com.condor.nexussoft.timeclock.identity.domain.port.in.LoginCommand;
import com.condor.nexussoft.timeclock.identity.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock UserRepositoryPort users;
    @Mock RefreshTokenStorePort refreshTokens;
    @Mock CompanyDirectoryPort companies;
    @Mock PasswordHasherPort passwordHasher;
    @Mock AccessTokenIssuerPort accessTokenIssuer;
    @Mock OpaqueTokenServicePort opaqueTokens;
    @Mock DomainEventPublisherPort eventPublisher;

    AuthenticationService service;
    final UUID tenantId = UUID.randomUUID();
    final Clock clock = Clock.fixed(Instant.parse("2026-07-21T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new AuthenticationService(users, refreshTokens, companies, passwordHasher,
                accessTokenIssuer, opaqueTokens, eventPublisher, AuthPolicy.defaults(), clock);
    }

    private User activeUser() {
        return new User(UUID.randomUUID(), tenantId, false, Email.of("admin@demo.com"),
                "hash", "Ana", "Admin", UserStatus.ACTIVE, 0, null, Set.of());
    }

    @Test
    void login_conCredencialesValidas_emiteTokens() {
        User user = activeUser();
        when(companies.resolveActiveTenant("DEMO")).thenReturn(Optional.of(tenantId));
        when(users.findByTenantAndEmail(tenantId, Email.of("admin@demo.com"))).thenReturn(Optional.of(user));
        when(passwordHasher.matches("secret", "hash")).thenReturn(true);
        when(accessTokenIssuer.issue(user)).thenReturn(new IssuedToken("access-jwt", 900));
        when(opaqueTokens.generate()).thenReturn("raw-refresh");
        when(opaqueTokens.hash("raw-refresh")).thenReturn("hashed-refresh");

        AuthTokens tokens = service.login(new LoginCommand("DEMO", "admin@demo.com", "secret", "1.1.1.1", "junit"));

        assertThat(tokens.accessToken()).isEqualTo("access-jwt");
        assertThat(tokens.refreshToken()).isEqualTo("raw-refresh");
        assertThat(tokens.tokenType()).isEqualTo("Bearer");
        verify(refreshTokens).save(any());
    }

    @Test
    void login_conPasswordIncorrecta_lanzaYRegistraFallo() {
        User user = activeUser();
        when(companies.resolveActiveTenant("DEMO")).thenReturn(Optional.of(tenantId));
        when(users.findByTenantAndEmail(tenantId, Email.of("admin@demo.com"))).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() ->
                service.login(new LoginCommand("DEMO", "admin@demo.com", "wrong", "1.1.1.1", "junit")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(users).save(user);                 // persiste el contador de fallos
        assertThat(user.failedLoginCount()).isEqualTo(1);
    }

    @Test
    void refresh_conTokenReutilizado_revocaLaFamilia() {
        UUID familyId = UUID.randomUUID();
        RefreshToken reused = new RefreshToken(UUID.randomUUID(), familyId, UUID.randomUUID(), tenantId,
                "hashed", Instant.parse("2026-08-21T10:00:00Z"),
                Instant.parse("2026-07-20T10:00:00Z"), UUID.randomUUID());  // ya revocado/rotado
        when(opaqueTokens.hash("raw")).thenReturn("hashed");
        when(refreshTokens.findByHash("hashed")).thenReturn(Optional.of(reused));

        assertThatThrownBy(() -> service.refresh("raw"))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokens).revokeFamily(familyId);
    }
}
