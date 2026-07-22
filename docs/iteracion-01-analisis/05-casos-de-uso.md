# 05 — Casos de uso

Se detallan los casos de uso críticos con flujo principal y excepciones. El resto de operaciones CRUD administrativas siguen el patrón estándar (crear/leer/actualizar/eliminar con validación + auditoría) y no se detallan individualmente.

---

## CU-01 — Autenticar usuario (RF-01)

- **Actor:** Usuario · **Precondición:** cuenta activa en un tenant.
- **Flujo principal:**
  1. El usuario envía credenciales + identificador de empresa.
  2. El sistema valida credenciales y estado de la cuenta.
  3. El sistema emite `accessToken` (corto) + `refreshToken` (rotatorio).
  4. El sistema registra el inicio de sesión en auditoría.
- **Excepciones:**
  - E1: Credenciales inválidas → 401 genérico; incrementa contador de fallos (RN-40).
  - E2: Cuenta bloqueada/inactiva → 403.
  - E3: Superado el límite de intentos → bloqueo temporal.

---

## CU-02 — Registrar entrada (RF-02, RF-14..RF-20) — **núcleo del sistema**

- **Actor:** Colaborador · **Canal:** App Flutter.
- **Precondiciones:** sesión activa; permisos de cámara y ubicación concedidos.
- **Flujo principal:**
  1. El colaborador abre "Registrar entrada".
  2. La app verifica **antifraude local**: mock location, root/jailbreak, GPS activo, precisión (RN-20..RN-24).
  3. (Opcional según política) solicita **biometría** (RN/RF-19).
  4. El colaborador **escanea el QR** del centro; la app decodifica el token firmado (RN-25).
  5. La app captura **posición GPS** (lat/lng + precisión) y (opcional) **foto de evidencia**.
  6. La app arma la operación con **UUID único** y la persiste localmente (RN-50).
  7. Si hay red, la envía al backend; si no, queda en **cola de sincronización**.
  8. El backend valida: QR (firma+vigencia+nonce), geocerca (RN-13), precisión (RN-14), horario/turno (RN-15, RN-16), antifraude (RN-20..RN-28), idempotencia/replay (RN-26, RN-51).
  9. El backend fija la **hora oficial de servidor** (RN-11), persiste el registro y emite evento de dominio `AttendanceRegistered`.
  10. El backend responde con resultado (`ACEPTADO` / `RECHAZADO` + motivo) y lo audita.
- **Excepciones / rechazos:** `INVALID_QR`, `OUT_OF_GEOFENCE`, `LOW_GPS_ACCURACY`, `GPS_UNAVAILABLE`, `FRAUD_MOCK_LOCATION`, `FRAUD_ROOTED_DEVICE`, `FRAUD_GPS_SPOOF_APP`, `REPLAY_DETECTED`, `OUT_OF_SCHEDULE`.
- **Postcondición:** registro persistido (aceptado o rechazado con motivo), auditado y notificado si aplica.

---

## CU-03 — Registrar salida (RF-03)

- Igual a CU-02 pero requiere **ENTRADA abierta** en el mismo centro/turno (RN-12). Calcula horas trabajadas/extra (RN-17) al cerrar.

---

## CU-04 — Registrar evento intermedio (RF-04)

- Igual a CU-02 para tipos `INICIO_DESCANSO/FIN_DESCANSO/CAMBIO_SITIO`, validando **secuencia coherente** (RN-12).

---

## CU-05 — Sincronizar registros offline (RF-21)

- **Actor:** Sistema (sincronizador) + Colaborador.
- **Flujo principal:**
  1. Al recuperar conectividad, la app envía por lotes los registros en cola (orden FIFO, con UUID).
  2. El backend procesa cada uno de forma **idempotente** (RN-51) aplicando todas las validaciones con hora de servidor.
  3. El backend responde por cada registro: `ACEPTADO / RECHAZADO / INCIDENCIA` (RN-53).
  4. La app actualiza el estado local y muestra **confirmación** (RN-54).
- **Excepciones:** fallo de red → **reintento con backoff** (RN-52); conflicto → prevalece el servidor (RN-53).

---

## CU-06 — Gestionar geocerca de un centro (RF-10)

- **Actor:** Administrador.
- **Flujo:** define centro (lat/lng) + radio (o polígono) + precisión máxima + política de evidencia/biometría; el sistema valida consistencia y audita el cambio.

---

## CU-07 — Generar / rotar QR de centro (RF-14)

- **Actor:** Administrador.
- **Flujo:** el sistema genera un token de centro **firmado** con vigencia y `nonce`; puede rotarse. El QR expira según política (RN-25).

---

## CU-08 — Resolver incidencia (RF-09)

- **Actor:** RR.HH. / Supervisor.
- **Flujo:** revisa la incidencia (retardo, falta, registro rechazado, bandera antifraude), aprueba/rechaza con comentario; el resultado se audita (RN-43, RN-60).

---

## CU-09 — Exportar reporte (RF-11)

- **Actor:** Admin / RR.HH. / Supervisor.
- **Flujo:** selecciona filtros (rango, centro, proyecto, empleado, tipo) y formato (Excel/PDF/CSV); el sistema genera el archivo (asíncrono si es grande) y lo pone a disposición.

---

## CU-10 — Consultar auditoría (RF-12)

- **Actor:** Auditor / Admin.
- **Flujo:** consulta paginada/filtrable de la bitácora (solo lectura), respetando aislamiento de tenant (RN-61, RN-62).

---

## CU-11 — Monitorear en tiempo real (RF-25, RF-26)

- **Actor:** Supervisor.
- **Flujo:** el portal se suscribe (WebSocket) a eventos de asistencia y estado de sincronización de su ámbito y los muestra en dashboards y **mapa en tiempo real**.
