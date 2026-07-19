-- V1.0.7: Seed initial medicos data
INSERT INTO medicos (id, nombre_completo, especialidad, telefono, email)
VALUES
    (gen_random_uuid(), 'Dra. Maria Gonzalez', 'Cardiologia', '5551001', 'maria.gonzalez@medisalud.com'),
    (gen_random_uuid(), 'Dr. Carlos Ruiz', 'Pediatria', '5551002', 'carlos.ruiz@medisalud.com'),
    (gen_random_uuid(), 'Dra. Ana Lopez', 'Dermatologia', '5551003', 'ana.lopez@medisalud.com');
