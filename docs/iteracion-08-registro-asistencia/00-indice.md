# Iteración 8 — Registro de asistencia (EL NÚCLEO)

**Objetivo:** implementar el corazón del producto — registrar asistencia validada por QR + GPS + geocerca + antifraude, con hora de servidor e idempotencia.

## Módulos

### Anti-Fraud (BC-07) — `backend/modules/antifraud/`
`FraudEvaluationService`: evalúa señales del dispositivo (mock location, root/jailbreak, GPS spoofing, GPS off, dispositivo no confiable) contra la política (REJECT bloquea / FLAG marca). Sin persistencia; devuelve banderas + decisión de bloqueo.

### Attendance (BC-06, núcleo) — `backend/modules/attendance/`
`RegisterAttendanceService` orquesta, en orden:
1. **Idempotencia** (RN-51): reenvío del mismo `operationUuid` → devuelve el resultado previo sin reprocesar.
2. **QR firmado** (RN-25): verifica firma + vigencia + coincidencia tenant/centro (delegado a Geofencing).
3. **Antifraude** (RN-20..RN-28): recoge banderas; bloquea si la política lo indica.
4. **Geocerca + precisión** (RN-13, RN-14): distancia geodésica (haversine) ≤ radio y precisión ≤ umbral.
5. **Anti-replay** (RN-26): consume el `nonce` del QR (`ON CONFLICT DO NOTHING`); duplicado → `REPLAY_DETECTED`.
6. **Hora de servidor** (RN-11) fija el timestamp oficial.
7. Persiste el registro (aceptado o rechazado con motivo) y **publica evento** `AttendanceRegistered`/`AttendanceRejected`.

**API:**
- `POST /api/v1/attendance` (`attendance:register`) → 200 con `{recordId, status, rejectionReason, serverTime, distanceToSiteM, flags}`. Un rechazo de negocio **no** es error HTTP.
- `GET /api/v1/attendance/me?limit=` → historial propio (RF-05).

Motivos de rechazo: `INVALID_QR`, `OUT_OF_GEOFENCE`, `LOW_GPS_ACCURACY`, `GPS_UNAVAILABLE`, `FRAUD_MOCK_LOCATION`, `FRAUD_ROOTED_DEVICE`, `FRAUD_GPS_SPOOF_APP`, `REPLAY_DETECTED`, `UNTRUSTED_DEVICE`.

## Detalles técnicos
- Idempotencia respaldada en `idempotency_keys.result_json` (migración **V11**); resultado serializado con Jackson.
- Ubicación persistida como `geography(Point,4326)` (hibernate-spatial/JTS); `validations_json` como `jsonb` (`@JdbcTypeCode(JSON)`).
- Puertos de salida hacia otros BC (QR, geocerca, fraude) vía adaptadores de integración → aplicación testeable con mocks.

## Estado de verificación

| Qué | Cómo | Resultado |
|---|---|---|
| **Backend compila** | `mvn compile` (reactor completo, release 17) | ✅ exit 0 |
| **Lógica del núcleo** | `mvn test` attendance (JUnit + Mockito) | ✅ **5/5 pasan** |
| Runtime E2E | stack + JDK 21 + Docker | ⚠️ no ejecutado |

> El compile-check atrapó un bug real (uso de `Jwt` sin la dependencia oauth2 en attendance) — se resolvió leyendo tenant/usuario del contexto de seguridad sin acoplar a esa librería.

## Criterios de aceptación
- [x] Registro por QR + GPS + geocerca + validaciones + antifraude + hora de servidor.
- [x] Idempotencia (offline-ready) y anti-replay por nonce.
- [x] Persistencia del resultado (aceptado/rechazado + motivo + banderas) y evento de dominio.
- [x] Historial propio del colaborador.
- [x] Pruebas de la lógica del núcleo (5 escenarios) — pasan.
- [ ] Verificación E2E en runtime (pendiente JDK 21 + Docker).

> Siguiente: **Iteración 9 — Sincronización offline** (ingesta por lotes idempotente y resolución de conflictos) + el registro de asistencia en la app Flutter (QR + GPS + cola local Drift).
