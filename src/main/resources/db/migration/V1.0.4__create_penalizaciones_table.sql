-- V1.0.4: Create penalizaciones table
CREATE TABLE penalizaciones (
    id              UUID            PRIMARY KEY,
    paciente_id     UUID            NOT NULL,
    cita_id         UUID            NOT NULL,
    fecha_hora      TIMESTAMPTZ     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_penalizaciones_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id),
    CONSTRAINT fk_penalizaciones_cita     FOREIGN KEY (cita_id)     REFERENCES citas (id)
);

-- Index for RN-05: count penalties in 30-day window
CREATE INDEX idx_penalizaciones_paciente_fecha ON penalizaciones (paciente_id, fecha_hora);

COMMENT ON TABLE penalizaciones IS 'Registro de penalizaciones por cancelacion tardia';
COMMENT ON COLUMN penalizaciones.id IS 'Identificador unico UUID v4';
COMMENT ON COLUMN penalizaciones.paciente_id IS 'Referencia al paciente penalizado';
COMMENT ON COLUMN penalizaciones.cita_id IS 'Referencia a la cita cancelada tardiamente';
COMMENT ON COLUMN penalizaciones.fecha_hora IS 'Momento en que se registro la penalizacion';
