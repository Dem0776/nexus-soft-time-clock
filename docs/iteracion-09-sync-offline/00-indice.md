# Iteración 9 — Sincronización offline y resolución de conflictos

**Objetivo:** ingesta por lotes idempotente en el backend y la máquina **offline-first** en la app Flutter (cola local + sincronización con reintentos).

## Backend — Sync (BC-08) `backend/modules/sync/`
`SyncAttendanceService` procesa un lote delegando cada operación en el **núcleo idempotente** (Iteración 8). Cada item se procesa de forma **aislada** (una transacción propia): un fallo no aborta el lote, y el cliente reintenta solo los items con `error` (RN-52). La validación del servidor es **autoritativa** (RN-53): cada operación vuelve como `ACCEPTED`/`REJECTED`+motivo o `ERROR`.

- `POST /api/v1/sync/attendance` (`attendance:register`) — body `{operations:[...]}` (máx. 200), devuelve `{results:[{operationUuid,status,rejectionReason,serverTime,error}]}` (RN-54).

## App Flutter — Offline-first `mobile/`
- **Cola local (Drift):** `PendingAttendanceOps` — cada operación se persiste **antes** de enviarse (nunca se pierde). `enqueue` idempotente (`insertOrIgnore` por `operationUuid`).
- **Captura GPS:** `LocationService` (geolocator) + señales antifraude locales (`isMocked`, GPS off).
- **Controlador (Riverpod):** `AttendanceController.register()` → captura GPS → construye operación con UUID → encola → intenta sincronizar; expone contador de pendientes.
- **Sincronizador:** `AttendanceSyncService.syncPending()` — envía el lote a `/sync/attendance`, aplica el resultado autoritativo por operación (`SYNCED`/`REJECTED`/`ERROR`), y ante fallo de red incrementa intentos dejando `PENDING` para reintento (RN-52, RN-54).
- **Pantalla:** `AttendanceScreen` (Entrada/Salida + token QR + contador de pendientes + sync manual). Ruta `/attendance`.

## Estado de verificación

| Qué | Cómo | Resultado |
|---|---|---|
| **Backend compila** | `mvn compile` (11 módulos, release 17) | ✅ exit 0 |
| **Ingesta por lotes** | `mvn test` sync (éxito parcial: item OK + item con error aislado) | ✅ **pasa** |
| App Flutter | `flutter analyze/test` + build_runner (Drift) | ⚠️ no ejecutado (SDK ausente) |
| Runtime E2E | stack + JDK 21 + Docker | ⚠️ no ejecutado |

> La cola Drift requiere codegen (`dart run build_runner build`) para generar `app_database.g.dart`; la CI de Flutter ya lo ejecuta. No verificable en este entorno.

## Criterios de aceptación
- [x] Ingesta por lotes idempotente con resultado por operación (servidor autoritativo).
- [x] Aislamiento de errores por item (reintento selectivo).
- [x] Cola local persistente (offline-first) con enqueue idempotente.
- [x] Sincronización automática al registrar + sincronización manual, con reintentos ante fallo de red.
- [x] Estado local por operación (PENDING/SYNCED/REJECTED/ERROR) y contador de pendientes.
- [x] Test de la ingesta por lotes (pasa).
- [ ] Verificación de la app Flutter y E2E en runtime (pendiente SDK/JDK 21/Docker).

> Siguiente: **Iteración 10 — Incidencias, auditoría y reglas de negocio** (BC-09 Incidents + BC-10 Audit consumiendo los eventos de dominio ya publicados).
