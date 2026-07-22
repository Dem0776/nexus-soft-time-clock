-- =====================================================================
-- V5 — Geofencing (BC-05): geocercas y QR de centro firmado
-- =====================================================================

CREATE TABLE geofences (
    id                 uuid                     PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          uuid                     NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    work_site_id       uuid                     NOT NULL REFERENCES work_sites (id) ON DELETE CASCADE,
    type               varchar(10)              NOT NULL DEFAULT 'CIRCLE'
                       CHECK (type IN ('CIRCLE','POLYGON')),
    center             geography(Point, 4326),                 -- requerido si CIRCLE
    radius_m           numeric(8,2)             CHECK (radius_m IS NULL OR radius_m > 0),
    area               geography(Polygon, 4326),               -- requerido si POLYGON (futuro, S-02)
    is_active          boolean                  NOT NULL DEFAULT true,
    created_at         timestamptz              NOT NULL DEFAULT now(),
    updated_at         timestamptz              NOT NULL DEFAULT now(),
    CONSTRAINT ck_geofence_shape CHECK (
        (type = 'CIRCLE'  AND center IS NOT NULL AND radius_m IS NOT NULL) OR
        (type = 'POLYGON' AND area   IS NOT NULL)
    )
);

CREATE INDEX ix_geofences_tenant_site ON geofences (tenant_id, work_site_id);
CREATE INDEX gix_geofences_center     ON geofences USING gist (center) WHERE center IS NOT NULL;
CREATE INDEX gix_geofences_area       ON geofences USING gist (area)   WHERE area   IS NOT NULL;
-- una geocerca activa por centro
CREATE UNIQUE INDEX uq_geofences_active_per_site ON geofences (work_site_id) WHERE is_active;

CREATE TRIGGER trg_geofences_updated_at BEFORE UPDATE ON geofences
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

COMMENT ON TABLE geofences IS 'Geocerca circular (centro+radio) o poligonal por centro (RN-13).';

-- =====================================================================
-- site_qr_tokens — token de centro firmado con nonce y vigencia (ADR-006)
-- =====================================================================
CREATE TABLE site_qr_tokens (
    id             uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      uuid         NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    work_site_id   uuid         NOT NULL REFERENCES work_sites (id) ON DELETE CASCADE,
    nonce          varchar(64)  NOT NULL,             -- aleatorio por emisión
    key_id         varchar(40)  NOT NULL,             -- kid de la llave de firma (rotación de llaves)
    issued_at      timestamptz  NOT NULL DEFAULT now(),
    expires_at     timestamptz  NOT NULL,
    is_active      boolean      NOT NULL DEFAULT true,
    rotated_from   uuid         REFERENCES site_qr_tokens (id) ON DELETE SET NULL,
    created_at     timestamptz  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_qr_tokens_tenant_nonce ON site_qr_tokens (tenant_id, nonce);
CREATE INDEX        ix_qr_tokens_site_active   ON site_qr_tokens (work_site_id) WHERE is_active;
CREATE INDEX        ix_qr_tokens_expires       ON site_qr_tokens (expires_at);

COMMENT ON TABLE site_qr_tokens IS 'QR de centro firmado (nonce + vigencia); se rota programadamente (ADR-006).';

-- =====================================================================
-- qr_nonce_consumed — anti-replay de nonces ya usados (RN-26)
-- =====================================================================
CREATE TABLE qr_nonce_consumed (
    id            uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     uuid        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    work_site_id  uuid        NOT NULL REFERENCES work_sites (id) ON DELETE CASCADE,
    nonce         varchar(64) NOT NULL,
    consumed_at   timestamptz NOT NULL DEFAULT now(),
    attendance_id uuid                                    -- referencia lógica (tabla particionada)
);

CREATE UNIQUE INDEX uq_qr_nonce_consumed ON qr_nonce_consumed (tenant_id, nonce);

COMMENT ON TABLE qr_nonce_consumed IS 'Nonces de QR ya consumidos para prevenir reutilización/replay (RN-26). Redis es la primera barrera; esta tabla da durabilidad.';
