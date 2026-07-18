-- V1.0.6: Create trigger function and triggers for updated_at auto-update

-- Function to update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for medicos
CREATE TRIGGER trg_medicos_updated_at
    BEFORE UPDATE ON medicos
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for pacientes
CREATE TRIGGER trg_pacientes_updated_at
    BEFORE UPDATE ON pacientes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for citas
CREATE TRIGGER trg_citas_updated_at
    BEFORE UPDATE ON citas
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for festivos
CREATE TRIGGER trg_festivos_updated_at
    BEFORE UPDATE ON festivos
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON FUNCTION update_updated_at_column() IS 'Trigger function para actualizar automaticamente updated_at en todas las tablas de negocio';
