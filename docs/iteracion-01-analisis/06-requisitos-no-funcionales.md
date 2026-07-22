# 06 — Requisitos no funcionales (RNF)

| Código | Categoría | Requisito | Meta / criterio verificable |
|---|---|---|---|
| **RNF-01** | Rendimiento | Latencia de registro de asistencia | p95 < 400 ms (excluyendo subida de foto) bajo carga nominal |
| **RNF-02** | Escalabilidad | Concurrencia | Soportar miles de usuarios concurrentes; escalado horizontal (stateless + Redis) |
| **RNF-03** | Escalabilidad | Volumen de datos | Millones de registros; particionamiento de tablas de asistencia/auditoría por fecha/tenant |
| **RNF-04** | Disponibilidad | Uptime objetivo | ≥ 99.5% en fase inicial; health checks (Actuator) y readiness/liveness para K8s |
| **RNF-05** | Seguridad | Transporte | HTTPS/TLS obligatorio extremo a extremo (RN-42) |
| **RNF-06** | Seguridad | Autenticación | JWT + refresh rotatorio, RBAC, device binding (RF-28) |
| **RNF-07** | Seguridad | Protecciones | Rate limiting, CSRF (donde aplique), XSS, SQL injection (queries parametrizadas/JPA), replay |
| **RNF-08** | Seguridad | Datos en reposo | Cifrado de datos sensibles y evidencias (MinIO + cifrado) |
| **RNF-09** | Auditoría | Trazabilidad | 100% de acciones de escritura auditadas e inmutables (RN-60, RN-61) |
| **RNF-10** | Offline | Resiliencia | Cero pérdida de registros sin red; sincronización idempotente (RN-50, RN-51) |
| **RNF-11** | Observabilidad | Métricas | Métricas Prometheus + dashboards Grafana; trazas y logs estructurados |
| **RNF-12** | Mantenibilidad | Arquitectura | Modular monolith + hexagonal + DDD; acoplamiento bajo entre bounded contexts |
| **RNF-13** | Calidad | Cobertura de pruebas | ≥ 80% en backend, web y móvil |
| **RNF-14** | Portabilidad | Despliegue | Docker + Docker Compose (local) y **Kubernetes-ready** (prod) |
| **RNF-15** | Internacionalización | i18n | Móvil y web con i18n; base para múltiples idiomas y zonas horarias por tenant |
| **RNF-16** | Accesibilidad | A11y | App y portal accesibles (contraste, lectores de pantalla, tamaños táctiles) |
| **RNF-17** | Usabilidad | Temas | Modo claro/oscuro en móvil y web (Material 3 / Angular Material) |
| **RNF-18** | Compatibilidad | Dispositivos | Teléfonos y tablets (Flutter responsive); navegadores modernos (Angular PWA responsive) |
| **RNF-19** | Localización temporal | Zonas horarias | Cálculos de turno/horario con zona horaria del centro/tenant; almacenamiento en UTC |
| **RNF-20** | Evolutividad | Microservicios | Bounded contexts desacoplados vía eventos, extraíbles a microservicios sin reescritura |
| **RNF-21** | Preparación IA | Spring AI | Arquitectura con punto de extensión para futuras funcionalidades inteligentes (sin implementación en fase inicial) |
| **RNF-22** | Cumplimiento | Datos personales | Manejo de datos personales y biométricos conforme a privacidad; consentimiento y minimización |

> Los umbrales concretos (precisión GPS, tolerancias, TTL de tokens, N intentos) son **parametrizables por tenant/centro**; los valores por defecto se fijarán en la Iteración 3 (dominio/BD) y se validan en pruebas.
