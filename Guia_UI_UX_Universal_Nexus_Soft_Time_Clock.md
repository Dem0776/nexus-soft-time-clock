# Nexus Soft Time Clock — Guía Universal de UI/UX para Implementación del Frontend

## 1. Propósito

Este documento define las reglas visuales, patrones UX, restricciones funcionales y criterios técnicos que deben seguir Claude, Codex o cualquier desarrollador encargado de implementar el panel administrativo web de **Nexus Soft Time Clock**.

El objetivo es garantizar que todas las pantallas:

- Se vean como parte del mismo producto.
- Sean fieles a los mockups aprobados.
- Respeten DTOs, endpoints, permisos y reglas reales.
- Sean implementables con Angular 18 y Angular Material 18.
- Funcionen en tema claro y oscuro.
- Eviten el aspecto genérico de dashboards generados por IA.
- Mantengan una identidad corporativa, seria y adecuada para RR. HH.

---

# 2. Contexto del producto

Nexus Soft Time Clock es una plataforma multiempresa de control de asistencia.

El ecosistema se compone de:

- Aplicación móvil Flutter para empleados.
- Backend Java con arquitectura hexagonal y DDD.
- Panel administrativo Angular para perfiles administrativos y supervisores.

El backend es la fuente autoritativa para:

- Permisos.
- RBAC.
- Estados.
- Reglas de negocio.
- Validaciones.
- Datos disponibles.
- Acciones permitidas.

El frontend no debe reconstruir ni duplicar estas reglas.

---

# 3. Stack obligatorio

- Angular 18.
- Standalone Components.
- Lazy loading por ruta.
- Angular Material 18.
- Angular Signals.
- Inter Variable.
- Material Icons autoalojados.
- CSS Custom Properties.
- Leaflet.
- jsPDF y jspdf-autotable.
- xlsx.
- qrcode.
- stompjs y sockjs.

## Restricciones

No agregar librerías visuales nuevas sin justificación.

No sustituir Angular Material por:

- PrimeNG.
- Tailwind UI.
- Bootstrap.
- Ant Design.
- Librerías de dashboard.
- Sistemas de componentes cerrados.

Toda interfaz debe poder implementarse con Angular Material y CSS.

---

# 4. Tema universal

## 4.1 Tema claro

```css
:root {
  --app-bg: #f4f6fb;
  --surface: #ffffff;
  --surface-2: #f7f9fc;
  --surface-hover: #f1f4f9;

  --border: rgba(16, 24, 40, 0.10);
  --border-strong: rgba(16, 24, 40, 0.16);

  --text: #101828;
  --text-muted: #667085;
  --text-soft: #98a2b3;

  --brand: #3949ab;
  --brand-hover: #303f9f;
  --brand-soft: #eef0ff;
  --brand-border: #cdd3ff;

  --success: #0f7a52;
  --success-bg: #e5f6ef;

  --warning: #b54708;
  --warning-bg: #fdf0e6;

  --danger: #b42318;
  --danger-bg: #fdecea;

  --info: #1856c9;
  --info-bg: #e8f0fe;

  --neutral: #475467;
  --neutral-bg: #eef1f5;

  --sp-1: 4px;
  --sp-2: 8px;
  --sp-3: 12px;
  --sp-4: 16px;
  --sp-5: 24px;
  --sp-6: 32px;

  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;

  --shadow-1: 0 1px 2px rgba(16, 24, 40, 0.04);
  --shadow-2: 0 4px 12px rgba(16, 24, 40, 0.06);
}
```

## 4.2 Tema oscuro

```css
body.dark {
  --app-bg: #0f1420;
  --surface: #171c28;
  --surface-2: #1e2433;
  --surface-hover: #252c3d;

  --border: rgba(230, 233, 240, 0.10);
  --border-strong: rgba(230, 233, 240, 0.18);

  --text: #e6e9f0;
  --text-muted: #98a2b3;
  --text-soft: #667085;

  --brand: #7c88ff;
  --brand-hover: #9099ff;
  --brand-soft: rgba(124, 136, 255, 0.14);
  --brand-border: rgba(124, 136, 255, 0.32);

  --success: #59c69b;
  --success-bg: rgba(15, 122, 82, 0.18);

  --warning: #f2a45f;
  --warning-bg: rgba(181, 71, 8, 0.18);

  --danger: #f97066;
  --danger-bg: rgba(180, 35, 24, 0.18);

  --info: #76a7ff;
  --info-bg: rgba(24, 86, 201, 0.18);

  --neutral: #b6becb;
  --neutral-bg: rgba(71, 84, 103, 0.24);

  --shadow-1: 0 1px 2px rgba(0, 0, 0, 0.22);
  --shadow-2: 0 6px 18px rgba(0, 0, 0, 0.28);
}
```

## 4.3 Identidad visual

El color principal debe sentirse:

- Corporativo.
- Sobrio.
- B2B.
- Adecuado para RR. HH.
- Compatible con nómina y control operativo.

Evitar:

- Gradientes morado-rosa.
- Neones.
- Glassmorphism.
- Sombras pesadas.
- Fondos decorativos excesivos.
- Un color diferente por módulo.
- Círculos pastel grandes con íconos decorativos.
- Apariencia de template genérico.

---

# 5. Principios UX universales

## 5.1 La tarea principal domina

Cada pantalla debe responder claramente:

> ¿Qué viene a hacer el usuario aquí?

La acción primaria debe identificarse en menos de tres segundos.

Ejemplos:

- Incidencias: revisar y resolver.
- Reportes: filtrar, consultar y exportar.
- Mapa: localizar empleados.
- Usuarios: encontrar, crear o editar.
- Geocerca: definir ubicación y radio.
- Horarios: administrar horarios y turnos.

No convertir cada pantalla en un dashboard.

## 5.2 Menos bloques, mejor jerarquía

No llenar espacios con:

- KPIs.
- Gráficas.
- Actividad reciente.
- Resúmenes.
- Atajos.
- Métricas derivadas.
- Cards informativas redundantes.

Solo mostrar estos elementos si el backend los entrega y ayudan a la tarea principal.

## 5.3 Revelado progresivo

Usar:

- Drawer para detalle.
- Dialog para confirmaciones.
- Expansion Panel para filtros secundarios.
- Tabs solo cuando existan secciones reales.
- Botón “Más filtros” para opciones menos frecuentes.

No mostrar toda la complejidad al mismo tiempo.

## 5.4 Colores semánticos

- Índigo: interacción y navegación.
- Verde: éxito, activo o aprobado.
- Rojo: error, rechazo o peligro.
- Naranja: advertencia o prioridad media.
- Gris: estados neutros o deshabilitados.

No usar color como decoración.

## 5.5 Saturación

Una pantalla está saturada cuando:

- Más de tres bloques compiten por atención.
- Hay cards, tabla y drawer visibles sin necesidad.
- Se usan demasiados colores semánticos.
- Los filtros ocupan varias filas.
- Las acciones se repiten.
- Se priorizan datos secundarios antes de la tarea principal.

---

# 6. Shell universal

## 6.1 Sidenav

Ancho:

- Expandido: 240–264 px.
- Colapsado: 72–80 px.

Estructura:

```text
Logo
Inicio
Operación
  Dashboard
  Incidencias
  Mapa en tiempo real
Análisis
  Reportes
  Auditoría
Organización
  Usuarios
  Empresas
  Centros de trabajo
  Proyectos
Planificación
  Horarios y turnos
  Asignaciones
  Tipos de evento
Cuenta
  Notificaciones
Tenant actual
```

La navegación debe construirse con permisos.

Correcto:

```typescript
permissions.includes('incident:approve')
```

Incorrecto:

```typescript
role === 'SUPERVISOR'
```

## 6.2 Toolbar

Debe contener solo:

- Colapsar sidenav.
- Búsqueda global, solo si funciona.
- Contexto de tenant.
- Tema claro/oscuro.
- Notificaciones.
- Perfil.

## 6.3 Contenedor

```css
.page-container {
  max-width: 1600px;
  margin: 0 auto;
  padding: var(--sp-5);
}
```

---

# 7. Tipografía

```css
font-family: "Inter Variable", Inter, sans-serif;
```

Escala:

```css
--font-page-title: 28px;
--font-section-title: 18px;
--font-card-title: 15px;
--font-body: 14px;
--font-small: 12px;
--font-caption: 11px;
```

Pesos:

- 700: título de página.
- 600: sección y labels importantes.
- 500: navegación y botones.
- 400: cuerpo.

---

# 8. Componentes universales

## 8.1 Page Header

Debe incluir:

- Título.
- Descripción breve.
- Breadcrumb si aplica.
- Acción primaria a la derecha.

## 8.2 Tabla

Patrón principal para catálogos:

- Filas de 56–64 px.
- Hover suave.
- Selección con `brand-soft`.
- Acciones a la derecha.
- Paginación consistente.
- Scroll horizontal en laptop o tablet.
- Header fijo cuando aporte valor.

## 8.3 Drawer

Usar para:

- Detalle.
- Resolución.
- Filtros complejos.
- Edición rápida.

Ancho:

- 420–480 px.
- Máximo 38% del viewport.

## 8.4 Dialog

Usar para:

- Suspender.
- Desactivar.
- Rechazar.
- Eliminar.
- Confirmar cambios sensibles.

## 8.5 Formularios

- Máximo dos columnas.
- Labels arriba.
- Ayuda breve debajo.
- Campos obligatorios con `*`.
- Acción primaria al final.
- Cancelar como secundaria.
- No usar panel lateral si solo repite obviedades.

## 8.6 Status Chip

Usar únicamente:

- success.
- warning.
- danger.
- info.
- neutral.

No crear un color por rol.

---

# 9. Patrones por tipo de pantalla

## Catálogo

```text
Page Header
Barra de búsqueda y filtros
Tabla
Paginación
Drawer opcional
```

## Formulario

```text
Breadcrumb
Título
Formulario principal
Ayuda contextual mínima
Acciones
```

## Pantalla operativa

```text
Título
Filtros mínimos
Contenido principal dominante
Detalle bajo demanda
Acciones rápidas
```

## Configuración técnica

```text
Título
Contexto de entidad
Configuración
Vista previa real
Guardar
```

---

# 10. Reglas por pantalla

## 10.1 Login

Campos:

- email.
- password.
- companyCode opcional.

No inventar:

- Recuperar contraseña.
- SSO.
- Active Directory.
- Recordar sesión.
- Registro.
- Login social.

## 10.2 Dashboard

Solo usar:

```typescript
interface DashboardSummary {
  attendanceTodayAccepted: number;
  attendanceTodayRejected: number;
  openIncidents: number;
  activeUsers: number;
  activeWorkSites: number;
}
```

Patrón aprobado:

- Asistencia de hoy.
- Incidencias abiertas.
- Operación activa.

No usar sparklines, gráficas ni comparativos.

## 10.3 Incidencias

Vista:

- Buscador.
- Prioridad.
- Estado.
- Más filtros.
- Tabla.
- Revisar.
- Drawer bajo demanda.

Drawer:

- Información real.
- APPROVED.
- REJECTED.
- RESOLVED.
- Nota.
- Guardar.
- Guardar y abrir siguiente.

## 10.4 Reportes

Vista:

- Rango de fechas.
- Búsqueda.
- Estado.
- Filtros por columna.
- Exportar Excel.
- Exportar PDF.
- Tabla completa.

Los filtros numéricos deben ir en drawer.

Los retardos mayores o iguales a 3 se resaltan.

## 10.5 Mapa

El mapa debe dominar.

Mostrar:

- Estado websocket.
- Última actualización si existe.
- Centro.
- Búsqueda.
- Lista lateral.
- Marcadores.
- Estado vacío.
- Reconexión.

## 10.6 Notificaciones

Usar lista cronológica.

No inventar acciones no soportadas.

## 10.7 Auditoría

Mostrar solo:

- Fecha.
- Actor.
- Acción.
- Recurso.
- ID.
- Nuevos valores.

No inventar IP, navegador, dispositivo o sistema operativo.

## 10.8 Usuarios

Campos reales:

```typescript
interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  employeeCode?: string;
  status: UserStatus;
  roles: string[];
}
```

No agregar teléfono, centro principal, proyectos o permisos directos.

## 10.9 Roles

Regla:

Un operador solo puede otorgar roles de rango estrictamente inferior.

| Rol | Rango |
|---|---:|
| SUPER_ADMIN | 100 |
| COMPANY_ADMIN | 80 |
| HR_ADMIN | 60 |
| SUPERVISOR | 60 |
| AUDITOR | 40 |
| EMPLOYEE | 20 |

## 10.10 Empresas

Campos:

```typescript
interface Company {
  id: string;
  code: string;
  name: string;
  legalName?: string;
  emailDomain?: string;
  timezone: string;
  locale: string;
  status: CompanyStatus;
}
```

No agregar RFC, teléfono, país, dirección o estadísticas.

## 10.11 Centros de trabajo

Campos:

```typescript
interface WorkSite {
  id: string;
  code: string;
  name: string;
  address?: string;
  latitude: number;
  longitude: number;
  timezone?: string;
  gpsAccuracyMaxM?: number;
  requirePhoto?: boolean;
  requireBiometric?: boolean;
  status: WorkSiteStatus;
}
```

No agregar empresa, proyecto, foto almacenada o actividad reciente.

## 10.12 Geocerca y QR

```typescript
interface Geofence {
  workSiteId: string;
  latitude: number;
  longitude: number;
  radiusM: number;
  active: boolean;
}

interface QrToken {
  token: string;
  expiresAt: string;
}
```

La geocerca es circular.

No usar polígonos, vértices, área, horarios o métricas.

QR:

- TTL.
- Generar.
- Mostrar expiración.
- Mostrar estado vencido.

## 10.13 Proyectos

```typescript
interface Project {
  id: string;
  code: string;
  name: string;
  status: ProjectStatus;
  startsOn?: string;
  endsOn?: string;
}
```

No agregar empresa, descripción, presupuesto o progreso.

## 10.14 Horarios

```typescript
interface Schedule {
  id: string;
  code: string;
  name: string;
  timezone?: string;
  status: 'ACTIVE' | 'INACTIVE';
}
```

El formulario solo contiene estos campos.

Los turnos se administran en el detalle del horario.

## 10.15 Turnos

```typescript
interface Shift {
  id: string;
  scheduleId: string;
  name: string;
  startTime: string;
  endTime: string;
  crossesMidnight: boolean;
  breakMinutes: number;
  lateToleranceMin: number;
  earlyToleranceMin: number;
  windowBeforeMin: number;
  windowAfterMin: number;
}
```

No agregar tipo diurno, inicio de descanso, días o colores.

## 10.16 Asignaciones

```typescript
interface Assignment {
  id: string;
  userId: string;
  shiftId: string;
  workSiteId?: string;
  validFrom: string;
  validTo?: string;
}
```

No agregar proyecto, evento, historial o creador.

## 10.17 Tipos de evento

Tipos fijos:

```typescript
type AttendanceEventType =
  | 'ENTRADA'
  | 'SALIDA'
  | 'INICIO_DESCANSO'
  | 'FIN_DESCANSO'
  | 'CAMBIO_SITIO';
```

Solo se puede cambiar:

- label.
- enabled.

ENTRADA y SALIDA permanecen activas.

No crear, eliminar ni reordenar.

---

# 11. Responsive

## Desktop

- Sidenav expandido.
- Tabla completa.
- Drawer lateral.
- Formularios de dos columnas.

## Laptop

- Menos columnas secundarias.
- Drawer máximo 420 px.
- Scroll horizontal.
- Reducir paneles informativos.

## Tablet

- Sidenav colapsado.
- Una columna.
- Drawer de 70% o fullscreen.
- Acciones sticky.
- Filtros en dialog.

---

# 12. Accesibilidad

- Contraste AA.
- Foco visible.
- Navegación con teclado.
- Labels reales.
- `aria-label` en icon buttons.
- No depender solo del color.
- Mensajes de error asociados.
- Orden lógico de tabulación.

---

# 13. Estados universales

## Loading

- Skeleton.
- Layout estable.
- Spinner solo en acciones puntuales.

## Empty

- Título.
- Descripción.
- Acción solo si existe.

## Sin resultados

- Mensaje claro.
- Limpiar filtros.

## Error

- Mensaje humano.
- Reintentar.

## Guardando

- Deshabilitar botón.
- Evitar doble envío.

## Éxito

- Snackbar breve.

---

# 14. Anti-patrones prohibidos

- Cards excesivas.
- Sparklines sin backend.
- Gráficas para llenar espacio.
- Donas decorativas.
- Ilustraciones stock.
- Emojis.
- Gradientes de IA.
- Glassmorphism.
- Campos inventados.
- Tabs sin contenido real.
- Actividad reciente inventada.
- Roles renombrados.
- Acciones sin endpoint.
- Datos aspiracionales.

---

# 15. Criterios de aceptación

## Datos

- Todos los campos existen.
- Todas las acciones tienen endpoint.
- Los estados existen.
- No hay relaciones inventadas.
- No hay métricas inventadas.

## UX

- La tarea principal domina.
- Se entiende rápido.
- No está saturada.
- Filtros dosificados.
- Acciones jerarquizadas.

## UI

- Usa tokens.
- Respeta tipografía.
- Mantiene shell.
- Usa colores semánticos.
- Funciona en dark mode.

## Técnica

- Standalone Component.
- Signals.
- Lazy loading.
- Permisos con `permissions.includes()`.
- Sin librerías nuevas.
- Sin lógica RBAC duplicada.

---

# 16. Prompt base para Claude

```text
Actúa como arquitecto frontend senior y diseñador UI/UX especializado en SaaS B2B de RR. HH.

Implementa la pantalla solicitada de Nexus Soft Time Clock usando exclusivamente:

- Angular 18.
- Standalone Components.
- Angular Material 18.
- Angular Signals.
- CSS Custom Properties del tema universal.
- Inter Variable.
- Material Icons.

Respeta estrictamente:

1. DTOs y endpoints.
2. Permisos resueltos por backend.
3. Patrones universales de esta guía.
4. Shell aprobado.
5. Tema claro y oscuro.
6. Accesibilidad AA.
7. Responsive para desktop, laptop y tablet.

No inventes:

- Campos.
- Endpoints.
- Métricas.
- Roles.
- Relaciones.
- Acciones.
- Estados.
- Gráficas.
- Historiales.

Antes de escribir código, entrega:

1. Objetivo UX.
2. Jerarquía visual.
3. Componentes Angular Material.
4. Signals necesarios.
5. Permisos.
6. Estados loading, empty, error y success.
7. Validación DTO por DTO.

Después genera:

- HTML.
- TypeScript.
- SCSS.
- Tema claro y oscuro.
- Responsive.
```

---

# 17. Prompt para reproducir mockups

```text
Reproduce el mockup aprobado con precisión visual, pero no copies información inventada.

La imagen es referencia únicamente para:

- Layout.
- Jerarquía.
- Espaciado.
- Densidad.
- Tipografía.
- Tabla.
- Drawer.
- Navegación.
- Botones.
- Colores semánticos.

La fuente de verdad funcional es:

1. DTO.
2. Endpoint.
3. Permiso.
4. Regla de negocio.

Si el mockup contiene algo no soportado, elimínalo y conserva la composición mediante espacio, agrupación o ayuda contextual real.
```

---

# 18. Estructura sugerida

```text
src/app/
  core/
    auth/
    guards/
    interceptors/
    services/
    stores/
  layout/
    app-shell/
    sidenav/
    toolbar/
  shared/
    components/
      page-header/
      stat-card/
      status-chip/
      empty-state/
      confirm-dialog/
      data-table/
      filter-bar/
      detail-drawer/
  features/
    dashboard/
    incidents/
    reports/
    map/
    notifications/
    audit/
    users/
    companies/
    work-sites/
    projects/
    scheduling/
    event-types/
```

---

# 19. Regla final

Cuando exista conflicto:

```text
DTO y endpoint > regla de negocio > permiso > mockup > decoración
```

La interfaz correcta es la que:

- Se ve limpia.
- Se siente consistente.
- Reduce fricción.
- No inventa información.
- Puede implementarse directamente.
- Mantiene el mismo lenguaje visual en todas las pantallas.
