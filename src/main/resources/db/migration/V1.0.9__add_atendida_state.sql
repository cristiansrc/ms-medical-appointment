-- V1.0.9: Agregar ATENDIDA al constraint CHECK de estado en citas
ALTER TABLE citas DROP CONSTRAINT IF EXISTS chk_citas_estado;
ALTER TABLE citas ADD CONSTRAINT chk_citas_estado CHECK (estado IN ('PROGRAMADA', 'CANCELADA', 'ATENDIDA'));

COMMENT ON COLUMN citas.estado IS 'Estado de la cita: PROGRAMADA, CANCELADA o ATENDIDA';
