import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { requirePermission } from './core/guards/permission.guard';

/**
 * Enrutado con lazy loading + standalone components (Angular Style Guide).
 * - /login: página pública sin shell.
 * - '' : shell autenticado (MainLayout) que envuelve las páginas privadas (rutas hijas),
 *        protegido por authGuard; cada ruta añade su guard de permiso (RBAC).
 */
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/main-layout.component').then((m) => m.MainLayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/admin/dashboard/metrics-dashboard.component').then(
            (m) => m.MetricsDashboardComponent,
          ),
      },
      {
        path: 'map',
        canActivate: [requirePermission('dashboard:read')],
        loadComponent: () =>
          import('./features/realtime-map/realtime-map.component').then((m) => m.RealtimeMapComponent),
      },
      {
        path: 'incidents',
        canActivate: [requirePermission('incident:approve')],
        loadComponent: () => import('./features/incidents/incidents.component').then((m) => m.IncidentsComponent),
      },
      {
        path: 'reports',
        canActivate: [requirePermission('report:export')],
        loadComponent: () => import('./features/reports/reports.component').then((m) => m.ReportsComponent),
      },
      {
        path: 'audit',
        canActivate: [requirePermission('audit:read')],
        loadComponent: () => import('./features/audit/audit.component').then((m) => m.AuditComponent),
      },
      {
        path: 'notifications',
        loadComponent: () =>
          import('./features/notifications/notifications.component').then((m) => m.NotificationsComponent),
      },
      {
        path: 'companies',
        canActivate: [requirePermission('company:manage')],
        loadComponent: () =>
          import('./features/admin/companies/companies.component').then((m) => m.CompaniesComponent),
      },
      {
        path: 'users',
        canActivate: [requirePermission('user:manage')],
        loadComponent: () => import('./features/admin/users/users.component').then((m) => m.UsersComponent),
      },
      {
        path: 'roles',
        canActivate: [requirePermission('role:manage')],
        loadComponent: () => import('./features/admin/roles/roles.component').then((m) => m.RolesComponent),
      },
      {
        path: 'work-sites',
        canActivate: [requirePermission('worksite:manage')],
        loadComponent: () =>
          import('./features/admin/work-sites/work-sites.component').then((m) => m.WorkSitesComponent),
      },
      {
        path: 'work-sites/:id/geofence',
        canActivate: [requirePermission('geofence:manage')],
        loadComponent: () =>
          import('./features/admin/work-sites/geofence/geofence.component').then((m) => m.GeofenceComponent),
      },
      {
        path: 'projects',
        canActivate: [requirePermission('project:manage')],
        loadComponent: () => import('./features/admin/projects/projects.component').then((m) => m.ProjectsComponent),
      },
      {
        path: 'scheduling',
        canActivate: [requirePermission('schedule:manage')],
        loadComponent: () =>
          import('./features/admin/scheduling/scheduling.component').then((m) => m.SchedulingComponent),
      },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: '' },
];
