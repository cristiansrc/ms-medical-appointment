-- V1.0.5: Create festivos table (cache for Nager.Date API)
CREATE TABLE festivos (
    id              UUID            PRIMARY KEY,
    date            DATE            NOT NULL,
    local_name      VARCHAR(255)    NOT NULL,
    name            VARCHAR(255),
    country_code    VARCHAR(10)     NOT NULL,
    fixed           BOOLEAN,
    global          BOOLEAN,
    types           TEXT,
    year            INT             NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_festivos_date_year_country UNIQUE (date, year, country_code)
);

-- Index for holiday lookup by year
CREATE INDEX idx_festivos_year ON festivos (year);

-- Index for holiday lookup by specific date
CREATE INDEX idx_festivos_date ON festivos (date);

COMMENT ON TABLE festivos IS 'Cache de dias festivos de Colombia obtenidos de Nager.Date API';
COMMENT ON COLUMN festivos.id IS 'Identificador unico UUID v4';
COMMENT ON COLUMN festivos.date IS 'Fecha del festivo';
COMMENT ON COLUMN festivos.local_name IS 'Nombre local del festivo';
COMMENT ON COLUMN festivos.name IS 'Nombre internacional del festivo';
COMMENT ON COLUMN festivos.country_code IS 'Codigo del pais (fijo: CO)';
COMMENT ON COLUMN festivos.fixed IS 'Indica si es fecha fija';
COMMENT ON COLUMN festivos.global IS 'Indica si aplica a todo el pais';
COMMENT ON COLUMN festivos.types IS 'Tipos de festivo separados por coma';
COMMENT ON COLUMN festivos.year IS 'Anio del festivo';
