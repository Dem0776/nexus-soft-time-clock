# Iteración 1 — Análisis funcional y refinamiento de requisitos

**Objetivo de la iteración:** transformar la especificación de `promt_001.txt` en un análisis funcional accionable y sin ambigüedades, que sirva como contrato para el diseño de arquitectura, dominio y base de datos de las siguientes iteraciones.

**Entregables de esta iteración**

| Doc | Contenido |
|---|---|
| [01 — Objetivos y alcance](01-objetivos-y-alcance.md) | Visión, alcance in/out, requisitos funcionales (RF) y no funcionales (RNF) catalogados |
| [02 — Actores y roles (RBAC)](02-actores-y-roles.md) | Actores del sistema, roles, permisos y matriz de autorización |
| [03 — Historias de usuario](03-historias-de-usuario.md) | Épicas e historias (HU) con criterios de aceptación |
| [04 — Reglas de negocio](04-reglas-de-negocio.md) | Reglas (RN) de asistencia, antifraude, offline, seguridad |
| [05 — Casos de uso](05-casos-de-uso.md) | Casos de uso principales (CU) con flujos y excepciones |
| [06 — Requisitos no funcionales](06-requisitos-no-funcionales.md) | Rendimiento, seguridad, disponibilidad, escalabilidad |
| [07 — Glosario / Lenguaje ubicuo](07-glosario-lenguaje-ubicuo.md) | Términos del dominio (DDD) |
| [08 — Bounded Contexts](08-bounded-contexts.md) | Contextos delimitados y su mapa (base para arquitectura) |
| [09 — Matriz de trazabilidad](09-matriz-trazabilidad.md) | RF ↔ HU ↔ RN ↔ CU |
| [10 — Supuestos y decisiones abiertas](10-supuestos-y-decisiones-abiertas.md) | Assumptions y preguntas pendientes de confirmar |

**Criterios de aceptación de la iteración**

- [ ] Todo requisito de `promt_001.txt` está catalogado con un código (RF/RNF) y trazado.
- [ ] Cada funcionalidad tiene al menos una historia de usuario con criterios de aceptación.
- [ ] Las reglas de asistencia y antifraude están expresadas de forma verificable.
- [ ] El lenguaje ubicuo y los bounded contexts están definidos (insumo para la Iteración 2).
- [ ] Las decisiones abiertas están explícitas para su confirmación.

> Convención de códigos: `RF-##` requisito funcional · `RNF-##` no funcional · `HU-##` historia de usuario · `RN-##` regla de negocio · `CU-##` caso de uso · `BC-##` bounded context.
