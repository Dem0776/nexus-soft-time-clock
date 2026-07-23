# Prompt para ChatGPT — Rediseño de UI del Frontend (Nexus Soft Time Clock)

Eres un diseñador de producto UI/UX senior especializado en dashboards SaaS B2B (fintech/HR-tech). Vas a rediseñar el panel administrativo web de mi producto, pantalla por pantalla, con mockups reales (no genéricos, no "de plantilla de IA"). No inventes funcionalidades, roles, campos ni endpoints que no estén descritos aquí: todo lo que sigue es el estado real del código.

---

## 1. Contexto, objetivo y alcance del proyecto

"Nexus Soft Time Clock" es un sistema de control de asistencia/fichaje laboral **multi-tenant** (multi-empresa) para PYMEs. Tiene 3 clientes:
- **App móvil (Flutter)**: la usa el empleado para fichar (entrada/salida/descansos) con geolocalización y QR del centro.
- **Backend (Java, arquitectura hexagonal/DDD)**: aplica todas las reglas de negocio y RBAC de forma autoritativa.
- **Panel web (Angular)** — **este es el que vamos a rediseñar**: lo usan roles administrativos y de supervisión, nunca el empleado raso.

**Objetivo de este ejercicio**: mejorar la interfaz visual y de UX del panel web manteniendo el stack actual (Angular Material), sin romper el modelo de datos ni el RBAC. **Alcance**: solo diseño/mockups, no se toca el backend ni la app móvil.

Funcionalidades núcleo del dominio:
- Fichaje con geofencing (radio GPS permitido por centro) y QR de acceso con caducidad configurable en minutos.
- Turnos y horarios con tolerancias de entrada/salida, ventanas de fichaje, descansos, turnos que cruzan medianoche.
- Incidencias: un fichaje rechazado abre automáticamente una incidencia que un supervisor debe resolver.
- Reportes de asistencia por colaborador, exportables a Excel y PDF.
- Mapa en tiempo real de ubicación de empleados.
- Auditoría inmutable (append-only) de acciones administrativas.
- Notificaciones al usuario autenticado.
- Gestión de empresas (multi-tenant), proyectos, centros de trabajo, usuarios y roles.

---

## 2. Roles y permisos

El backend expone en `/api/v1/auth/me` la identidad del usuario junto con sus roles y **permisos ya resueltos** (`Me.permissions: string[]`). El frontend no calcula permisos por rol: simplemente pregunta `permissions.includes('xxx:yyy')`. Esto significa que la UI debe diseñarse para **ocultar/mostrar secciones completas según permiso**, no según "el rol X ve la pantalla Y" de forma hardcodeada.

Roles existentes y su rango (usado solo para la jerarquía de asignación de roles, no define permisos por sí solo):

| Rol | Rango |
|---|---|
| SUPER_ADMIN | 100 (plataforma, cross-tenant) |
| COMPANY_ADMIN | 80 |
| HR_ADMIN | 60 |
| SUPERVISOR | 60 |
| AUDITOR | 40 |
| EMPLOYEE | 20 (no usa el panel web) |

Regla de negocio clave: **un operador solo puede otorgar roles de rango estrictamente inferior al suyo** (ej. un HR_ADMIN no puede crear otro HR_ADMIN ni un SUPER_ADMIN). Esto debe reflejarse visualmente en la pantalla de asignación de roles (ej. deshabilitar/ocultar opciones no otorgables, no solo validarlo al enviar).

Permisos usados en las rutas del panel: `dashboard:read`, `incident:approve`, `report:export`, `audit:read`, `company:manage`, `user:manage`, `worksite:manage`, `geofence:manage`, `project:manage`, `schedule:manage`.

---

## 3. Flujos actuales (comportamiento real, no aspiracional)

**Login**: `POST /api/v1/auth/login` con `email + password + companyCode` opcional (multi-tenant: el código de empresa desambigua si el email existe en varias). Devuelve `accessToken/refreshToken`; el refresh se maneja vía interceptor HTTP automático.

**Fichaje (lo hace la app móvil, pero el resultado es lo que ve el panel web en Incidencias/Reportes)**. El backend valida en este orden exacto — es útil para que los mensajes de incidencia/rechazo en la UI del panel tengan sentido:
1. Idempotencia (reenvío del mismo `operationUuid` devuelve el resultado previo, no duplica).
2. QR válido, vigente (no expirado) y coincide con el tenant/centro de trabajo.
3. Tipo de evento habilitado para la empresa (ENTRADA/SALIDA siempre lo están; los intermedios son configurables en `/event-types`).
4. Antifraude: mock location, dispositivo rooteado/jailbreak, apps de GPS falso, GPS deshabilitado.
5. Geocerca + precisión GPS (umbral configurable por centro).
6. Ventana de horario del turno asignado (si tiene turno vigente).
7. Secuencia coherente de jornada (entrada/salida emparejadas en el mismo centro, sin dobles descansos).
8. Políticas obligatorias del centro: foto y/o biometría si el centro lo exige.
9. Anti-replay (nonce del QR de un solo uso).

Si cualquier paso falla → el fichaje queda `REJECTED` con un motivo (`RejectionReason`) y **se abre automáticamente una incidencia** ligada a ese registro de asistencia.

**Resolución de incidencias** (`/incidents`, permiso `incident:approve`): el supervisor ve la bandeja de incidencias abiertas (con motivo, empleado, fecha, prioridad), abre un diálogo de resolución y decide `APPROVED | REJECTED | RESOLVED` con una nota. Esto es un flujo de alta frecuencia para el rol SUPERVISOR — debe ser rápido (poder resolver muchas seguidas sin fricción).

**Generación de reportes** (`/reports`, permiso `report:export`): se elige un rango de fechas (dispara recarga contra backend), y sobre el dataset ya cargado se aplican filtros locales (texto por columna, rangos numéricos min/max, estado). Hay 9 columnas numéricas filtrables por rango — el reto de UX es no abrumar con 9 pares de inputs simultáneos. Los retardos ≥ 3 se resaltan visualmente. Exporta a Excel/PDF.

**Gestión de centro de trabajo + geocerca** (`/work-sites/:id/geofence`, permiso `geofence:manage`): se define lat/long + radio en metros sobre un mapa (Leaflet), y se puede emitir un QR (`POST /work-sites/{id}/qr`) con TTL en minutos que expira solo.

**Asignación de turnos** (`/scheduling`): se crean horarios (`Schedule`), luego turnos (`Shift`) dentro de un horario, y luego se asignan (`Assignment`) usuario + turno + centro + vigencia (`validFrom/validTo`).

---

## 4. Stack frontend y componentes disponibles (restricción real, no cambiar)

- Angular 18, **standalone components**, lazy loading por ruta (no hay NgModules).
- **Angular Material 18** (mat-toolbar, mat-sidenav, mat-table, mat-card, mat-menu, mat-dialog...) — cualquier propuesta debe ser realizable con Material + CSS. No asumas librerías de componentes nuevas.
- Tipografía: Inter Variable. Iconos: Material Icons (auto-hospedados).
- Leaflet (mapa), jsPDF + jspdf-autotable + xlsx (exportar reportes), `qrcode` (generar QR), stompjs/sockjs (tiempo real por websocket).
- Estado con **Angular Signals** (no NgRx): `AuthStore`, `ThemeService` son servicios con signals.

Componentes UI reutilizables ya existentes (reutilízalos en los mockups, no los reinventes):
- `StatCardComponent` — tarjetas de métrica (dashboard).
- `StatusChipComponent` — chip de estado con 5 variantes semánticas (success/warning/danger/info/neutral).
- `PageHeaderComponent`, `EmptyStateComponent`, `ConfirmDialogComponent`.

---

## 5. Paleta, identidad visual y restricciones

Sistema de diseño propio sobre Angular Material, con tema claro/oscuro real (toggle persistido, clase `.dark` en `<body>`):

```css
/* Tema claro */
--app-bg: #f4f6fb;   --surface: #ffffff;  --surface-2: #f7f9fc;
--border: rgba(16,24,40,.1); --text: #101828; --text-muted: #667085;
--brand: #3949ab; /* índigo */

--success:#0f7a52 (bg #e5f6ef)  --warning:#b54708 (bg #fdf0e6)
--danger: #b42318 (bg #fdecea)  --info:   #1856c9 (bg #e8f0fe)
--neutral:#475467 (bg #eef1f5)

/* Tema oscuro (misma paleta semántica, tokens propios) */
--app-bg:#0f1420 --surface:#171c28 --surface-2:#1e2433
--text:#e6e9f0 --text-muted:#98a2b3

--sp-1..6: 4/8/12/16/24/32px   --radius-sm/md/lg: 8/12/16px
--shadow-1, --shadow-2 (sombras suaves, sin drop shadows duros)
```

Base de Material: `theme-type` light/dark, `primary: mat.$blue-palette`, `tertiary: mat.$azure-palette`, densidad 0 (sin comprimir).

**Restricciones de identidad visual**:
- El color de marca (índigo) se puede refinar pero **debe seguir leyéndose como corporativo/serio** (RRHH/nómina), no como producto de consumo.
- **Evita el look genérico de "IA generó esto"**: nada de gradientes morado-a-rosa por defecto, nada de ilustraciones stock genéricas, nada de glassmorphism sin razón. Debe sentirse como un producto con identidad propia, no un template.
- Debe funcionar igual de bien en modo claro y oscuro — dame ambos si propones cambios de paleta.
- Todo debe ser traducible directo a estas CSS custom properties (no props nuevas sin justificar por qué las custom actuales no alcanzan).

---

## 6. Pantallas existentes y servicios del backend

Cada pantalla del panel consume estos endpoints reales (todos bajo `/api/v1`, todos requieren tenant salvo que se indique "plataforma"):

| Pantalla (ruta) | Permiso | Endpoints backend |
|---|---|---|
| `/dashboard` | — | `GET /dashboard/summary` |
| `/map` | `dashboard:read` | (websocket STOMP, ubicación en tiempo real) |
| `/notifications` | — | `GET /notifications/me` |
| `/incidents` | `incident:approve` | `GET /incidents`, `PATCH /incidents/{id}/resolve` |
| `/reports` | `report:export` | `GET /reports/attendance`, `GET /reports/attendance-summary` |
| `/audit` | `audit:read` | `GET /audit` |
| `/companies` | `company:manage` (plataforma) | `GET/POST /companies`, `GET/PUT /companies/{id}`, `PATCH /companies/{id}/status` |
| `/users` | `user:manage` | `GET/POST /users`, `GET /users/{id}`, `PATCH /users/{id}/status`, `PUT /users/{id}/roles`, `GET /roles` |
| `/work-sites` | `worksite:manage` | `GET/POST /work-sites`, `GET/PUT /work-sites/{id}`, `PATCH /work-sites/{id}/status` |
| `/work-sites/:id/geofence` | `geofence:manage` | `GET/PUT /work-sites/{id}/geofence`, `POST /work-sites/{id}/qr` |
| `/projects` | `project:manage` | `GET/POST /projects`, `GET/PUT /projects/{id}` |
| `/scheduling` | `schedule:manage` | `GET/POST /schedules`, `GET/PUT /schedules/{id}`, `GET/POST /schedules/{id}/shifts`, `PUT /shifts/{id}`, `GET/POST /shift-assignments` |
| `/event-types` | `schedule:manage` | `GET/PUT /attendance/event-types` |
| Login | — | `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `GET /auth/me` |

### DTOs reales por pantalla (usa exactamente estos campos)

```typescript
// Auth
interface LoginRequest { email: string; password: string; companyCode?: string; }
interface Me { userId: string; tenantId: string | null; platformAdmin: boolean; roles: string[]; permissions: string[]; }

// Dashboard
interface DashboardSummary {
  attendanceTodayAccepted: number; attendanceTodayRejected: number;
  openIncidents: number; activeUsers: number; activeWorkSites: number;
}

// Empresas
type CompanyStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';
interface Company { id: string; code: string; name: string; legalName?: string; emailDomain?: string; timezone: string; locale: string; status: CompanyStatus; }

// Usuarios
type UserStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED' | 'INVITED';
interface User { id: string; email: string; firstName: string; lastName: string; employeeCode?: string; status: UserStatus; roles: string[]; }
interface Role { code: string; name: string; }

// Centros de trabajo + geocerca
type WorkSiteStatus = 'ACTIVE' | 'INACTIVE';
interface WorkSite {
  id: string; code: string; name: string; address?: string;
  latitude: number; longitude: number; timezone?: string;
  gpsAccuracyMaxM?: number; requirePhoto?: boolean; requireBiometric?: boolean; status: WorkSiteStatus;
}
interface Geofence { workSiteId: string; latitude: number; longitude: number; radiusM: number; active: boolean; }
interface QrToken { token: string; expiresAt: string; }

// Proyectos
type ProjectStatus = 'ACTIVE' | 'INACTIVE' | 'CLOSED';
interface Project { id: string; code: string; name: string; status: ProjectStatus; startsOn?: string; endsOn?: string; }

// Horarios y turnos
interface Schedule { id: string; code: string; name: string; timezone?: string; status: 'ACTIVE' | 'INACTIVE'; }
interface Shift {
  id: string; scheduleId: string; name: string; startTime: string; endTime: string;
  crossesMidnight: boolean; breakMinutes: number;
  lateToleranceMin: number; earlyToleranceMin: number;
  windowBeforeMin: number; windowAfterMin: number;
}
interface Assignment { id: string; userId: string; shiftId: string; workSiteId?: string; validFrom: string; validTo?: string; }

// Tipos de evento
type AttendanceEventType = 'ENTRADA' | 'SALIDA' | 'INICIO_DESCANSO' | 'FIN_DESCANSO' | 'CAMBIO_SITIO';
interface EventTypeSetting { eventType: AttendanceEventType; enabled: boolean; label: string; }
// ENTRADA y SALIDA son núcleo: siempre habilitadas, no configurables

// Incidencias
type IncidentResolution = 'APPROVED' | 'REJECTED' | 'RESOLVED';
interface Incident {
  id: string; userId: string; type: string; status: string; priority: string;
  incidentDate: string; relatedAttendanceId?: string; description?: string;
  resolutionNote?: string; resolvedBy?: string; resolvedAt?: string;
}

// Reportes — una fila por empleado, ya agregada por el backend
interface AttendanceReport {
  employeeNumber: string; employeeName: string; workCenter: string;
  expectedDays: number; attendedDays: number;
  justifiedAbsences: number; unjustifiedAbsences: number; lateArrivals: number;
  workedHours: number; overtimeHours: number; totalHours: number;
  active: boolean; compliancePercentage: number;
}
// Filtros: rango de fechas, búsqueda libre, filtros de texto por columna, rangos numéricos min/max
// por columna, estado (ALL/ACTIVE/INACTIVE). Retardos >= 3 se marcan visualmente en rojo.

// Auditoría (append-only)
interface AuditEntry { id: string; actorUserId: string; action: string; resourceType: string; resourceId: string; newValues?: string; createdAt: string; }

// Notificaciones
interface AppNotification { id: string; channel: string; type: string; title: string; body: string; status: string; }
```

---

## 7. Cómo vamos a trabajar (importante, sigue este proceso)

1. Con todo el contexto anterior, primero **propón el mapa completo de pantallas y el orden sugerido de rediseño** (cuáles primero, por qué — ej. por frecuencia de uso, por impacto visual, por complejidad).
2. A partir de ahí trabajaremos **una pantalla a la vez**: generas la propuesta visual de esa pantalla, la reviso, hacemos correcciones, y **solo avanzamos a la siguiente cuando la apruebe explícitamente**.
3. Mantén **consistencia real entre pantallas** (misma escala tipográfica, mismos componentes, mismo lenguaje de espaciado/color) — cada pantalla nueva debe sentirse parte del mismo sistema, no un mockup aislado.
4. Evita a toda costa que el resultado se sienta genérico o "hecho por IA" (ver restricciones de identidad visual en la sección 5).
5. Todo debe ser implementable con Angular Material 18 + las CSS custom properties ya definidas — si propones un cambio de token, dime exactamente qué valor cambia y por qué.

Empieza por el punto 1: el mapa de pantallas y el orden de trabajo.
