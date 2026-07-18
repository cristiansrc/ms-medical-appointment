-- V1.0.7: Seed initial medicos data
INSERT INTO medicos (id, nombre_completo, especialidad, telefono, email)
VALUES
    (gen_random_uuid(), 'Dra. Maria Gonzalez', 'Cardiologia', '555-1001', 'maria.gonzalez@medisalud.com'),
    (gen_random_uuid(), 'Dr. Carlos Ruiz', 'Pediatria', '555-1002', 'carlos.ruiz@medisalud.com'),
    (gen_random_uuid(), 'Dra. Ana Lopez', 'Dermatologia', '555-1003', 'ana.lopez@medisalud.com');
