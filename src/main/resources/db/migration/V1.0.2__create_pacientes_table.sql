-- V1.0.2: Create pacientes table
CREATE TABLE pacientes (
    id                    UUID            PRIMARY KEY,
    nombre_completo       VARCHAR(100)    NOT NULL,
    documento_identidad   VARCHAR(20)     NOT NULL,
    telefono              VARCHAR(20)     NOT NULL,
    email                 VARCHAR(255)    NOT NULL,
    birth_date            DATE,
    created_at            TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_pacientes_documento_identidad UNIQUE (documento_identidad)
);

COMMENT ON TABLE pacientes IS 'Registro de pacientes de la clinica MediSalud';
COMMENT ON COLUMN pacientes.id IS 'Identificador unico UUID v4';
COMMENT ON COLUMN pacientes.nombre_completo IS 'Nombre completo del paciente (3-100 caracteres)';
COMMENT ON COLUMN pacientes.documento_identidad IS 'Documento de identidad (unico, minimo 7 caracteres)';
COMMENT ON COLUMN pacientes.telefono IS 'Telefono de contacto (minimo 7 digitos)';
COMMENT ON COLUMN pacientes.email IS 'Correo electronico (formato valido)';
COMMENT ON COLUMN pacientes.birth_date IS 'Fecha de nacimiento (ISO 8601, no futura)';
