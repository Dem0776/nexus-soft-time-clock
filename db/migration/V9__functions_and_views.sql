-- =====================================================================
-- V9 — Funciones, vistas y utilitarios
-- =====================================================================

-- --- Distancia geodésica en metros entre dos puntos -------------------
CREATE OR REPLACE FUNCTION fn_distance_m(a geography, b geography)
RETURNS numeric AS $$
    SELECT round(ST_Distance(a, b)::numeric, 2);
$$ LANGUAGE sql IMMUTABLE STRICT;

COMMENT ON FUNCTION fn_distance_m(geography, geography)
    IS 'Distancia geodésica en metros (ST_Distance). Usada para validar radio de geocerca (RN-13).';

-- --- ¿Un punto está dentro de la geocerca de un centro? --------------
CREATE OR REPLACE FUNCTION fn_within_geofence(p_geofence_id uuid, p_point geography)
RETURNS boolean AS $$
DECLARE
    g geofences%ROWTYPE;
BEGIN
    SELECT * INTO g FROM geofences WHERE id = p_geofence_id AND is_active;
    IF NOT FOUND THEN
        RETURN false;
    END IF;
    IF g.type = 'CIRCLE' THEN
        RETURN ST_DWithin(p_point, g.center, g.radius_m::double precision);  -- true si dentro del radio
    ELSE
        RETURN ST_Contains(g.area::geometry, p_point::geometry);
    END IF;
END;
$$ LANGUAGE plpgsql STABLE;

COMMENT ON FUNCTION fn_within_geofence(uuid, geography)
    IS 'Verifica pertenencia de un punto a la geocerca activa (círculo o polígono) — RN-13.';

-- --- Creación idempotente de partición mensual -----------------------
-- Usada por un job (Spring Scheduler) para provisionar particiones futuras.
CREATE OR REPLACE FUNCTION fn_create_monthly_partition(p_parent text, p_month date)
RETURNS void AS $$
DECLARE
    v_start date := date_trunc('month', p_month)::date;
    v_end   date := (date_trunc('month', p_month) + interval '1 month')::date;
    v_child text := format('%s_%s', p_parent, to_char(v_start, 'YYYY_MM'));
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = v_child) THEN
        EXECUTE format(
            'CREATE TABLE %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L);',
            v_child, p_parent, v_start, v_end
        );
    END IF;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_create_monthly_partition(text, date)
    IS 'Crea (si no existe) la partición mensual de una tabla particionada por rango de fecha.';

-- =====================================================================
-- Vistas de lectura (dashboards / reportes). El caller filtra por tenant_id.
-- =====================================================================

-- Asistencias del día (aceptadas)
CREATE OR REPLACE VIEW v_attendance_today AS
SELECT ar.tenant_id, ar.user_id, u.first_name, u.last_name,
       ar.work_site_id, ws.name AS work_site_name,
       ar.event_type, ar.status, ar.server_time, ar.location
FROM attendance_records ar
JOIN users u       ON u.id = ar.user_id
JOIN work_sites ws ON ws.id = ar.work_site_id
WHERE ar.server_time >= date_trunc('day', now())
  AND ar.status = 'ACCEPTED';

COMMENT ON VIEW v_attendance_today IS 'Registros aceptados del día en curso (RF-24, mapa/dashboard).';

-- Horas trabajadas por usuario y mes
CREATE OR REPLACE VIEW v_worked_hours_by_user_month AS
SELECT tenant_id, user_id,
       date_trunc('month', work_date)::date AS month,
       sum(worked_minutes)   AS worked_minutes,
       sum(overtime_minutes) AS overtime_minutes,
       sum(late_minutes)     AS late_minutes
FROM work_days
GROUP BY tenant_id, user_id, date_trunc('month', work_date);

COMMENT ON VIEW v_worked_hours_by_user_month IS 'Agregado mensual de horas trabajadas/extra/retardos (RF-24, RF-11).';

-- Incidencias abiertas
CREATE OR REPLACE VIEW v_open_incidents AS
SELECT tenant_id, id, user_id, type, priority, incident_date, created_at
FROM incidents
WHERE status = 'OPEN';

COMMENT ON VIEW v_open_incidents IS 'Incidencias pendientes de resolución (RF-09).';
