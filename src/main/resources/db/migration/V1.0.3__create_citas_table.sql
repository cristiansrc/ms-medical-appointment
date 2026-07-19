-- V1.0.3: Create citas table
CREATE TABLE citas (
    id                    UUID            PRIMARY KEY,
    paciente_id           UUID            NOT NULL,
    medico_id             UUID            NOT NULL,
    fecha_hora            TIMESTAMPTZ     NOT NULL,
    estado                VARCHAR(20)     NOT NULL DEFAULT 'PROGRAMADA',
    motivo_cancelacion    VARCHAR(255),
    fecha_cancelacion     TIMESTAMPTZ,
    created_at            TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_citas_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id),
    CONSTRAINT fk_citas_medico   FOREIGN KEY (medico_id)   REFERENCES medicos (id),
    CONSTRAINT chk_citas_estado  CHECK (estado IN ('PROGRAMADA', 'CANCELADA', 'ATENDIDA'))
);

-- Index for RN-02: medico slot conflict lookup
CREATE INDEX idx_citas_medico_fecha ON citas (medico_id, fecha_hora);

-- Index for RN-04: paciente slot conflict lookup
CREATE INDEX idx_citas_paciente_fecha ON citas (paciente_id, fecha_hora);

-- Index for listing/filtering by estado
CREATE INDEX idx_citas_estado ON citas (estado);

-- Index for race condition prevention (Opción A): unique partial index on (medico_id, fecha_hora) where estado = 'PROGRAMADA'
-- Evita doble reserva en alta concurrencia: el primer INSERT en commit lo bloquea, el segundo falla con violación de unique
CREATE UNIQUE INDEX idx_unique_medico_franja_programada ON citas (medico_id, fecha_hora) WHERE estado = 'PROGRAMADA';

COMMENT ON TABLE citas IS 'Registro de citas medicas';
COMMENT ON COLUMN citas.id IS 'Identificador unico UUID v4';
COMMENT ON COLUMN citas.paciente_id IS 'Referencia al paciente';
COMMENT ON COLUMN citas.medico_id IS 'Referencia al medico';
COMMENT ON COLUMN citas.fecha_hora IS 'Fecha y hora de inicio de la franja (30 min)';
COMMENT ON COLUMN citas.estado IS 'Estado de la cita: PROGRAMADA, CANCELADA o ATENDIDA';
COMMENT ON COLUMN citas.motivo_cancelacion IS 'Motivo de cancelacion (solo si CANCELADA)';
COMMENT ON COLUMN citas.fecha_cancelacion IS 'Fecha/hora en que se cancelo (solo si CANCELADA)';
