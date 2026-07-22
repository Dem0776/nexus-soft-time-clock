# Iteración 5 — Autenticación y autorización

**Objetivo:** implementar el primer bounded context real (**Identity & Access, BC-01**) extremo a extremo: login con JWT + refresh rotatorio, RBAC y multi-tenant en el backend, e integración en el portal Angular y la app Flutter.

## Diseño

- **Access token:** JWT RS256 de vida corta (~15 min), claims `sub`, `tenant_id`, `roles`, `permissions`, `platform_admin` (ADR-007).
- **Refresh token:** opaco (256 bits), almacenado **hasheado** (SHA-256), **rotatorio** por familia; reutilización → revoca la familia (RN-41).
- **RBAC:** `@EnableMethodSecurity` + autoridades = permisos (`hasAuthority('recurso:accion')`) y roles (`ROLE_*`).
- **Multi-tenant:** `TenantContextFilter` fija el tenant desde el claim del JWT ya autenticado (RN-31), nunca desde el cliente.
- **Bloqueo:** N intentos fallidos → cuenta `LOCKED` temporal (RN-40).
- **Hexagonal:** dominio puro (`User`, `RefreshToken`, VOs) → application (`AuthenticationService`) → infraestructura (JPA, JWT Nimbus, BCrypt, web).

## Endpoints (API v1)

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/api/v1/auth/login` | pública | Login → par de tokens |
| POST | `/api/v1/auth/refresh` | pública | Rotación de tokens |
| POST | `/api/v1/auth/logout` | pública | Revoca la familia del refresh |
| GET | `/api/v1/auth/me` | Bearer | Identidad del usuario autenticado |

## Estructura entregada

```
backend/modules/identity/          # BC-01 hexagonal
  domain/        model · event · exception · port(in/out)
  application/   AuthenticationService, AuthPolicy
  infrastructure/ persistence(JPA) · security(JWT/BCrypt/converter/tenant filter) · web(controller/DTOs)
web/src/app/core/  auth(store Signals, service) · interceptors(refresh 401) · guards(authGuard)
web/src/app/features/ auth/login · dashboard (protegido)
mobile/lib/src/    core(dio, secure storage) · features/auth(repository, controller Riverpod, login)
```

## Datos demo (perfil `dev`)

`DevDataSeeder` crea la empresa **DEMO** y usuarios (contraseña por defecto `Admin123!`, override con `NEXUS_SEED_PASSWORD`):
- `superadmin@nexus.io` (SUPER_ADMIN, plataforma)
- `admin@demo.com` (COMPANY_ADMIN)
- `empleado@demo.com` (EMPLOYEE)

## Estado de verificación

| Qué | Cómo | Resultado |
|---|---|---|
| **Backend compila** | `mvn compile` (reactor completo, release 17) | ✅ **Compila** |
| **Lógica de auth** | `mvn test` módulo identity (JUnit 5 + Mockito) | ✅ **3/3 tests pasan** |
| **Portal Angular** | `npm run build` | ✅ **Compila** (login/dashboard lazy, interceptor, guard) |
| Backend runtime | `mvn verify` con **JDK 21** + Testcontainers | ⚠️ No ejecutado (entorno JDK 17, Docker inactivo) |
| Flutter | `flutter analyze/test` | ⚠️ No ejecutado (SDK ausente) |
| E2E login real | levantar stack + `POST /auth/login` | ⚠️ No ejecutado (requiere Docker + JDK 21) |

> El backend se compiló con `-Dmaven.compiler.release=17` por falta de JDK 21 en el entorno; el `pom.xml` mantiene **Java 21** como target oficial (el código no usa APIs exclusivas de 21, por lo que compila también en 17+).

## Criterios de aceptación

- [x] Login emite JWT + refresh; refresh rota y detecta reuso (revoca familia).
- [x] RBAC por método con permisos como autoridades.
- [x] Tenant derivado del token (aislamiento multi-tenant).
- [x] Bloqueo por intentos fallidos.
- [x] Manejo uniforme de errores (401/423 con ProblemDetail).
- [x] Cliente web (interceptor + guard + login) y móvil (repo + controller + login) integrados.
- [x] Pruebas unitarias de la lógica de autenticación (pasan).
- [ ] Verificación E2E en runtime (pendiente de JDK 21 + Docker en tu entorno).

## Cómo probar E2E (en tu máquina)

```bash
docker compose -f infra/docker-compose.yml up -d postgres redis
cd backend && mvn spring-boot:run -pl bootstrap    # SPRING_PROFILES_ACTIVE=dev, requiere JDK 21
# En otra terminal:
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@demo.com","password":"Admin123!","companyCode":"DEMO"}'
```
