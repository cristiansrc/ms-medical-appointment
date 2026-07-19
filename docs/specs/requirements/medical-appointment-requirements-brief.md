# Requirements Brief — Sistema de Agendamiento de Citas Médicas (MediSalud)

> **Status:** `ready-for-planner`
> **Incremento:** medical-appointment
> **Fecha de creación:** 2026-07-17
> **Última actualización:** 2026-07-18
> **Autor:** requirements-analyst

---

## 1. Objetivo

Digitalizar el proceso de agendamiento de citas médicas de la clínica MediSalud, reemplazando el sistema telefónico actual por una API REST que permita:

- Registrar médicos y pacientes en el sistema.
- Consultar médicos y pacientes registrados.
- Actualizar datos de médicos y pacientes.
- Consultar disponibilidad de citas en franjas de 30 minutos.
- Reservar citas validando reglas de negocio (sin duplicidad, sin conflictos).
- Cancelar citas con registro de penalizaciones por cancelación tardía.
- Reprogramar citas existentes.
- Listar citas con filtros.
- Consultar citas individuales.

El sistema debe garantizar integridad en la reserva (un médico y un paciente no pueden tener dos citas simultáneas), controlar la disponibilidad por franja horaria, aplicar penalizaciones para reducir el ausentismo y gestionar los días festivos de Colombia mediante la API de Nager.Date.

---

## 2. Contexto

MediSalud es una clínica que actualmente gestiona sus citas médicas por teléfono. Este proceso manual genera:

- **Conflictos de horarios:** dos pacientes pueden ser agendados con el mismo médico en la misma franja.
- **Duplicidad de citas:** un paciente puede tener dos citas en el mismo horario con diferentes médicos.
- **Falta de control de cancelaciones:** no hay registro ni penalización por cancelaciones tardías.
- **Ausentismo:** sin penalizaciones, los pacientes no tienen incentivo para cancelar a tiempo.

La dirección ha decidido construir un **backend (API REST)** que resuelva estos problemas. **No se requiere frontend** en este incremento.

### Stack tecnológico decidido (contexto para Planner, no para diseño funcional)

Java 21, Spring Boot 3.x, Gradle, PostgreSQL, JPA/Hibernate (validate), UUID, Arquitectura Hexagonal, API Design First (OpenAPI), Flyway, JUnit 5, Mockito, ArchUnit, JaCoCo, H2 (integración), `@RestControllerAdvice`.

---

## 3. Actores y Permisos

| Actor | Descripción | Acciones permitidas |
|-------|-------------|---------------------|
| **Administrador** (implícito) | Personal de la clínica que gestiona la operación | Registrar médicos, listar médicos, consultar médicos individuales, actualizar médicos, listar pacientes, consultar pacientes individuales, actualizar pacientes, listar citas, consultar citas individuales, posiblemente cancelar citas en nombre de otros |
| **Paciente** | Persona que solicita una cita médica | Registrarse, consultar disponibilidad, reservar citas, cancelar sus propias citas, reprogramar sus citas, listar sus citas, consultar sus citas individuales |
| **Sistema** | Motor de reglas de negocio | Validar franjas horarias, verificar duplicidad, aplicar penalizaciones, bloquear agendamiento por exceso de penalizaciones, consultar festivos de Colombia |

> **Nota:** El enunciado no define explícitamente un rol "Administrador" ni un mecanismo de autenticación/autorización. Se asume que las operaciones de gestión de médicos son administrativas. La autenticación/autorización queda como mejora propuesta (ver sección 15).

---

## 4. Alcance (Scope)

### 4.1 Registro de Médicos (RF-01)

- Crear un médico con: ID (UUID), nombre completo (obligatorio, 3-100 caracteres), especialidad (obligatorio), teléfono (opcional, solo dígitos, mínimo 7 caracteres), email (opcional, formato válido).
- El ID es generado por el sistema.
- El teléfono se almacena solo con dígitos (se eliminan automáticamente guiones, espacios y cualquier carácter no numérico). Debe tener al menos 7 dígitos.

### 4.2 Registro de Pacientes (RF-02)

- Crear un paciente con: ID (UUID), nombre completo (obligatorio, 3-100 caracteres), documento de identidad (obligatorio, único, mínimo 7 caracteres), teléfono (obligatorio, solo dígitos, mínimo 7 caracteres), email (obligatorio, formato válido), **fecha de nacimiento** (opcional, formato ISO 8601 date — `YYYY-MM-DD`).
- El documento de identidad debe ser único en todo el sistema.
- La fecha de nacimiento cumple RN-03: no puede ser futura; si no se provee, se asume edad 0.

### 4.3 Consulta y Actualización de Médicos (RF-01b)

- Listar todos los médicos registrados.
- Consultar un médico individual por su ID.
- Actualizar los datos de un médico existente por su ID.

### 4.4 Consulta y Actualización de Pacientes (RF-02b)

- Listar todos los pacientes registrados.
- Consultar un paciente individual por su ID.
- Actualizar los datos de un paciente existente por su ID.

### 4.5 Reserva de Citas (RF-03)

- Crear una cita con: ID (UUID), referencia al paciente, referencia al médico, fecha y hora (ISO 8601), estado inicial = `PROGRAMADA`.
- Validar reglas de negocio RN-01 (franjas válidas), RN-02 (sin duplicidad de médico), RN-04 (sin conflicto de paciente), RN-05 (penalizaciones).
- Si `pacienteId` o `medicoId` no existen, retornar **404 Not Found** con mensaje claro.

### 4.6 Consulta de Disponibilidad (RF-04)

- Consultar franjas horarias disponibles dado: `medicoId`, `fechaInicio`, `fechaFin`.
- Cada franja = 30 minutos.
- Las franjas ocupadas no aparecen en el resultado.
- Solo se muestran franjas dentro del horario de atención (RN-01).
- Se excluyen domingos y festivos de Colombia (obtenidos de la API de Nager.Date o del caché en BD).

### 4.7 Cancelación de Citas (RF-05)

- Cancelar una cita por su ID.
- Cambiar estado a `CANCELADA`.
- Registrar fecha/hora de cancelación.
- Aplicar penalización si la cancelación es con **2 horas o menos** (`<= 2h`) de antelación (RN-05).

### 4.8 Consulta de Cita Individual (RF-05b)

- Consultar una cita individual por su ID.

### 4.9 Listado de Citas (RF-06)

- Listar citas con filtros opcionales: `medicoId`, `pacienteId`, estado (`PROGRAMADA`, `CANCELADA`), rango de fechas.

### 4.10 Reglas de Negocio

| Regla | Descripción |
|-------|-------------|
| **RN-01** Franjas horarias | Lun-Vie: 08:00-18:00, Sáb: 08:00-13:00, Dom/festivos: sin atención. Franjas de 30 min. Los festivos se obtienen de la API de Nager.Date (caché en BD). |
| **RN-02** No duplicidad (médico) | Un médico no puede tener dos citas en la misma franja de 30 min. |
| **RN-03** Antigüedad mínima | No se aceptan fechas de nacimiento futuras. Si no se proporciona, edad = 0. |
| **RN-04** Conflicto de paciente | Un paciente no puede tener dos citas en la misma franja (independiente del médico). |
| **RN-05** Penalización | Cancelar con `<= 2h` de antelación = penalización. 3+ penalizaciones en 30 días = bloqueo de agendamiento. |
| **RN-06** Reprogramación | Cancelar cita anterior (aplicando RN-05) + crear nueva cita validando RN-02 y RN-04. |

### 4.11 Datos Iniciales (Seed)

Tres médicos para carga inicial:

1. Dra. María González — Cardiología — 555-1001 — maria.gonzalez@medisalud.com
2. Dr. Carlos Ruiz — Pediatría — 555-1002 — carlos.ruiz@medisalud.com
3. Dra. Ana López — Dermatología — 555-1003 — ana.lopez@medisalud.com

---

## 5. No Objetivos (Out of Scope)

| Elemento | Razón |
|----------|-------|
| Frontend / UI | El enunciado indica explícitamente que es solo backend (API REST). |
| Autenticación / Autorización (JWT, OAuth2, Keycloak) | No se menciona en el enunciado. Queda como mejora propuesta (ver sección 15). |
| Notificaciones (email, SMS) | No se solicitan. Queda como mejora propuesta. |
| Pagos / Facturación | Fuera del alcance de la prueba. |
| Eliminación de médicos o pacientes | No se solicita; solo registro, consulta, actualización y operaciones sobre citas. |
| Auditoría / Logging de eventos | No se solicita explícitamente. Queda como mejora propuesta. |
| Reportes o dashboards | Fuera del alcance. |
| Concurrencia distribuida / locking optimista | No se menciona; es una API single-instance en contexto de prueba. |
| Internacionalización (i18n) | No se solicita. Queda como mejora propuesta. |
| Estado ATENDIDA para citas | No existe transición definida en el enunciado. Queda como mejora propuesta (ver sección 15). |

---

## 6. Flujos de Usuario

### 6.1 Flujo Principal: Reserva de Cita Exitosa

1. El paciente (o administrador) consulta disponibilidad para un médico en un rango de fechas.
2. El sistema devuelve las franjas horarias disponibles (30 min, dentro del horario de atención, excluyendo ocupadas, domingos y festivos).
3. El paciente selecciona una franja y solicita la reserva.
4. El sistema valida:
   - La franja está dentro del horario de atención (RN-01).
   - El médico no tiene otra cita en esa franja (RN-02).
   - El paciente no tiene otra cita en esa franja (RN-04).
   - El paciente no tiene 3+ penalizaciones en los últimos 30 días (RN-05).
   - El `pacienteId` y `medicoId` existen (si no, 404 Not Found).
5. Si todas las validaciones pasan, se crea la cita con estado `PROGRAMADA`.

### 6.2 Flujo Alterno: Reserva Rechazada por Duplicidad de Médico

1. Pasos 1-3 iguales al flujo principal.
2. El sistema detecta que el médico ya tiene una cita en la franja solicitada (RN-02).
3. Se rechaza la solicitud con mensaje de error claro indicando que el horario no está disponible.

### 6.3 Flujo Alterno: Reserva Rechazada por Conflicto de Paciente

1. Pasos 1-3 iguales al flujo principal.
2. El sistema detecta que el paciente ya tiene una cita en la franja solicitada (RN-04), incluso con otro médico.
3. Se rechaza la solicitud con mensaje de error claro indicando que el paciente ya tiene una cita en ese horario.

### 6.4 Flujo Alterno: Reserva Rechazada por Penalizaciones

1. Pasos 1-3 iguales al flujo principal.
2. El sistema detecta que el paciente tiene 3 o más penalizaciones en los últimos 30 días (RN-05).
3. Se rechaza la solicitud indicando que el paciente no puede agendar por exceso de cancelaciones tardías.

### 6.5 Flujo Alterno: Reserva Rechazada por Paciente o Médico Inexistente

1. Pasos 1-3 iguales al flujo principal.
2. El sistema detecta que el `pacienteId` o `medicoId` proporcionado no existe.
3. Se rechaza la solicitud con **404 Not Found** y mensaje claro indicando cuál ID no fue encontrado.

### 6.6 Flujo: Cancelación de Cita

1. Se solicita la cancelación de una cita por su ID.
2. El sistema verifica que la cita existe y está en estado `PROGRAMADA`.
3. Se calcula el tiempo entre la solicitud de cancelación y la hora de la cita.
4. Si faltan **2 horas o menos** (`<= 2h`), se registra una penalización para el paciente (RN-05).
5. Se cambia el estado de la cita a `CANCELADA` y se registra la fecha/hora de cancelación.

### 6.7 Flujo: Reprogramación de Cita

1. Se solicita la reprogramación de una cita existente a un nuevo horario.
2. El sistema cancela la cita original (aplicando RN-05 si corresponde).
3. El sistema valida disponibilidad del nuevo horario (RN-01, RN-02, RN-04).
4. Se crea una nueva cita con el nuevo horario en estado `PROGRAMADA`.

### 6.8 Flujo: Consulta de Disponibilidad

1. Se proporciona `medicoId`, `fechaInicio`, `fechaFin`.
2. El sistema genera todas las franjas de 30 minutos dentro del horario de atención para cada día en el rango (Lun-Vie 08:00-18:00, Sáb 08:00-13:00, excluyendo Dom y festivos de Colombia).
3. Los festivos se obtienen del caché en BD; si no existen para el año consultado, se consultan de la API de Nager.Date y se almacenan.
4. Se filtran las franjas donde el médico ya tiene una cita.
5. Se devuelven las franjas disponibles.

### 6.9 Flujo: Listado de Citas

1. Se solicitan las citas con filtros opcionales: `medicoId`, `pacienteId`, estado (`PROGRAMADA`, `CANCELADA`), rango de fechas.
2. El sistema devuelve las citas que coinciden con los filtros.

### 6.10 Flujo: Consulta de Cita Individual

1. Se solicita una cita por su ID.
2. Si la cita existe, se devuelve con todos sus datos.
3. Si no existe, se retorna **404 Not Found**.

### 6.11 Flujo: Carga de Festivos desde Nager.Date

1. El sistema necesita validar si una fecha es festivo (para RN-01).
2. Consulta la tabla de festivos en BD para el año de la fecha.
3. Si la tabla está vacía para ese año, realiza una llamada a `GET https://date.nager.at/api/v3/PublicHolidays/{year}/CO`.
4. Almacena los festivos obtenidos en la tabla de festivos.
5. Si ya existen festivos para ese año, los consulta directamente de BD.

---

## 7. Entidades Funcionales

### 7.1 Médico

| Campo | Descripción | Obligatorio | Restricciones |
|-------|-------------|-------------|---------------|
| ID | Identificador único | Sí (generado) | UUID |
| Nombre completo | Nombre del médico | Sí | 3-100 caracteres |
| Especialidad | Área médica | Sí | Ej: Cardiología, Pediatría, Dermatología |
| Teléfono | Número de contacto | No | Mínimo 7 dígitos. Solo se almacenan caracteres numéricos (se eliminan guiones, espacios, etc.). |
| Email | Correo electrónico | No | Formato email válido |

### 7.2 Paciente

| Campo | Descripción | Obligatorio | Restricciones |
|-------|-------------|-------------|---------------|
| ID | Identificador único | Sí (generado) | UUID |
| Nombre completo | Nombre del paciente | Sí | 3-100 caracteres |
| Documento de identidad | Identificación oficial | Sí | Único, mínimo 7 caracteres |
| Teléfono | Número de contacto | Sí | Mínimo 7 dígitos. Solo se almacenan caracteres numéricos. |
| Email | Correo electrónico | Sí | Formato email válido |
| Fecha de nacimiento | Fecha de nacimiento del paciente | No | Formato ISO 8601 date (`YYYY-MM-DD`). No puede ser futura (RN-03). Si no se provee, se asume edad 0. |

### 7.3 Cita

| Campo | Descripción | Obligatorio | Restricciones |
|-------|-------------|-------------|---------------|
| ID | Identificador único | Sí (generado) | UUID |
| Paciente | Referencia al paciente | Sí | Debe existir (404 si no existe) |
| Médico | Referencia al médico | Sí | Debe existir (404 si no existe) |
| Fecha y hora | Momento de la cita | Sí | ISO 8601, dentro de franja válida (RN-01) |
| Estado | Estado de la cita | Sí | `PROGRAMADA`, `CANCELADA` |
| Fecha/hora de cancelación | Momento de cancelación | Condicional | Solo si estado = `CANCELADA` |

### 7.4 Penalización

| Campo | Descripción |
|-------|-------------|
| Paciente | Referencia al paciente penalizado |
| Cita | Referencia a la cita cancelada tardíamente |
| Fecha/hora | Momento en que se registró la penalización |

> **Nota:** La entidad Penalización es implícita en RN-05. No se define explícitamente en los RF pero es necesaria para llevar el conteo.

### 7.5 Festivo

| Campo | Descripción | Obligatorio | Restricciones |
|-------|-------------|-------------|---------------|
| ID | Identificador único | Sí (generado) | UUID |
| date | Fecha del festivo | Sí | Formato ISO 8601 date |
| local_name | Nombre local del festivo | Sí | Ej: "Día de la Independencia" |
| name | Nombre internacional | No | Ej: "Independence Day" |
| country_code | Código del país | Sí | Fijo: `CO` (Colombia) |
| fixed | Si es fecha fija | No | Booleano |
| global | Si aplica a todo el país | No | Booleano |
| types | Tipos de festivo | No | Lista de tipos (ej: Public, Bank, School) |
| year | Año del festivo | Sí | Ej: 2026 |

> **Nota:** Esta entidad persiste los datos obtenidos de la API de Nager.Date para evitar llamadas repetidas. Sigue la política de carga lazy/first-access.

### 7.6 Franja Horaria (concepto funcional, no necesariamente entidad persistente)

- Duración: 30 minutos.
- Horario de atención:
  - Lunes a Viernes: 08:00 - 18:00 → 20 franjas por día.
  - Sábados: 08:00 - 13:00 → 10 franjas por día.
  - Domingos y festivos: sin atención.

---

## 8. Integraciones

| Sistema externo | Propósito | Dirección | Criticidad | Frecuencia |
|-----------------|-----------|-----------|------------|------------|
| **Nager.Date API** (https://date.nager.at) | Obtener días festivos de Colombia para validar RN-01 | Salida (el sistema consume la API) | Media | Una vez por año (caché en BD). Si la tabla de festivos está vacía para el año consultado, se obtienen de la API y se almacenan. Si ya existen, se consultan de BD. |

### 8.1 Detalle de Integración: Nager.Date API

- **Endpoint:** `GET https://date.nager.at/api/v3/PublicHolidays/{year}/CO`
- **Política de uso:** Carga lazy/first-access.
  1. Cuando el sistema necesita validar si una fecha es festivo, primero consulta la tabla de festivos en BD para el año correspondiente.
  2. Si la tabla está vacía para ese año, se invoca la API de Nager.Date, se almacenan los resultados en la tabla de festivos y se usa la respuesta.
  3. Si ya existen festivos para ese año en BD, se consultan directamente de la base de datos sin llamar a la API.
- **Manejo de errores:** Si la API no está disponible y no hay datos en BD, el sistema debe manejar el error gracefully (registrar en log y posiblemente rechazar la operación o asumir que no hay festivos).

---

## 9. Seguridad y Restricciones

### 9.1 Control de Acceso

- **No definido en el enunciado.** No se especifica mecanismo de autenticación ni autorización.
- Se asume que todos los endpoints son accesibles sin autenticación en esta versión.
- La autenticación/autorización queda como mejora propuesta (ver sección 15).

### 9.2 Validaciones de Integridad

| Validación | Descripción |
|------------|-------------|
| Documento único | El documento de identidad del paciente debe ser único en el sistema. |
| Franja válida | La fecha/hora de la cita debe caer exactamente en una franja de 30 minutos dentro del horario de atención. |
| Sin duplicidad de médico | Dos citas del mismo médico no pueden compartir la misma franja. |
| Sin conflicto de paciente | Dos citas del mismo paciente no pueden compartir la misma franja. |
| Estado válido | Solo `PROGRAMADA` y `CANCELADA`. |
| Penalización | Cancelación con `<= 2h` de antelación genera penalización. |
| Bloqueo por penalizaciones | 3+ penalizaciones en 30 días bloquea nuevo agendamiento. |
| Paciente/Médico existente | Al crear una cita, tanto `pacienteId` como `medicoId` deben existir. Si no, 404 Not Found. |
| Fecha de nacimiento no futura | La fecha de nacimiento del paciente no puede ser una fecha futura (RN-03). |
| Teléfono médico | Mínimo 7 dígitos. Solo se almacenan caracteres numéricos (se eliminan guiones, espacios). |

### 9.3 Manejo de Errores

- Cada validación fallida debe retornar un error claro y descriptivo.
- Manejo global de errores mediante `@RestControllerAdvice`.
- **Formato de error estándar:** `ApiErrorResponse` con los siguientes campos:
  - `timestamp`: Fecha/hora del error (ISO 8601).
  - `status`: Código HTTP de estado.
  - `error`: Descripción breve del tipo de error.
  - `code`: Código interno estable del error.
  - `message`: Mensaje descriptivo y claro del error.
  - `path`: Ruta del endpoint que generó el error.
  - `trace_id`: Identificador único de la traza (para debugging).
  - `details`: Lista de detalles adicionales (ej: campos con errores de validación).
- **Códigos HTTP esperados:**
  - `400 Bad Request`: Errores de validación de negocio (RN-01 a RN-06).
  - `404 Not Found`: Paciente o médico inexistente al crear cita; cita inexistente al consultar/cancelar.
  - `409 Conflict`: Documento de identidad duplicado.
  - `500 Internal Server Error`: Errores inesperados del sistema.

### 9.4 Datos Sensibles

- Documento de identidad: dato personal, debe ser único.
- Email y teléfono: datos de contacto personal.
- Fecha de nacimiento: dato personal sensible.

---

## 10. Edge Cases

| # | Escenario | Comportamiento esperado |
|---|-----------|------------------------|
| EC-01 | Intentar agendar en domingo | Rechazar: no hay atención. |
| EC-02 | Intentar agendar en festivo | Rechazar: no hay atención. El festivo se valida contra la tabla de festivos (caché de Nager.Date). |
| EC-03 | Intentar agendar fuera de horario (ej: 19:00) | Rechazar: fuera de franja válida. |
| EC-04 | Intentar agendar en franja no exacta (ej: 08:15) | Rechazar: debe ser inicio de franja de 30 min (08:00 o 08:30). |
| EC-05 | Dos solicitudes simultáneas para la misma franja y médico | Solo una debe ser aceptada (integridad RN-02). |
| EC-06 | Dos solicitudes simultáneas para la misma franja y paciente (diferente médico) | Solo una debe ser aceptada (integridad RN-04). |
| EC-07 | Cancelar una cita ya cancelada | Rechazar o indicar que ya está cancelada. |
| EC-08 | Paciente con exactamente 3 penalizaciones en los últimos 30 días | Bloquear nuevo agendamiento. |
| EC-09 | Paciente con 2 penalizaciones en los últimos 30 días | Permitir agendamiento (aún no llega al umbral). |
| EC-10 | Cancelar exactamente a 2 horas de la cita | Genera penalización (`<= 2h` es inclusivo: 2 horas exactas sí penaliza). |
| EC-11 | Reprogramar una cita que ya fue cancelada | Rechazar: solo se pueden reprogramar citas en estado `PROGRAMADA`. |
| EC-12 | Consultar disponibilidad con fechaInicio > fechaFin | Rechazar o devolver resultado vacío. |
| EC-13 | Consultar disponibilidad con rango que incluye domingos/festivos | Solo devolver franjas de días hábiles no festivos. |
| EC-14 | Registrar paciente con documento de identidad duplicado | Rechazar con error 409 Conflict claro. |
| EC-15 | Registrar médico/paciente con nombre de menos de 3 caracteres | Rechazar con error de validación. |
| EC-16 | Registrar paciente sin fecha de nacimiento | Asumir edad 0 según RN-03. |
| EC-17 | Registrar paciente con fecha de nacimiento futura | Rechazar según RN-03. |
| EC-18 | Listar citas sin ningún filtro | Devolver todas las citas. |
| EC-19 | Crear cita con pacienteId inexistente | Retornar 404 Not Found con mensaje claro. |
| EC-20 | Crear cita con medicoId inexistente | Retornar 404 Not Found con mensaje claro. |
| EC-21 | Consultar cita inexistente por ID | Retornar 404 Not Found. |
| EC-22 | Cancelar cita inexistente por ID | Retornar 404 Not Found. |
| EC-23 | Actualizar médico/paciente inexistente | Retornar 404 Not Found. |
| EC-24 | API de Nager.Date no disponible y no hay festivos en BD | Manejar gracefully: registrar en log y definir comportamiento (rechazar operación o asumir sin festivos). |
| EC-25 | Registrar médico con teléfono de menos de 7 dígitos | Rechazar con error de validación. |
| EC-26 | Registrar médico con teléfono con formato especial (ej: "555-1001") | Rechazar por contener caracteres no numéricos. El frontend debe limpiar el formato antes de enviar. |

---

## 11. Criterios de Aceptación

### CA-01: Registro de Médicos

- [ ] Se puede crear un médico con todos los campos obligatorios y el sistema asigna un UUID.
- [ ] Se rechaza un médico con nombre de menos de 3 o más de 100 caracteres.
- [ ] Se rechaza un médico sin especialidad.
- [ ] El teléfono, si se proporciona, debe tener al menos 7 dígitos.
- [ ] El teléfono se almacena solo con dígitos (se eliminan automáticamente caracteres no numéricos).
- [ ] El email, si se proporciona, debe tener formato válido.
- [ ] Los campos opcionales (teléfono, email) pueden omitirse sin error.

### CA-02: Registro de Pacientes

- [ ] Se puede crear un paciente con todos los campos obligatorios y el sistema asigna un UUID.
- [ ] Se rechaza un paciente con nombre de menos de 3 o más de 100 caracteres.
- [ ] Se rechaza un paciente sin documento de identidad.
- [ ] Se rechaza un paciente con documento de identidad duplicado.
- [ ] El documento de identidad debe tener al menos 7 caracteres.
- [ ] El teléfono es obligatorio, solo dígitos, y debe tener al menos 7 caracteres.
- [ ] El email es obligatorio y debe tener formato válido.
- [ ] Si se proporciona fecha de nacimiento, no puede ser futura (RN-03).
- [ ] Si no se proporciona fecha de nacimiento, se asume edad 0.
- [ ] La fecha de nacimiento se acepta en formato ISO 8601 date (`YYYY-MM-DD`).

### CA-03: Reserva de Citas

- [ ] Se puede crear una cita con paciente, médico, fecha/hora válidos. Estado inicial = `PROGRAMADA`.
- [ ] Se rechaza una cita en horario no válido (domingo, festivo, fuera de horario, franja no exacta).
- [ ] Se rechaza una cita si el médico ya tiene cita en esa franja (RN-02).
- [ ] Se rechaza una cita si el paciente ya tiene cita en esa franja (RN-04).
- [ ] Se rechaza una cita si el paciente tiene 3+ penalizaciones en 30 días (RN-05).
- [ ] Si `pacienteId` no existe, se retorna 404 Not Found con mensaje claro.
- [ ] Si `medicoId` no existe, se retorna 404 Not Found con mensaje claro.
- [ ] El mensaje de error indica claramente la razón del rechazo.

### CA-04: Consulta de Disponibilidad

- [ ] Dado un médico y un rango de fechas, se devuelven solo las franjas de 30 min disponibles.
- [ ] Las franjas ocupadas no aparecen en el resultado.
- [ ] Solo se incluyen franjas dentro del horario de atención (Lun-Vie 08:00-18:00, Sáb 08:00-13:00).
- [ ] No se incluyen franjas de domingos ni festivos de Colombia.
- [ ] Los festivos se obtienen del caché en BD; si no existen para el año, se consultan de Nager.Date.

### CA-05: Cancelación de Citas

- [ ] Se puede cancelar una cita en estado `PROGRAMADA`.
- [ ] El estado cambia a `CANCELADA`.
- [ ] Se registra la fecha/hora de cancelación.
- [ ] Si la cancelación es con 2 horas o menos (`<= 2h`) de antelación, se registra penalización.
- [ ] Si la cancelación es con más de 2 horas de antelación, no se registra penalización.

### CA-06: Listado de Citas

- [ ] Se pueden listar todas las citas sin filtros.
- [ ] Se pueden filtrar citas por `medicoId`.
- [ ] Se pueden filtrar citas por `pacienteId`.
- [ ] Se pueden filtrar citas por estado (`PROGRAMADA`, `CANCELADA`).
- [ ] Se pueden filtrar citas por rango de fechas.
- [ ] Los filtros son combinables.

### CA-07: Consulta y Actualización de Médicos

- [ ] Se pueden listar todos los médicos registrados.
- [ ] Se puede consultar un médico individual por su ID.
- [ ] Si el médico no existe, se retorna 404 Not Found.
- [ ] Se pueden actualizar los datos de un médico existente por su ID.
- [ ] Si el médico a actualizar no existe, se retorna 404 Not Found.

### CA-08: Consulta y Actualización de Pacientes

- [ ] Se pueden listar todos los pacientes registrados.
- [ ] Se puede consultar un paciente individual por su ID.
- [ ] Si el paciente no existe, se retorna 404 Not Found.
- [ ] Se pueden actualizar los datos de un paciente existente por su ID.
- [ ] Si el paciente a actualizar no existe, se retorna 404 Not Found.

### CA-09: Consulta de Cita Individual

- [ ] Se puede consultar una cita individual por su ID.
- [ ] Si la cita no existe, se retorna 404 Not Found.

### CA-10: Reglas de Negocio

- [ ] **RN-01:** Solo se permiten citas en franjas de 30 min dentro del horario de atención (excluyendo domingos y festivos de Colombia).
- [ ] **RN-02:** Ningún médico tiene dos citas en la misma franja.
- [ ] **RN-03:** Ningún paciente tiene fecha de nacimiento futura.
- [ ] **RN-04:** Ningún paciente tiene dos citas en la misma franja.
- [ ] **RN-05:** Cancelación con `<= 2h` de antelación genera penalización. 3+ penalizaciones en 30 días bloquea agendamiento.
- [ ] **RN-06:** Reprogramación cancela la cita original (con penalización si aplica) y crea una nueva válida.

### CA-11: Datos Iniciales

- [ ] Los tres médicos del enunciado están disponibles tras la carga inicial.

### CA-12: Manejo de Errores

- [ ] Todas las respuestas de error siguen el formato `ApiErrorResponse` (timestamp, status, error, code, message, path, trace_id, details).
- [ ] Los mensajes de error son claros y descriptivos.
- [ ] Se usan códigos HTTP apropiados (400, 404, 409, 500).
- [ ] El manejo de errores es global (`@RestControllerAdvice`).

### CA-13: Integración con Nager.Date

- [ ] Los festivos de Colombia se obtienen de la API de Nager.Date cuando no existen en BD para el año consultado.
- [ ] Los festivos obtenidos se almacenan en la tabla de festivos.
- [ ] Si ya existen festivos para el año en BD, no se llama a la API.
- [ ] La validación de festivos funciona correctamente en RN-01.

---

## 12. Preguntas Abiertas

### 12.1 Preguntas Resueltas

Las siguientes preguntas fueron planteadas durante el discovery y ya tienen respuesta definida:

| # | Pregunta | Respuesta |
|---|----------|-----------|
| P-01 | ¿Se requiere autenticación y autorización? | **No** en esta versión. No se menciona en el enunciado. Queda como mejora propuesta (ver sección 15). |
| P-02 | ¿Quién puede registrar médicos y cancelar citas? | Se asume que las operaciones de gestión de médicos y cancelación son accesibles sin restricción (admin implícito). No hay autenticación en esta versión. |
| P-03 | ¿Cómo se definen los festivos? | Se consumen de la **API de Nager.Date** (https://date.nager.at) con política de carga lazy/first-access. Se almacenan en una tabla de festivos en BD. Si ya existen para el año, se consultan de BD; si no, se obtienen de la API. |
| P-04 | ¿La fecha de nacimiento del paciente es un campo del registro (RF-02)? | **Sí**, se agrega como campo **opcional** en RF-02 con formato ISO 8601 date. Cumple RN-03: no puede ser futura; si no se provee, edad = 0. |
| P-05 | ¿Cómo y quién marca una cita como ATENDIDA? | El estado `ATENDIDA` **se elimina** de los estados válidos en esta versión. Los únicos estados son `PROGRAMADA` y `CANCELADA`. Queda como mejora propuesta (ver sección 15). |

### 12.2 Preguntas No Críticas (no bloquean handoff a Planner)

| # | Pregunta | Impacto |
|---|----------|---------|
| P-06 | ~~¿El umbral de 2 horas para cancelación tardía es inclusivo o exclusivo?~~ | **Resuelto:** Es inclusivo (`<= 2h`). Cancelar exactamente a 2 horas genera penalización. |
| P-07 | ¿Se requiere paginación en el listado de citas (RF-06), médicos y pacientes? | No se especifica en el enunciado. Se asume que no hay paginación. Queda como mejora propuesta. |
| P-08 | ¿Se pueden eliminar médicos o pacientes? | El enunciado no menciona eliminación. Se asume que no se eliminan, solo se registran, consultan y actualizan. |
| P-09 | ¿La reprogramación (RN-06) es un endpoint separado o es una combinación de cancelar + crear? | Funcionalmente es claro: cancelar + crear. La decisión de si es un endpoint dedicado o dos operaciones separadas queda para Planner. |
| P-10 | ¿Las penalizaciones tienen fecha de expiración automática? | Funcionalmente es claro: conteo dinámico en ventana de 30 días hacia atrás desde la fecha de la nueva reserva. |
| P-11 | ¿Se requiere algún ordenamiento por defecto en el listado de citas? | Detalle de diseño de API. Se sugiere por fecha descendente, pero queda para Planner. |
| P-12 | ~~¿El teléfono del médico acepta solo dígitos o también caracteres como guiones?~~ | **Resuelto:** Solo dígitos. La API rechaza caracteres no numéricos (pattern `^\d{7,20}$`). El backend sanitiza con `replaceAll("\\D", "")` como defensa en profundidad. El frontend debe encargarse del formateo visual. |

---

## 13. Supuestos

| # | Supuesto | Justificación |
|---|----------|---------------|
| S-01 | No se requiere autenticación ni autorización en esta versión. | El enunciado no lo menciona. Responde P-01. |
| S-02 | Las operaciones de registro de médicos son accesibles sin restricción (como un admin implícito). | No se define un rol administrador explícito. Responde P-02. |
| S-03 | Los festivos se obtienen de la API de Nager.Date y se almacenan en BD con política lazy/first-access. | Responde P-03. Se define la entidad Festivo y la integración. |
| S-04 | La fecha de nacimiento del paciente se agrega como campo opcional en RF-02. | Responde P-04. RN-03 necesita este campo para operar. |
| S-05 | El estado `ATENDIDA` no existe en esta versión. Solo `PROGRAMADA` y `CANCELADA`. | Responde P-05. No hay transición definida en el enunciado. |
| S-06 | No se requiere paginación en listados; se devuelven todos los resultados. | No se menciona paginación en el enunciado. |
| S-07 | Los médicos y pacientes no se eliminan; solo se crean, consultan y actualizan. | No se menciona eliminación en ningún RF. |
| S-08 | La reprogramación es una operación lógica (cancelar + crear). | RN-06 lo describe como un flujo unitario. |
| S-09 | El sistema opera en una sola zona horaria (la local del servidor). | No se menciona manejo de zonas horarias. |
| S-10 | Las penalizaciones se calculan dinámicamente sobre la ventana de 30 días anteriores a la fecha de la nueva reserva. | Es la interpretación más natural de "en los últimos 30 días". |
| S-11 | El umbral de 2 horas para penalización es inclusivo (`<= 2h`). | Responde P-06. |
| S-12 | El teléfono se almacena solo con dígitos, sin guiones ni espacios. La API valida con `pattern: ^\d{7,20}$`. | Responde P-12. |

---

## 14. Handoff para Planner

### Resumen Funcional

Se requiere una API REST para agendamiento de citas médicas con las siguientes capacidades:

- **CRUD parcial de médicos:** crear, listar, consultar individual, actualizar.
- **CRUD parcial de pacientes:** crear, listar, consultar individual, actualizar.
- **Reserva de citas** con validación de franjas horarias, duplicidad, conflictos y existencia de paciente/médico.
- **Consulta de disponibilidad** por médico y rango de fechas (excluyendo domingos y festivos de Colombia).
- **Cancelación** con registro de penalizaciones por cancelación tardía (`<= 2h`).
- **Reprogramación** como cancelación + nueva reserva.
- **Listado de citas** con filtros múltiples.
- **Consulta de cita individual** por ID.
- **Gestión de festivos** mediante integración con Nager.Date API (caché en BD).

### Entregables funcionales para Planner

1. **7 entidades funcionales:** Médico, Paciente, Cita, Penalización, Festivo, Franja Horaria (concepto), y posiblemente entidades de soporte.
2. **Grupos de endpoints:**
   - Registro de médicos: `POST /medicos`
   - Listar médicos: `GET /medicos`
   - Consultar médico individual: `GET /medicos/{id}`
   - Actualizar médico: `PUT /medicos/{id}`
   - Registro de pacientes: `POST /pacientes`
   - Listar pacientes: `GET /pacientes`
   - Consultar paciente individual: `GET /pacientes/{id}`
   - Actualizar paciente: `PUT /pacientes/{id}`
   - Reserva de cita: `POST /citas`
   - Consulta de disponibilidad: `GET /disponibilidad` (o similar)
   - Cancelación de cita: `DELETE /citas/{id}` o `PUT /citas/{id}/cancelar`
   - Consulta de cita individual: `GET /citas/{id}`
   - Listado de citas: `GET /citas`
   - Reprogramación de cita: `PUT /citas/{id}/reprogramar` o similar
3. **6 reglas de negocio** que deben validarse en el dominio: franjas horarias (con festivos), no duplicidad médico, antigüedad, conflicto paciente, penalización, reprogramación.
4. **3 médicos seed** para datos iniciales.
5. **Manejo de errores global** con formato `ApiErrorResponse` (timestamp, status, error, code, message, path, trace_id, details) y códigos HTTP apropiados (400, 404, 409, 500).
6. **Integración con Nager.Date API** para festivos de Colombia con política lazy/first-access.

### Entregables de Proyecto

Los siguientes entregables son requeridos según el PDF del proyecto:

1. **Repositorio GitHub público** con el código fuente completo.
2. **README.md completo** que incluya: instrucciones de ejecución, arquitectura, tecnologías utilizadas y endpoints con ejemplos de request/response.
3. **Tests automatizados** que validen los flujos de negocio.
4. **Despliegue en nube** (valorado positivamente como diferenciador en la evaluación del proyecto).

### Criterios de Evaluación del Proyecto

Los siguientes criterios fueron identificados en el PDF del proyecto y deben ser considerados durante la implementación:

- **Principios SOLID y DRY:** El código debe seguir estos principios de diseño. Especialmente:
  - Single Responsibility: cada clase/módulo tiene una única razón para cambiar.
  - Open/Closed: abierto para extensión, cerrado para modificación.
  - Dependency Inversion: depender de abstracciones, no de implementaciones.
  - DRY: no duplicar lógica de negocio.
- **Protección contra inyección SQL:** Uso de JPA/Hibernate parametrizado para todas las consultas. Nunca concatenar SQL manualmente.
- **Cobertura de pruebas:** Se esperan pruebas automatizadas con cobertura significativa (unitarias, integración, arquitectura). JaCoCo para medición con umbral mínimo del 85%.
- **Documentación:** El README.md debe ser completo e incluir:
  - Descripción del proyecto y arquitectura.
  - Tecnologías utilizadas.
  - Endpoints disponibles con ejemplos de request/response.
  - Instrucciones de instalación y ejecución.
  - Sección de "Mejoras Propuestas" con las sugerencias de la sección 15 de este brief.

### Decisiones pendientes que Planner debe resolver

| Decisión | Opciones | Recomendación funcional |
|----------|----------|------------------------|
| Reprogramación | Endpoint dedicado / Dos operaciones separadas | **Endpoint dedicado** para atomicidad funcional. |
| Paginación | Sí / No | **No** en esta versión. Queda como mejora propuesta. |
| Endpoint de cancelación | `DELETE /citas/{id}` / `PUT /citas/{id}/cancelar` | Planner decide según convención REST. Funcionalmente es cambiar estado a CANCELADA. |
| Endpoint de disponibilidad | Ruta y estructura | Planner define. Funcionalmente: dado medicoId, fechaInicio, fechaFin → franjas disponibles. |
| Manejo de errores de Nager.Date | Rechazar operación / Asumir sin festivos / Reintentar | Planner define estrategia. Funcionalmente: si no hay festivos y la API falla, el sistema debe manejarlo gracefully. |

### Riesgos funcionales

| Riesgo | Mitigación |
|--------|------------|
| Concurrencia en reserva (dos peticiones simultáneas para la misma franja) | Planner debe definir estrategia de locking o validación atómica. |
| Interpretación de "franjas de 30 minutos" | ¿La cita debe iniciar exactamente en :00 o :30? La interpretación más natural es que el inicio debe ser :00 o :30. |
| Ventana de 30 días para penalizaciones | Son 30 días calendario hacia atrás desde la fecha de la nueva reserva. Definir claramente en la spec. |
| API de Nager.Date no disponible | Planner debe definir estrategia: caché persistente, reintento, fallback. Si no hay festivos en BD y la API falla, ¿se asume que no hay festivos o se rechaza la operación? |
| Festivos de Colombia cambian entre años | La política lazy/first-access por año resuelve esto: cada año se consultan una vez y se cachean. |

### Nota sobre README.md

El README.md del proyecto debe incluir una sección de **"Mejoras Propuestas"** que liste las sugerencias identificadas durante el análisis de requerimientos (ver sección 15 de este brief). Esto demuestra visión de producto y mejora continua.

---

## 15. Mejoras Propuestas

Las siguientes mejoras **NO están en el alcance del PDF/enunciado** pero fueron identificadas como valiosas durante el análisis de requerimientos. Quedan como sugerencias para futuras iteraciones o para ser evaluadas por el equipo. El despliegue en AWS ECS Fargate es parte de los entregables del proyecto (ver Master Spec) y no se lista aquí:

1. **Estado ATENDIDA para citas:** Agregar un estado `ATENDIDA` para citas que ya fueron realizadas, con un endpoint para marcar la transición (ej: `PATCH /citas/{id}/atender`). Esto permitiría llevar un historial completo del ciclo de vida de la cita.

2. **Paginación en listados:** Agregar paginación a `GET /citas`, `GET /medicos`, `GET /pacientes` para manejar grandes volúmenes de datos y mejorar el rendimiento de la API.

3. **Autenticación y Autorización:** Implementar JWT o Keycloak para proteger endpoints por rol (admin vs paciente). Esto permitiría controlar quién puede registrar médicos, cancelar citas, etc.

4. **Notificaciones:** Enviar emails/SMS recordatorios de citas programadas y confirmaciones de cancelaciones. Esto reduciría el ausentismo y mejoraría la experiencia del paciente.

5. **Calendario de festivos configurable:** Interfaz de administración para gestionar festivos manualmente, además de la integración con Nager.Date. Esto permitiría agregar festivos locales o extraordinarios.

6. **Cancelación masiva:** Permitir cancelar múltiples citas a la vez (ej: cuando un médico no puede atender por emergencia).

7. **Logs de auditoría:** Registrar quién hizo cada operación y cuándo (crear médico, crear paciente, crear cita, cancelar cita, etc.). Esto es útil para troubleshooting y cumplimiento.

8. **Internacionalización (i18n):** Soporte multi-idioma para mensajes de error y respuestas de la API. Esto sería útil si la clínica atiende pacientes extranjeros.

9. **Pruebas E2E con frontend:** Suite de pruebas end-to-end si se desarrolla frontend en el futuro. Esto aseguraría la integración completa del sistema.

10. **Frontend con React o Angular:** Desarrollar una interfaz de usuario para que los pacientes puedan agendar citas visualmente. Incluiría: calendario visual con franjas disponibles, formularios de registro, gestión de citas del paciente, y panel administrativo para médicos.

---

*Fin del Requirements Brief.*
