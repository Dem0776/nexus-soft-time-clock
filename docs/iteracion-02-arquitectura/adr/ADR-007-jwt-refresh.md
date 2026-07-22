# ADR-007 — JWT access + refresh token rotatorio

**Estado:** Aceptado · **Fecha:** 2026-07-21

## Contexto
Se requiere autenticación stateless escalable (RNF-02), con RBAC y revocación razonable (RF-01, RF-22, RNF-06).

## Decisión
**Access token JWT** de vida corta (~15 min) firmado (RS256/EdDSA), portando `sub`, `tenant_id`, roles/permisos. **Refresh token** opaco de vida larga (~30 días) **rotatorio**: cada uso emite uno nuevo e invalida el anterior; la **reutilización revoca toda la familia** (detección de robo, RN-41). Refresh guardado en **Flutter Secure Storage** (móvil) y cookie httpOnly/secure o storage seguro (web). Lista de revocación y rate limiting en **Redis**. OAuth2/OIDC **preparado** para SSO futuro (no habilitado).

## Consecuencias
- ➕ Escalado stateless; validación sin ida a BD en cada request.
- ➕ Ventana de exposición corta; rotación mitiga robo de refresh.
- ➖ Revocación de access token no es inmediata (mitigado por vida corta + blacklist para casos críticos).
- ➖ Gestión de claves (rotación de llaves de firma) y almacenamiento seguro de refresh.
