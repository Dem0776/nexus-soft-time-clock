# 05 — Estrategia de pruebas

## Pirámide de pruebas

```
        /\        E2E (pocas)        — flujos críticos (login → registro → sync)
       /  \       Cypress (web), integration_test (Flutter)
      /----\      Integración (medias)
     /      \     Testcontainers (backend + PostGIS), @SpringBootTest
    /--------\    Unitarias (muchas)
   /__________\   JUnit+Mockito (backend), Jasmine (web), flutter_test (móvil)
```

## Backend
- **Unitarias (dominio/aplicación):** cada servicio de negocio con Mockito de sus puertos. Cubren reglas críticas: autenticación/refresh, código duplicado, validación de asistencia (aceptado, fuera de geocerca, idempotencia, replay, fraude), firma de QR, resolución de incidencias, notificaciones, exportación CSV, sincronización con éxito parcial.
- **Arquitectura:** ArchUnit impone las reglas de dependencia hexagonal (dominio sin frameworks; application sin infraestructura).
- **Integración:** `ApplicationSmokeIT` levanta la app contra **PostGIS real** (Testcontainers) ejecutando migraciones V1–V12 y verificando arranque + endpoints. Base para IT de flujos (login, registro) con `TestRestTemplate`.
- **Cobertura:** JaCoCo por módulo; objetivo del proyecto **≥ 80%** (RNF-13). El gate se aplica en CI.

## Web (Angular)
- **Unitarias/componentes:** Jasmine + Karma (`AppComponent`, servicios).
- **E2E:** Cypress (recomendado) para login y navegación protegida.

## Móvil (Flutter)
- **Unit/Widget:** `flutter_test` (p.ej. `HomeScreen`).
- **Integración:** `integration_test` para el flujo GPS → cola → sincronización.

## Ejecución
```bash
# Backend (unit + IT; IT requiere Docker)
cd backend && mvn verify

# Web
cd web && npm test -- --watch=false --browsers=ChromeHeadless

# Móvil
cd mobile && flutter test
```

## Estado en el entorno de generación
Las pruebas **unitarias** de backend se ejecutaron y **pasan** (compiladas con `release=17` por ausencia de JDK 21). El build de **Angular** pasa. Las **IT** (Testcontainers) y las de **Flutter** están listas para CI, donde hay Docker/SDK.
