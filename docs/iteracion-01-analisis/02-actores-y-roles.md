# 02 — Actores y roles (RBAC)

## 2.1 Actores del sistema

| Actor | Descripción | Canal principal |
|---|---|---|
| **Colaborador (Empleado)** | Registra su asistencia en campo | App Flutter |
| **Supervisor** | Monitorea a su equipo, valida incidencias, ve mapa en tiempo real | Portal Angular |
| **Administrador de Empresa** | Gestiona usuarios, centros, horarios, geocercas de **su** empresa (tenant) | Portal Angular |
| **Administrador de RR.HH.** | Gestiona incidencias, reportes y reglas de asistencia | Portal Angular |
| **Super Administrador (Plataforma)** | Gestiona empresas/tenants y configuración global | Portal Angular |
| **Auditor** | Consulta bitácora de auditoría (solo lectura) | Portal Angular |
| **Sistema / Jobs** | Actores no humanos: scheduler, sincronizador, notificador, generador de reportes | Backend |

## 2.2 Roles y jerarquía

```
SUPER_ADMIN            (global, cross-tenant)
 └── COMPANY_ADMIN     (administra un tenant)
      ├── HR_ADMIN     (incidencias, reportes, reglas)
      ├── SUPERVISOR   (equipo/centros asignados)
      ├── AUDITOR      (solo lectura de auditoría)
      └── EMPLOYEE     (registra su propia asistencia)
```

- Los roles son **por tenant** (salvo `SUPER_ADMIN`, que es global).
- El modelo RBAC soporta **permisos granulares** agrupados en roles; un usuario puede tener uno o varios roles dentro de un tenant.
- Preparado para **scoping por centro/proyecto** (un supervisor solo ve sus centros asignados).

## 2.3 Catálogo de permisos (extracto)

Formato `recurso:acción`. Acciones: `read`, `create`, `update`, `delete`, `export`, `approve`, `manage`.

| Permiso | Descripción |
|---|---|
| `company:manage` | Alta/baja/edición de empresas (solo SUPER_ADMIN) |
| `user:manage` | Gestión de usuarios del tenant |
| `role:manage` | Gestión de roles y permisos |
| `worksite:manage` | Gestión de centros de trabajo |
| `project:manage` | Gestión de proyectos |
| `schedule:manage` | Gestión de horarios y turnos |
| `geofence:manage` | Gestión de geocercas |
| `attendance:read` | Consulta de asistencias del ámbito autorizado |
| `attendance:register` | Registrar asistencia propia (EMPLOYEE) |
| `incident:approve` | Aprobar/rechazar incidencias |
| `report:export` | Exportar reportes |
| `audit:read` | Consultar bitácora de auditoría |
| `dashboard:read` | Ver dashboards y mapa en tiempo real |
| `sync:read` | Ver estado de sincronización |

## 2.4 Matriz de autorización (resumen)

| Permiso | SUPER_ADMIN | COMPANY_ADMIN | HR_ADMIN | SUPERVISOR | AUDITOR | EMPLOYEE |
|---|:--:|:--:|:--:|:--:|:--:|:--:|
| `company:manage` | ✅ | — | — | — | — | — |
| `user:manage` | ✅ | ✅ | — | — | — | — |
| `role:manage` | ✅ | ✅ | — | — | — | — |
| `worksite:manage` | ✅ | ✅ | — | — | — | — |
| `project:manage` | ✅ | ✅ | ✅ | — | — | — |
| `schedule:manage` | ✅ | ✅ | ✅ | — | — | — |
| `geofence:manage` | ✅ | ✅ | — | — | — | — |
| `attendance:read` | ✅ | ✅ | ✅ | ✅ (su ámbito) | — | ✅ (propia) |
| `attendance:register` | — | — | — | — | — | ✅ |
| `incident:approve` | ✅ | ✅ | ✅ | ✅ (su ámbito) | — | — |
| `report:export` | ✅ | ✅ | ✅ | ✅ (su ámbito) | — | — |
| `audit:read` | ✅ | ✅ | — | — | ✅ | — |
| `dashboard:read` | ✅ | ✅ | ✅ | ✅ (su ámbito) | — | — |
| `sync:read` | ✅ | ✅ | — | ✅ | — | — |

> El aislamiento **multi-tenant** es transversal a toda la matriz: ningún rol (excepto SUPER_ADMIN) puede acceder a datos de otro tenant. Ver [RN-30..RN-33](04-reglas-de-negocio.md).
