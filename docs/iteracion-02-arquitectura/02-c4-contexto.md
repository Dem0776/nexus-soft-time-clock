# 02 — C4 Nivel 1: Diagrama de Contexto

Muestra Nexus Soft Time Clock como caja negra, sus usuarios y los sistemas externos con los que interactúa.

```mermaid
C4Context
    title Nexus Soft Time Clock — Diagrama de Contexto (C4 L1)

    Person(employee, "Colaborador", "Registra su asistencia en campo con la app móvil")
    Person(supervisor, "Supervisor", "Monitorea equipos, incidencias y mapa en tiempo real")
    Person(admin, "Administrador / RR.HH.", "Gestiona empresa, usuarios, centros, horarios, reportes")
    Person(superadmin, "Super Administrador", "Gestiona empresas (tenants) de la plataforma")

    System(nexus, "Nexus Soft Time Clock", "Plataforma multi-tenant de control de asistencia por QR + GPS + geocercas + antifraude")

    System_Ext(fcm, "Firebase Cloud Messaging", "Notificaciones push + Crashlytics + Analytics")
    System_Ext(mail, "Servidor de correo (SMTP)", "Notificaciones y correos transaccionales")
    System_Ext(maps, "Proveedor de mapas / tiles", "Teselas para mapa (OSM/Leaflet)")
    System_Ext(oauth, "Proveedor OAuth2 (futuro)", "SSO corporativo — preparado, no habilitado")

    Rel(employee, nexus, "Registra asistencia (offline-first)", "HTTPS / REST")
    Rel(supervisor, nexus, "Supervisa y resuelve incidencias", "HTTPS / PWA")
    Rel(admin, nexus, "Administra y exporta reportes", "HTTPS / PWA")
    Rel(superadmin, nexus, "Gestiona tenants", "HTTPS / PWA")

    Rel(nexus, fcm, "Envía push / reporta crashes", "HTTPS")
    Rel(nexus, mail, "Envía correos", "SMTP/TLS")
    Rel(supervisor, maps, "Carga teselas del mapa", "HTTPS")
    Rel(nexus, oauth, "Delegará autenticación (futuro)", "OIDC")
```

## Contexto narrativo

- **Colaborador**: usa la app Flutter, mayoritariamente en campo y a veces **sin conexión**; su interacción crítica es registrar asistencia validada.
- **Supervisor / Administrador / RR.HH. / Super Admin**: usan el portal Angular (PWA) para gestión, monitoreo y reportería.
- **Sistemas externos**: FCM (push/crashlytics/analytics), SMTP (correo), proveedor de teselas de mapa, y un proveedor **OAuth2/OIDC preparado** para SSO futuro (fuera de alcance inicial, RF/RNF preparados).

El sistema es la **única fuente de verdad** de la asistencia: valida con su propia hora y reglas antifraude, sin confiar en el dispositivo.
