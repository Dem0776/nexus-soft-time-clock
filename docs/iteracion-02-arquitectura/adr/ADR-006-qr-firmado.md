# ADR-006 — QR de centro firmado con nonce y vigencia

**Estado:** Aceptado · **Fecha:** 2026-07-21 · **Confirmada por el usuario** (default D-04)

## Contexto
El QR identifica el centro para el registro (RF-14). Un QR estático es fácilmente fotografiable/reutilizable (fraude, RN-25, RN-26).

## Decisión
El QR **no** contiene un valor estático adivinable, sino un **token firmado** (HMAC-SHA256 o JWS) que incluye: `tenant_id`, `site_id`, `nonce` (aleatorio), `issued_at` y `expires_at`. El backend valida firma y vigencia y **consume el nonce** (Redis) para prevenir replay. El QR se **rota de forma programada** (Scheduler) según política del centro. Modelo elegido frente a "QR dinámico en pantalla del sitio" por no requerir hardware adicional; el diseño no lo impide a futuro.

## Consecuencias
- ➕ Resistente a copia/reutilización dentro de la ventana; replay detectable.
- ➕ No requiere pantalla/hardware en el sitio.
- ➖ Un QR impreso es válido durante su ventana de vigencia → se mitiga con vigencia corta + geocerca + antifraude + (opcional) foto/biometría. La ubicación física sigue siendo la barrera principal.
- ➖ Requiere gestión de rotación y sincronización de reloj.
