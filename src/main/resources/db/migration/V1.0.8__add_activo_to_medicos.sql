-- V1.0.8: Add activo column to medicos table for soft delete support
ALTER TABLE medicos ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN medicos.activo IS 'Indica si el medico esta activo (soft delete)';
