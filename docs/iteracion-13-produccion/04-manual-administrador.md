# 04 — Manual del administrador

Portal web **Nexus Soft Time Clock** para supervisores y administradores.

## Roles
| Rol | Puede |
|---|---|
| SUPER_ADMIN | Gestionar empresas (tenants) de la plataforma |
| COMPANY_ADMIN | Usuarios, roles, centros, proyectos, horarios, geocercas de su empresa |
| HR_ADMIN | Horarios, incidencias, reportes |
| SUPERVISOR | Monitoreo, incidencias y reportes de su ámbito |
| AUDITOR | Consulta de la bitácora de auditoría |

## Tareas frecuentes

### Empresas (SUPER_ADMIN)
`/admin/companies` → crear/editar empresas, activar/suspender. Cada alta crea su configuración por defecto.

### Usuarios y roles
`API /api/v1/users` → alta de colaboradores con roles; cambio de estado; asignación de roles.
`GET /api/v1/roles` → roles disponibles.

### Centros de trabajo y geocercas
1. Crear el **centro** con su ubicación (lat/lng) — `POST /api/v1/work-sites`.
2. Definir la **geocerca** circular (centro + radio) — `PUT /api/v1/work-sites/{id}/geofence`.
3. Generar/rotar el **QR** del centro — `POST /api/v1/work-sites/{id}/qr` (devuelve el token a imprimir/mostrar).

### Horarios y turnos
`/api/v1/schedules` y `/schedules/{id}/shifts` → definir turnos con tolerancias y ventanas de registro; asignar turnos a colaboradores (`/shift-assignments`).

### Incidencias
`/api/v1/incidents` → revisar incidencias (retardos, rechazos), **aprobar/rechazar** con comentario. Las incidencias por registro rechazado se crean automáticamente.

### Dashboards y tiempo real
`/admin/dashboard` → KPIs (asistencias/rechazos del día, incidencias abiertas, usuarios/centros activos). El canal WebSocket (`/ws`, tópico `/topic/tenant/{id}/attendance`) alimenta el mapa/monitor en vivo.

### Reportes
`GET /api/v1/reports/attendance?from&to&status&format=csv|xlsx|pdf` → exportar asistencia con filtros.

### Auditoría
`GET /api/v1/audit` → bitácora inmutable de acciones y eventos (quién, cuándo, qué).

## Documentación de la API
Swagger UI: `/swagger-ui.html`. Todas las operaciones están versionadas bajo `/api/v1` y protegidas por RBAC (permiso por operación).
