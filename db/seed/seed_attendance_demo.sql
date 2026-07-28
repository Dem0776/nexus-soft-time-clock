-- =====================================================================
-- SIEMBRA DE DEMO — fichajes e incidencias (NO para producción)
-- Puebla attendance_records con ENTRADA/comida/SALIDA de la 1ª quincena de
-- agosto 2026 (días hábiles) para todos los colaboradores cuyo tenant tenga
-- un centro de trabajo, y agrega a Roberto 2 faltas y 2 retardos para el
-- ejemplo. Correr una sola vez (reejecutarlo duplica filas).
--
--   Get-Content .\db\seed\seed_attendance_demo.sql -Raw | `
--     docker compose -f infra/docker-compose.yml exec -T postgres psql -U nexus -d nexus
-- =====================================================================

-- 1) Fichajes (entrada, salida a comida, regreso, salida) por día hábil
WITH tgt AS (
    SELECT u.id AS user_id, u.tenant_id, u.email,
           ws.id AS work_site_id, ws.location AS loc
    FROM users u
    JOIN LATERAL (
        SELECT w.id, w.location
        FROM work_sites w
        WHERE w.tenant_id = u.tenant_id AND w.status = 'ACTIVE'
        ORDER BY w.created_at
        LIMIT 1
    ) ws ON true
    WHERE u.is_platform_admin = false
),
days AS (
    SELECT d::date AS d
    FROM generate_series(DATE '2026-08-03', DATE '2026-08-14', interval '1 day') d
    WHERE EXTRACT(dow FROM d) BETWEEN 1 AND 5      -- lunes a viernes
),
ev AS (
    SELECT 'ENTRADA'::varchar AS et, time '08:00' AS t
    UNION ALL SELECT 'INICIO_DESCANSO', time '13:00'
    UNION ALL SELECT 'FIN_DESCANSO',    time '14:00'
    UNION ALL SELECT 'SALIDA',          time '17:00'
)
INSERT INTO attendance_records
    (tenant_id, server_time, user_id, work_site_id, event_type, status,
     location, gps_accuracy_m, operation_uuid, source)
SELECT t.tenant_id,
       ((days.d + ev.t)::timestamptz
         + CASE WHEN ev.et = 'ENTRADA' AND random() < 0.25 THEN interval '18 minutes' ELSE interval '0' END),
       t.user_id, t.work_site_id, ev.et, 'ACCEPTED',
       t.loc, 8.0, gen_random_uuid(), 'ONLINE'
FROM tgt t
CROSS JOIN days
CROSS JOIN ev
-- Roberto "falta" el 6 y 7 de agosto (no ficha esos días).
WHERE NOT (t.email = 'roberto.juarez@i-condor.com' AND days.d IN (DATE '2026-08-06', DATE '2026-08-07'));

-- 2) Incidencias de Roberto: 2 retardos + 2 faltas injustificadas (para el reporte)
INSERT INTO incidents (tenant_id, user_id, type, status, priority, incident_date)
SELECT u.tenant_id, u.id, 'RETARDO', 'RESOLVED', 'LOW', v.d
FROM users u, (VALUES (DATE '2026-08-04'), (DATE '2026-08-11')) v(d)
WHERE u.email = 'roberto.juarez@i-condor.com';

INSERT INTO incidents (tenant_id, user_id, type, status, priority, incident_date)
SELECT u.tenant_id, u.id, 'FALTA', 'OPEN', 'MEDIUM', v.d
FROM users u, (VALUES (DATE '2026-08-06'), (DATE '2026-08-07')) v(d)
WHERE u.email = 'roberto.juarez@i-condor.com';
