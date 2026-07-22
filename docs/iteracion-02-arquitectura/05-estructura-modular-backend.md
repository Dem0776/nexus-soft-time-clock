# 05 — Estructura modular del backend

Layout físico del **modular monolith** con arquitectura hexagonal por módulo. Base de paquetes: `com.condor.nexussoft.timeclock`.

## 5.1 Estructura de módulos (Maven multi-módulo)

```
backend/
├── pom.xml                      # parent (BOM, versiones, plugins comunes)
├── bootstrap/                   # app Spring Boot (main, wiring, config, Flyway)
│   └── com.condor.nexussoft.timeclock.bootstrap
├── shared-kernel/               # tipos comunes, contratos de eventos, base hexagonal
│   └── ...timeclock.shared
├── platform/                    # infra transversal: seguridad, multitenancy, outbox, persistencia base
│   └── ...timeclock.platform
└── modules/
    ├── identity/                # BC-01 Identity & Access
    ├── tenancy/                 # BC-02
    ├── organization/            # BC-03
    ├── scheduling/              # BC-04
    ├── geofencing/              # BC-05
    ├── attendance/              # BC-06 (core)
    ├── antifraud/               # BC-07
    ├── sync/                    # BC-08
    ├── incidents/               # BC-09
    ├── audit/                   # BC-10
    ├── reporting/               # BC-11
    ├── notifications/           # BC-12
    └── monitoring/              # BC-13
```

> El **bootstrap** es el único artefacto ejecutable (un solo deployable). Cada módulo expone una **API pública mínima** (puertos) y oculta su implementación. Esto permite, más adelante, mover un módulo a su propio bootstrap (microservicio) cambiando solo el adaptador de mensajería.

## 5.2 Anatomía interna de un módulo (hexagonal)

Ejemplo `modules/attendance` — paquete `com.condor.nexussoft.timeclock.attendance`:

```
attendance/
├── domain/                      # NÚCLEO — sin dependencias de framework
│   ├── model/                   # Agregados, Entidades, Value Objects
│   │   ├── AttendanceRecord.java      (aggregate root)
│   │   ├── WorkDay.java
│   │   ├── GpsFix.java                (VO: lat, lng, accuracy)
│   │   ├── AttendanceEventType.java   (enum)
│   │   └── ValidationResult.java      (VO)
│   ├── event/                   # Eventos de dominio (AttendanceRegistered, ...)
│   ├── service/                 # Domain services (reglas puras RN-10..RN-17)
│   └── port/
│       ├── in/                  # Puertos de entrada (use case interfaces)
│       │   └── RegisterAttendanceUseCase.java
│       └── out/                 # Puertos de salida (interfaces)
│           ├── AttendanceRepository.java
│           ├── GeofenceValidationPort.java   (hacia BC-05)
│           ├── ScheduleValidationPort.java   (hacia BC-04)
│           ├── FraudEvaluationPort.java       (hacia BC-07)
│           ├── EvidenceStoragePort.java       (hacia MinIO)
│           └── ServerClockPort.java
├── application/                 # Casos de uso (orquestan dominio + puertos)
│   ├── RegisterAttendanceService.java   (implements RegisterAttendanceUseCase)
│   ├── command/  query/                 # CQRS: comandos y consultas
│   └── mapper/                           # MapStruct application<->domain
└── infrastructure/              # Adaptadores concretos
    ├── web/                     # REST controllers, DTOs, error handling
    ├── persistence/             # JPA entities + adapters (implements *Repository)
    ├── messaging/               # publishers/consumers de eventos (outbox)
    └── config/                  # Spring @Configuration del módulo
```

## 5.3 Reglas de dependencia (impuestas por diseño y verificables con ArchUnit)

| Regla | Descripción |
|---|---|
| **DR-1** | `domain` **no** importa `application` ni `infrastructure` ni Spring/JPA. |
| **DR-2** | `application` **no** importa `infrastructure`; solo `domain` y sus puertos. |
| **DR-3** | `infrastructure` implementa puertos de `application`/`domain`; es la única capa con anotaciones de framework. |
| **DR-4** | Un módulo **solo** accede a otro a través de sus **puertos públicos** (paquete `...port.in`) o mediante **eventos**; nunca a su `infrastructure`/`persistence`. |
| **DR-5** | `shared-kernel` no depende de ningún módulo de negocio. |

> Estas reglas se **verificarán automáticamente** con **ArchUnit** en la suite de pruebas (Iteración 4/13), evitando erosión de la arquitectura.

## 5.4 Configuración y ejecución

- **Spring Boot** con perfiles (`dev`, `test`, `prod`); configuración externalizada (env vars / ConfigMaps).
- **Flyway** ejecuta migraciones al arrancar (`bootstrap`), con scripts por módulo bajo `db/migration/{modulo}`.
- **Spring Modulith** (opcional, a evaluar) para verificar y documentar los límites de módulos y el flujo de eventos.
