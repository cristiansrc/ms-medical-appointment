-- V1.0.1: Create medicos table
CREATE TABLE medicos (
    id              UUID            PRIMARY KEY,
    nombre_completo VARCHAR(100)    NOT NULL,
    especialidad    VARCHAR(100)    NOT NULL,
    telefono        VARCHAR(20),
    email           VARCHAR(255),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE medicos IS 'Registro de medicos de la clinica MediSalud';
COMMENT ON COLUMN medicos.id IS 'Identificador unico UUID v4';
COMMENT ON COLUMN medicos.nombre_completo IS 'Nombre completo del medico (3-100 caracteres)';
COMMENT ON COLUMN medicos.especialidad IS 'Especialidad medica';
COMMENT ON COLUMN medicos.telefono IS 'Telefono de contacto (minimo 7 caracteres si se provee)';
COMMENT ON COLUMN medicos.email IS 'Correo electronico (formato valido si se provee)';
