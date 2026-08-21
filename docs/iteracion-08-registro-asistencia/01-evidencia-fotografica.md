# RF-18 / HU-13 — Evidencia fotográfica opcional

**Objetivo:** que un centro pueda exigir foto al registrar asistencia, con la imagen almacenada
cifrada en MinIO y asociada al registro, sin que el binario atraviese el backend (ADR-008).

## Criterios de aceptación

| CA | Qué exige | Dónde se cumple |
|---|---|---|
| CA1 | Si la política del centro exige foto, el registro no se completa sin ella | `RegisterAttendanceService` (bloque 3.6) → `PHOTO_REQUIRED` |
| CA2 | La foto se almacena cifrada/segura (MinIO) y se asocia al registro | Subida prefirmada a MinIO con SSE-S3 + `attendance_records.evidence_*` |

## Flujo

1. La app consulta `GET /api/v1/attendance/site-policy/{workSiteId}` y sabe si el centro exige foto
   (la respuesta ya trae resuelta la herencia empresa → centro). La cachea en Drift para el modo offline.
2. Si hace falta foto, la captura, la comprime (lado mayor 1280 px, calidad 70) y la guarda en el
   directorio de documentos junto a la operación pendiente.
3. Al sincronizar, pide `POST /api/v1/attendance/evidence/uploads`. **El servidor decide la clave**:
   `t/{tenant}/s/{centro}/d/{yyyy}/{MM}/{dd}/u/{usuario}/{uuid}.jpg`.
4. La app sube el archivo con un `PUT` a la URL prefirmada, **directo a MinIO**.
5. Registra la asistencia enviando la clave. El servidor la **verifica** contra el almacenamiento
   antes de aceptarla (ver más abajo) y persiste `evidence_bucket/key/hash`.
6. El portal muestra la foto pidiendo `GET /api/v1/attendance/evidence/{recordId}/url`, que devuelve
   una URL de lectura de vida corta.

## Por qué se verifica la clave contra el almacenamiento

La clave la aporta el cliente al registrar, así que aceptarla tal cual haría la política de foto
puramente declarativa: bastaría con enviar `evidenceKey: "x"` para darla por cumplida. Antes de
asociarla, `EvidenceStoragePort.validate` comprueba en orden:

1. **Prefijo**: la clave cuelga de `t/{tenant}/…/u/{usuario}/` — una clave ajena se rechaza sin tocar la red.
2. **Existencia** (`statObject`): el objeto se subió de verdad. Una URL emitida no prueba que el PUT ocurriera.
3. **Tamaño** y **content-type** reales del objeto, no los declarados.
4. **Frescura**: subido dentro de la ventana configurada; corta la reutilización de fotos antiguas.

Resultados posibles y comportamiento:

| Situación | Foto obligatoria | Foto opcional |
|---|---|---|
| Evidencia válida | Aceptado, evidencia asociada | Aceptado, evidencia asociada |
| Clave inventada o ajena | `PHOTO_REQUIRED` | Aceptado **sin** evidencia + bandera `EVIDENCE_REJECTED_*` |
| Almacenamiento caído | `PHOTO_REQUIRED` (falla cerrado) | Aceptado sin evidencia |

> **Operación:** con foto obligatoria, MinIO caído **bloquea los fichajes** de ese centro. Es
> deliberado: aceptar un registro con una foto que no existe vaciaría de sentido la exigencia.
> Vigilar el healthcheck de MinIO.

La evidencia verificada se persiste **aunque el registro se rechace por otro motivo** (fuera de
geocerca, por ejemplo): es prueba de un intento real y hace exacta la regla de huérfanos —un objeto
sin fila en `attendance_records` es un huérfano.

## Herencia de la política

`work_sites.require_photo` es **tri-estado**: `NULL` hereda de `company_settings.require_photo`;
`TRUE`/`FALSE` lo sobrescriben. La resuelve `WorkSitePolicyAdapter`. En el portal, la ficha del
centro ofrece **Heredar / Sí / No**, y la política de empresa se edita en Configuración →
Políticas de registro (`GET`/`PUT /api/v1/company/settings`, permiso `company:settings`).

## Infraestructura

- Bucket `evidence`, **privado**, con SSE-S3 y expiración a 400 días; lo prepara
  `StorageBucketInitializer` al arrancar (idempotente).
- NGINX publica MinIO en `/evidence/`. El prefijo coincide con el nombre del bucket **a propósito**,
  para que no haga falta ningún rewrite y el path canónico de la firma SigV4 cuadre.
- `STORAGE_MINIO_PUBLIC_ENDPOINT` debe ser el origen que ve el cliente: SigV4 firma el `Host`, así
  que firmar contra el endpoint interno produce `SignatureDoesNotMatch` en toda subida.
- `client_max_body_size 3m` en esa ruta; el default de 1 MB rechazaría las fotos con 413.

## Verificación ejecutada

Con el stack local (PostgreSQL + MinIO + backend + NGINX de paso):

| Caso | Resultado |
|---|---|
| Bucket creado con SSE-S3 y retención | `mc encrypt info` → `sse-s3 is enabled` |
| Política de empresa `requirePhoto=true`, centro en «Heredar» | política efectiva del centro: `requirePhoto: true` |
| Presign + `PUT` directo a MinIO | HTTP 200 |
| Registro con la clave emitida | `ACCEPTED`, `evidence_bucket='evidence'` |
| Registro con `evidenceKey:"x"` | `REJECTED / PHOTO_REQUIRED` |
| Registro con clave de otro tenant | `REJECTED / PHOTO_REQUIRED` |
| Bucket declarado por el cliente | ignorado; se persiste el del servidor |
| Subida y lectura **a través de NGINX** | HTTP 200; imagen idéntica byte a byte |
| Foto opcional con clave inválida | `ACCEPTED` + `EVIDENCE_REJECTED_FOREIGN_PREFIX` |
