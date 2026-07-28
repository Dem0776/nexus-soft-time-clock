# Cambios — Perfil de empleado y Vacaciones

Extraer en la RAÍZ del repo `nexus-soft-time-clock/` (respeta rutas). Revisa con `git status`/`git diff`.

## Base de datos
- db/migration/V15__employee_profile_and_vacations.sql  (validada en PostgreSQL 16)

## Backend (módulo nuevo `hr` + wiring)
- backend/modules/hr/**  (perfil de empleado + vacaciones, hexagonal)
- backend/pom.xml         (añade <module>modules/hr</module> + dependencyManagement)
- backend/bootstrap/pom.xml (añade dependencia hr)

## Frontend (Angular)
- web/src/app/features/vacations/**          (bandeja + aprobar/rechazar)
- web/src/app/features/admin/settings/**      (configuración de vacaciones)
- web/src/app/features/admin/users/**         (modal de alta rediseñado + perfil)
- web/src/app/app.routes.ts                   (rutas /vacations y /settings)
- web/src/app/layout/main-layout.component.ts (ítems de menú)

## Pendiente en tu entorno (no pude aquí)
- Backend: `cd backend && mvn -pl modules/hr -am compile` con JDK 21 (Maven Central está bloqueado en el sandbox).
  Verifica en CurrentUser.java los nombres de claim del JWT (tid/uid) contra identity/JwtAccessTokenIssuer,
  y el prefijo de ruta "/api/v1" si usas context-path.
- Frontend: `cd web && npm run build`.
- Aplicar migración: `mvn -pl bootstrap flyway:migrate` o `docker compose up` (Flyway corre V15 al arrancar).
