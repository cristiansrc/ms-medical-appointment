# Master Spec — Sistema de Agendamiento de Citas Medicas (MediSalud)

> **Version:** 1.0.0
> **Fecha:** 2026-07-17
> **Autor:** planner
> **Status:** `approved`
> **Incremento:** medical-appointment

---

## 1. Resumen Ejecutivo

MediSalud es una clinica que actualmente gestiona sus citas medicas por telefono. Este sistema digitaliza el proceso de agendamiento mediante una API REST que permite:

- Registrar, consultar y actualizar medicos y pacientes.
- Consultar disponibilidad de citas en franjas de 30 minutos.
- Reservar citas validando reglas de negocio (sin duplicidad, sin conflictos, sin festivos).
- Cancelar citas con registro de penalizaciones por cancelacion tardia.
- Reprogramar citas existentes.
- Listar citas con filtros multiples.
- Gestionar dias festivos de Colombia mediante integracion con la API de Nager.Date.

El sistema garantiza integridad en la reserva, control de disponibilidad por franja horaria, aplicacion de penalizaciones para reducir el ausentismo y gestion de festivos.

---

## 2. Stack Tecnologico

| Componente | Tecnologia | Version |
|---|---|---|
| Lenguaje | Java | 21 |
| Framework | Spring Boot | 3.x (latest stable) |
| Build Tool | Gradle (Groovy DSL) | 8.x |
| Base de Datos | PostgreSQL | 15+ |
| ORM | Spring Data JPA (Jakarta Persistence 3.x) | - |
| Migraciones | Flyway | 10.x |
| API Contract | OpenAPI | 3.1 |
| Code Generation | OpenAPI Generator Gradle Plugin | 7.x |
| Mapping | MapStruct | 1.6.x |
| Boilerplate | Lombok | 1.18.x |
| API Docs | springdoc-openapi-starter-webmvc-ui | 2.x |
| Testing | JUnit 5, Mockito, AssertJ, MockMvc, DataJpaTest | - |
| Architecture Tests | ArchUnit | 1.x |
| Coverage | JaCoCo | 0.8.x |
| Test DB | H2 (modo PostgreSQL) | - |
| JSON | Jackson (jsr310 support) | - |

---

## 3. Arquitectura

### 3.1 Patron: Arquitectura Hexagonal (Puertos y Adaptadores)

```
                    ┌─────────────────────────────────────┐
                    │         DRIVING ADAPTERS             │
                    │  ┌─────────────────────────────┐    │
                    │  │   REST Controllers           │    │
                    │  │   (infrastructure.web)       │    │
                    │  └──────────┬──────────────────┘    │
                    └─────────────┼───────────────────────┘
                                  │ calls
                    ┌─────────────▼───────────────────────┐
                    │         APPLICATION LAYER            │
                    │  ┌─────────────────────────────┐    │
                    │  │   Input Ports (Use Cases)    │    │
                    │  │   (application.port.input)   │    │
                    │  ├─────────────────────────────┤    │
                    │  │   Use Case Implementations   │    │
                    │  │   (application.service)      │    │
                    │  ├─────────────────────────────┤    │
                    │  │   Commands / Queries / Results│   │
                    │  │   (application.dto)          │    │
                    │  └──────────┬──────────────────┘    │
                    └─────────────┼───────────────────────┘
                                  │ uses
                    ┌─────────────▼───────────────────────┐
                    │          DOMAIN LAYER                │
                    │  ┌─────────────────────────────┐    │
                    │  │   Domain Models              │    │
                    │  │   (domain.model)             │    │
                    │  ├─────────────────────────────┤    │
                    │  │   Domain Services            │    │
                    │  │   (domain.service)           │    │
                    │  ├─────────────────────────────┤    │
                    │  │   Domain Exceptions          │    │
                    │  │   (domain.exception)         │    │
                    │  └─────────────────────────────┘    │
                    └─────────────────────────────────────┘
                                  ▲
                                  │ implements
                    ┌─────────────┴───────────────────────┐
                    │        DRIVEN ADAPTERS               │
                    │  ┌─────────────────────────────┐    │
                    │  │   JPA Repositories           │    │
                    │  │   (infrastructure.persistence)│   │
                    │  ├─────────────────────────────┤    │
                    │  │   HTTP Clients               │    │
                    │  │   (infrastructure.client)    │    │
                    │  ├─────────────────────────────┤    │
                    │  │   Configuration              │    │
                    │  │   (infrastructure.config)    │    │
                    │  └─────────────────────────────┘    │
                    └─────────────────────────────────────┘
```

### 3.2 Direccion de Dependencias

- `domain` → no depende de ninguna otra capa.
- `application` → depende de `domain`.
- `infrastructure` → depende de `application` y `domain`.

### 3.3 Estructura de Paquetes

```
com.medisalud.appointment/
├── domain/
│   ├── model/              ← Entidades de dominio + Value Objects
│   │   ├── Medico.java
│   │   ├── Paciente.java
│   │   ├── Cita.java
│   │   ├── Penalizacion.java
│   │   ├── Festivo.java
│   │   ├── EstadoCita.java         ← Enum: PROGRAMADA, CANCELADA
│   │   └── FranjaHoraria.java      ← Value Object
│   ├── service/            ← Domain services
│   │   ├── DisponibilidadService.java
│   │   ├── FranjaHorariaValidator.java
│   │   └── PenalizacionEvaluator.java
│   └── exception/          ← Domain exceptions
│       ├── BusinessException.java
│       ├── ResourceNotFoundException.java
│       ├── ConflictException.java
│       ├── SlotNotAvailableException.java
│       ├── PatientConflictException.java
│       ├── PatientBlockedException.java
│       └── InvalidSlotException.java
├── application/
│   ├── port/
│   │   ├── input/          ← Use cases (interfaces)
│   │   │   ├── CreateMedicoUseCase.java
│   │   │   ├── GetMedicoUseCase.java
│   │   │   ├── ListMedicosUseCase.java
│   │   │   ├── UpdateMedicoUseCase.java
│   │   │   ├── CreatePacienteUseCase.java
│   │   │   ├── GetPacienteUseCase.java
│   │   │   ├── ListPacientesUseCase.java
│   │   │   ├── UpdatePacienteUseCase.java
│   │   │   ├── CreateCitaUseCase.java
│   │   │   ├── GetCitaUseCase.java
│   │   │   ├── ListCitasUseCase.java
│   │   │   ├── CancelCitaUseCase.java
│   │   │   ├── ReprogramarCitaUseCase.java
│   │   │   └── ConsultarDisponibilidadUseCase.java
│   │   └── output/         ← Output ports (interfaces)
│   │       ├── MedicoRepositoryPort.java
│   │       ├── PacienteRepositoryPort.java
│   │       ├── CitaRepositoryPort.java
│   │       ├── PenalizacionRepositoryPort.java
│   │       └── FestivoRepositoryPort.java
│   ├── service/            ← Use case implementations
│   │   ├── MedicoService.java
│   │   ├── PacienteService.java
│   │   ├── CitaService.java
│   │   └── DisponibilidadService.java
│   └── dto/                ← Commands, queries, results
│       ├── command/
│       │   ├── CreateMedicoCommand.java
│       │   ├── UpdateMedicoCommand.java
│       │   ├── CreatePacienteCommand.java
│       │   ├── UpdatePacienteCommand.java
│       │   ├── CreateCitaCommand.java
│       │   └── ReprogramarCitaCommand.java
│       ├── query/
│       │   └── ListCitasQuery.java
│       └── result/
│           ├── MedicoResult.java
│           ├── PacienteResult.java
│           ├── CitaResult.java
│           ├── DisponibilidadResult.java
│           └── FranjaResult.java
└── infrastructure/
    ├── web/                ← Controllers + GlobalExceptionHandler
    │   ├── MedicoController.java
    │   ├── PacienteController.java
    │   ├── CitaController.java
    │   ├── DisponibilidadController.java
    │   ├── GlobalExceptionHandler.java
    │   └── mapper/         ← API DTO <-> Command/Result mappers
    │       └── WebMapper.java
    ├── persistence/        ← JPA entities, repositories, mappers
    │   ├── entity/
    │   │   ├── MedicoEntity.java
    │   │   ├── PacienteEntity.java
    │   │   ├── CitaEntity.java
    │   │   ├── PenalizacionEntity.java
    │   │   └── FestivoEntity.java
    │   ├── repository/
    │   │   ├── MedicoJpaRepository.java
    │   │   ├── PacienteJpaRepository.java
    │   │   ├── CitaJpaRepository.java
    │   │   ├── PenalizacionJpaRepository.java
    │   │   └── FestivoJpaRepository.java
    │   ├── adapter/
    │   │   ├── MedicoRepositoryAdapter.java
    │   │   ├── PacienteRepositoryAdapter.java
    │   │   ├── CitaRepositoryAdapter.java
    │   │   ├── PenalizacionRepositoryAdapter.java
    │   │   └── FestivoRepositoryAdapter.java
    │   └── mapper/
    │       └── PersistenceMapper.java  ← MapStruct: Entity <-> Domain
    ├── client/             ← HTTP clients
    │   └── NagerDateClient.java
    └── config/             ← Spring configurations
        ├── JacksonConfig.java
        ├── OpenApiConfig.java
        └── RestClientConfig.java
```

---

## 4. Modelo de Datos

### 4.1 Entidades y Relaciones

```
┌──────────────┐       ┌──────────────┐
│   medicos    │       │  pacientes   │
├──────────────┤       ├──────────────┤
│ id (UUID PK) │       │ id (UUID PK) │
│ nombre_completo      │ nombre_completo
│ especialidad │       │ documento_identidad (UNIQUE)
│ telefono     │       │ telefono     │
│ email        │       │ email        │
│ created_at   │       │ birth_date   │
│ updated_at   │       │ created_at   │
└──────┬───────┘       │ updated_at   │
       │               └──────┬───────┘
       │                      │
       │  ┌───────────────────┤
       │  │                   │
       ▼  ▼                   ▼
┌──────────────┐       ┌──────────────┐
│    citas     │       │penalizaciones│
├──────────────┤       ├──────────────┤
│ id (UUID PK) │◄──────│ id (UUID PK) │
│ paciente_id  │  ref  │ paciente_id  │
│ medico_id    │       │ cita_id      │
│ fecha_hora   │       │ fecha_hora   │
│ estado       │       │ created_at   │
│ motivo_cancelacion   └──────────────┘
│ fecha_cancelacion
│ created_at   │
│ updated_at   │
└──────────────┘

┌──────────────┐
│   festivos   │
├──────────────┤
│ id (UUID PK) │
│ date         │
│ local_name   │
│ name         │
│ country_code │
│ fixed        │
│ global       │
│ types        │
│ year         │
│ created_at   │
│ updated_at   │
│ UNIQUE(date, year, country_code)
└──────────────┘
```

### 4.2 Tabla: medicos

| Columna | Tipo | Nullable | Default | Descripcion |
|---|---|---|---|---|
| id | UUID | NOT NULL | - | PK, generado por aplicacion |
| nombre_completo | VARCHAR(100) | NOT NULL | - | 3-100 caracteres |
| especialidad | VARCHAR(100) | NOT NULL | - | Area medica |
| telefono | VARCHAR(20) | NULLABLE | NULL | Minimo 7 caracteres si se provee |
| email | VARCHAR(255) | NULLABLE | NULL | Formato email valido si se provee |
| created_at | TIMESTAMPTZ | NOT NULL | CURRENT_TIMESTAMP | Auditoria |
| updated_at | TIMESTAMPTZ | NOT NULL | CURRENT_TIMESTAMP | Auditoria (trigger) |

### 4.3 Tabla: pacientes

| Columna | Tipo | Nullable | Default | Descripcion |
|---|---|---|---|---|
| id | UUID | NOT NULL | - | PK, generado por aplicacion |
| nombre_completo | VARCHAR(100) | NOT NULL | - | 3-100 caracteres |
| documento_identidad | VARCHAR(20) | NOT NULL | - | UNIQUE, minimo 7 caracteres |
| telefono | VARCHAR(20) | NOT NULL | - | Minimo 7 digitos |
| email | VARCHAR(255) | NOT NULL | - | Formato email valido |
| birth_date | DATE | NULLABLE | NULL | No futura (RN-03) |
| created_at | TIMESTAMPTZ | NOT NULL | CURRENT_TIMESTAMP | Auditoria |
| updated_at | TIMESTAMPTZ | NOT NULL | CURRENT_TIMESTAMP | Auditoria (trigger) |

### 4.4 Tabla: citas

| Columna | Tipo | Nullable | Default | Descripcion |
|---|---|---|---|---|
| id | UUID | NOT NULL | - | PK, generado por aplicacion |
| paciente_id | UUID | NOT NULL | - | FK → pacientes(id) |
| medico_id | UUID | NOT NULL | - | FK → medicos(id) |
| fecha_hora | TIMESTAMPTZ | NOT NULL | - | Inicio de franja de 30 min |
| estado | VARCHAR(20) | NOT NULL | 'PROGRAMADA' | CHECK IN ('PROGRAMADA','CANCELADA') |
| motivo_cancelacion | VARCHAR(255) | NULLABLE | NULL | Solo si CANCELADA |
| fecha_cancelacion | TIMESTAMPTZ | NULLABLE | NULL | Solo si CANCELADA |
| created_at | TIMESTAMPTZ | NOT NULL | CURRENT_TIMESTAMP | Auditoria |
| updated_at | TIMESTAMPTZ | NOT NULL | CURRENT_TIMESTAMP | Auditoria (trigger) |

**Indices:**
- `idx_citas_medico_fecha` ON (medico_id, fecha_hora)
- `idx_citas_paciente_fecha` ON (paciente_id, fecha_hora)
- `idx_citas_estado` ON (estado)

### 4.5 Tabla: penalizaciones

| Columna | Tipo | Nullable | Default | Descripcion |
|---|---|---|---|---|
| id | UUID | NOT NULL | - | PK, generado por aplicacion |
| paciente_id | UUID | NOT NULL | - | FK → pacientes(id) |
| cita_id | UUID | NOT NULL | - | FK → citas(id) |
| fecha_hora | TIMESTAMPTZ | NOT NULL | - | Momento del registro |
| created_at | TIMESTAMPTZ | NOT NULL | CURRENT_TIMESTAMP | Auditoria |

**Indices:**
- `idx_penalizaciones_paciente_fecha` ON (paciente_id, fecha_hora)

### 4.6 Tabla: festivos

| Columna | Tipo | Nullable | Default | Descripcion |
|---|---|---|---|---|
| id | UUID | NOT NULL | - | PK, generado por aplicacion |
| date | DATE | NOT NULL | - | Fecha del festivo |
| local_name | VARCHAR(255) | NOT NULL | - | Nombre local |
| name | VARCHAR(255) | NULLABLE | NULL | Nombre internacional |
| country_code | VARCHAR(10) | NOT NULL | - | Fijo: CO |
| fixed | BOOLEAN | NULLABLE | NULL | Fecha fija |
| global | BOOLEAN | NULLABLE | NULL | Aplica a todo el pais |
| types | TEXT | NULLABLE | NULL | Tipos separados por coma |
| year | INT | NOT NULL | - | Anio del festivo |
| created_at | TIMESTAMPTZ | NOT NULL | CURRENT_TIMESTAMP | Auditoria |
| updated_at | TIMESTAMPTZ | NOT NULL | CURRENT_TIMESTAMP | Auditoria (trigger) |

**Constraints:**
- UNIQUE(date, year, country_code)

**Indices:**
- `idx_festivos_year` ON (year)
- `idx_festivos_date` ON (date)

### 4.7 Politica de IDs

- Todas las entidades usan **UUID v4** como identificador primario.
- Los UUIDs son generados por la aplicacion (Java `UUID.randomUUID()`), no por la base de datos.
- Los UUIDs se almacenan como tipo nativo `UUID` en PostgreSQL.

---

## 5. Reglas de Negocio

### RN-01: Franjas Horarias

| Aspecto | Definicion |
|---|---|
| **Descripcion** | Lun-Vie: 08:00-18:00, Sab: 08:00-13:00, Dom/festivos: sin atencion. Franjas de 30 min. |
| **Strategy** | `FranjaHorariaValidator` en domain.service valida: (1) dia de semana permitido, (2) hora de inicio alineada a :00 o :30, (3) dentro del horario segun dia, (4) no es festivo (consulta FestivoRepositoryPort). |
| **Franjas por dia** | Lun-Vie: 20 franjas (08:00, 08:30, ..., 17:30). Sab: 10 franjas (08:00, 08:30, ..., 12:30). |
| **Festivos** | Se obtienen de tabla `festivos` (cache de Nager.Date). Si no existen para el anio, se consultan de la API externa. |
| **Error** | `InvalidSlotException` → 422 UNPROCESSABLE_ENTITY, code: `INVALID_SLOT` |

### RN-02: No Duplicidad (Medico)

| Aspecto | Definicion |
|---|---|
| **Descripcion** | Un medico no puede tener dos citas PROGRAMADAS en la misma franja de 30 min. |
| **Strategy** | `CitaService` consulta `CitaRepositoryPort.existsByMedicoIdAndFechaHoraAndEstado(medicoId, fechaHora, PROGRAMADA)` antes de crear. |
| **Error** | `SlotNotAvailableException` → 409 CONFLICT, code: `MEDICO_SLOT_CONFLICT` |

### RN-03: Antigueedad Minima

| Aspecto | Definicion |
|---|---|
| **Descripcion** | No se aceptan fechas de nacimiento futuras. Si no se proporciona, edad = 0. |
| **Strategy** | `PacienteService` valida que `birthDate <= LocalDate.now()` si se provee. |
| **Error** | `BusinessException` → 422 UNPROCESSABLE_ENTITY, code: `INVALID_BIRTH_DATE` |

### RN-04: Conflicto de Paciente

| Aspecto | Definicion |
|---|---|
| **Descripcion** | Un paciente no puede tener dos citas PROGRAMADAS en la misma franja (independiente del medico). |
| **Strategy** | `CitaService` consulta `CitaRepositoryPort.existsByPacienteIdAndFechaHoraAndEstado(pacienteId, fechaHora, PROGRAMADA)` antes de crear. |
| **Error** | `PatientConflictException` → 409 CONFLICT, code: `PACIENTE_SLOT_CONFLICT` |

> **Nota sobre RN-04:** El PDF de la prueba tiene una incongruencia en la redaccion de RN-04. La primera oracion dice "Un paciente no puede tener dos citas con el mismo medico en la misma franja horaria", pero la segunda oracion dice "(aunque sea otro medico en otra especialidad)", lo que contradice la primera. La implementacion adopta la interpretacion mas restrictiva (el paciente no puede tener ninguna cita en la misma franja, independientemente del medico), que es funcionalmente mas segura y coherente con el espiritu de evitar conflictos de horario para el paciente.

### RN-05: Penalizacion

| Aspecto | Definicion |
|---|---|
| **Descripcion** | Cancelar con <= 2h de antelacion genera penalizacion. 3+ penalizaciones en 30 dias bloquea agendamiento. |
| **Strategy** | (1) `PenalizacionEvaluator.shouldPenalize(fechaHoraCita, now)` retorna true si `Duration.between(now, fechaHoraCita) <= 2h`. (2) `PenalizacionEvaluator.isBlocked(pacienteId)` consulta conteo de penalizaciones en ventana de 30 dias. |
| **Ventana** | 30 dias calendario hacia atras desde la fecha de la nueva reserva (no desde now). |
| **Umbral** | `<= 2h` es inclusivo: cancelar exactamente a 2 horas genera penalizacion. |
| **Error bloqueo** | `PatientBlockedException` → 409 CONFLICT, code: `PACIENTE_BLOCKED` |

### RN-06: Reprogramacion

| Aspecto | Definicion |
|---|---|
| **Descripcion** | Cancelar cita anterior (aplicando RN-05 si corresponde) + crear nueva cita validando RN-01, RN-02 y RN-04. |
| **Strategy** | `ReprogramarCitaUseCase` es un endpoint dedicado (`POST /api/v1/citas/{citaId}/reprogramar`) que ejecuta atomicamente: (1) valida que la cita existe y esta PROGRAMADA, (2) cancela la cita original con penalizacion si aplica, (3) valida disponibilidad del nuevo horario, (4) crea nueva cita. |
| **Atomicidad** | Toda la operacion ocurre en una unica transaccion `@Transactional`. Si la nueva cita falla, la cancelacion se revierte. |
| **Error** | Si la cita ya esta CANCELADA → 409 CONFLICT, code: `CITA_ALREADY_CANCELLED` |

---

## 6. Contratos de API

### 6.1 Convenciones

| Aspecto | Convencion |
|---|---|
| Base path | `/api/v1` |
| JSON naming | `snake_case` |
| IDs en URL | UUID v4 |
| Fechas | ISO 8601 (`date` para birth_date, `date-time` para instantes) |
| Auth | Ninguna en esta version |
| Paginacion | No aplica en esta version |
| Ordenamiento | Listado de citas: `fecha_hora DESC` por defecto |

### 6.2 Endpoints: Medicos

#### POST /api/v1/medicos

| Aspecto | Detalle |
|---|---|
| Descripcion | Registrar un nuevo medico |
| Request Body | `MedicoRequest` |
| Success | `201 Created` + `Location: /api/v1/medicos/{id}` + `MedicoResponse` |
| Errors | `400` (validacion), `500` (inesperado) |
| Side Effects | Crea registro en BD |
| Idempotencia | No idempotente (genera nuevo UUID) |

#### GET /api/v1/medicos

| Aspecto | Detalle |
|---|---|
| Descripcion | Listar todos los medicos |
| Success | `200 OK` + `List<MedicoResponse>` |
| Errors | `500` |

#### GET /api/v1/medicos/{medicoId}

| Aspecto | Detalle |
|---|---|
| Descripcion | Consultar medico individual |
| Path Param | `medicoId` (UUID) |
| Success | `200 OK` + `MedicoResponse` |
| Errors | `404` (no encontrado), `400` (UUID invalido), `500` |

#### PUT /api/v1/medicos/{medicoId}

| Aspecto | Detalle |
|---|---|
| Descripcion | Actualizar datos de medico existente |
| Path Param | `medicoId` (UUID) |
| Request Body | `MedicoUpdateRequest` |
| Success | `200 OK` + `MedicoResponse` |
| Errors | `404` (no encontrado), `400` (validacion), `500` |
| Idempotencia | Idempotente |

### 6.3 Endpoints: Pacientes

#### POST /api/v1/pacientes

| Aspecto | Detalle |
|---|---|
| Descripcion | Registrar un nuevo paciente |
| Request Body | `PacienteRequest` |
| Success | `201 Created` + `Location: /api/v1/pacientes/{id}` + `PacienteResponse` |
| Errors | `400` (validacion), `409` (documento duplicado), `500` |
| Side Effects | Crea registro en BD |

#### GET /api/v1/pacientes

| Aspecto | Detalle |
|---|---|
| Descripcion | Listar todos los pacientes |
| Success | `200 OK` + `List<PacienteResponse>` |
| Errors | `500` (inesperado) |

#### GET /api/v1/pacientes/{pacienteId}

| Aspecto | Detalle |
|---|---|
| Descripcion | Consultar paciente individual |
| Path Param | `pacienteId` (UUID) |
| Success | `200 OK` + `PacienteResponse` |
| Errors | `404`, `400`, `500` |

#### PUT /api/v1/pacientes/{pacienteId}

| Aspecto | Detalle |
|---|---|
| Descripcion | Actualizar datos de paciente existente |
| Path Param | `pacienteId` (UUID) |
| Request Body | `PacienteUpdateRequest` |
| Success | `200 OK` + `PacienteResponse` |
| Errors | `404`, `400`, `409` (documento duplicado si cambia), `500` |

### 6.4 Endpoints: Citas

#### POST /api/v1/citas

| Aspecto | Detalle |
|---|---|
| Descripcion | Reservar una nueva cita |
| Request Body | `CitaRequest` |
| Success | `201 Created` + `Location: /api/v1/citas/{id}` + `CitaResponse` |
| Errors | `400` (validacion), `422` (RN-01/RN-03), `404` (paciente/medico inexistente), `409` (RN-02/RN-04/RN-05 bloqueo), `500` |
| Side Effects | Crea cita, posiblemente consulta festivos |
| Validaciones | RN-01, RN-02, RN-04, RN-05 (bloqueo), existencia de paciente y medico |

#### GET /api/v1/citas

| Aspecto | Detalle |
|---|---|
| Descripcion | Listar citas con filtros opcionales |
| Query Params | `medicoId` (UUID, optional), `pacienteId` (UUID, optional), `estado` (string, optional: PROGRAMADA/CANCELADA), `fecha_inicio` (date, optional), `fecha_fin` (date, optional) |
| Success | `200 OK` + `List<CitaResponse>` |
| Ordenamiento | `fecha_hora DESC` por defecto |
| Errors | `500` (inesperado) |

#### GET /api/v1/citas/{citaId}

| Aspecto | Detalle |
|---|---|
| Descripcion | Consultar cita individual |
| Path Param | `citaId` (UUID) |
| Success | `200 OK` + `CitaResponse` |
| Errors | `404`, `400`, `500` |

#### DELETE /api/v1/citas/{citaId}

| Aspecto | Detalle |
|---|---|
| Descripcion | Cancelar una cita (cambia estado a CANCELADA) |
| Path Param | `citaId` (UUID) |
| Success | `200 OK` + `CitaResponse` (con estado CANCELADA) |
| Errors | `404` (no encontrada), `409` (ya cancelada), `500` |
| Side Effects | Cambia estado, registra fecha_cancelacion, posiblemente crea penalizacion (RN-05) |
| Semantica | DELETE semantico (cancelacion logica, no borrado fisico) |

#### POST /api/v1/citas/{citaId}/reprogramar

| Aspecto | Detalle |
|---|---|
| Descripcion | Reprogramar una cita existente a un nuevo horario |
| Path Param | `citaId` (UUID) |
| Request Body | `ReprogramarRequest` (nueva fecha_hora) |
| Success | `200 OK` + `CitaResponse` (nueva cita PROGRAMADA) |
| Errors | `400` (validacion), `422` (RN-01: franja horaria invalida), `404` (cita/paciente/medico inexistente), `409` (cita ya cancelada, conflicto horario, bloqueo), `500` |
| Side Effects | Cancela cita original + crea nueva cita. Atomico en transaccion. |

### 6.5 Endpoints: Disponibilidad

#### GET /api/v1/disponibilidad

| Aspecto | Detalle |
|---|---|
| Descripcion | Consultar franjas horarias disponibles |
| Query Params | `medico_id` (UUID, required), `fecha_inicio` (date, required), `fecha_fin` (date, required) |
| Success | `200 OK` + `DisponibilidadResponse` |
| Errors | `400` (params invalidos, fecha_inicio > fecha_fin), `404` (medico inexistente), `500` |
| Logica | Genera franjas de 30 min en horario de atencion, excluye ocupadas, domingos y festivos |

---

## 7. Estrategia de Errores

### 7.1 ApiErrorResponse

```json
{
  "timestamp": "2026-07-17T10:30:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "code": "VALIDATION_ERROR",
  "message": "The request contains invalid fields.",
  "path": "/api/v1/medicos",
  "trace_id": "4f0f6d2c6b1d4c8a",
  "details": [
    {
      "field": "nombre_completo",
      "code": "FIELD_INVALID",
      "message": "must be between 3 and 100 characters",
      "rejected_value": "AB"
    }
  ]
}
```

### 7.2 Codigos de Error Estables

| HTTP Status | Code | Descripcion |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Bean Validation fallida |
| 400 | `INVALID_REQUEST_BODY` | JSON malformado |
| 422 | `INVALID_SLOT` | Franja horaria invalida (RN-01) |
| 422 | `INVALID_BIRTH_DATE` | Fecha de nacimiento futura (RN-03) |
| 400 | `INVALID_DATE_RANGE` | fecha_inicio > fecha_fin |
| 404 | `RESOURCE_NOT_FOUND` | Recurso inexistente |
| 404 | `MEDICO_NOT_FOUND` | Medico inexistente |
| 404 | `PACIENTE_NOT_FOUND` | Paciente inexistente |
| 404 | `CITA_NOT_FOUND` | Cita inexistente |
| 409 | `DOCUMENTO_DUPLICADO` | Documento de identidad ya registrado |
| 409 | `MEDICO_SLOT_CONFLICT` | Medico ya tiene cita en esa franja (RN-02) |
| 409 | `PACIENTE_SLOT_CONFLICT` | Paciente ya tiene cita en esa franja (RN-04) |
| 409 | `PACIENTE_BLOCKED` | Paciente bloqueado por penalizaciones (RN-05) |
| 409 | `CITA_ALREADY_CANCELLED` | Cita ya cancelada |
| 500 | `INTERNAL_ERROR` | Error inesperado |

### 7.3 GlobalExceptionHandler

Implementado en `infrastructure.web.GlobalExceptionHandler` con `@RestControllerAdvice`:

| Excepcion | Handler | HTTP Status |
|---|---|---|
| `MethodArgumentNotValidException` | handleValidation | 400 |
| `HttpMessageNotReadableException` | handleInvalidBody | 400 |
| `ResourceNotFoundException` | handleNotFound | 404 |
| `ConflictException` | handleConflict | 409 |
| `BusinessException` | handleBusiness | 422 |
| `Exception` (fallback) | handleUnexpected | 500 |

---

## 8. Integracion: Nager.Date API

### 8.1 Contrato

| Aspecto | Detalle |
|---|---|
| Endpoint | `GET https://date.nager.at/api/v3/PublicHolidays/{year}/CO` |
| Politica | Carga lazy/first-access por anio |
| Timeout | 5 segundos (conect + read) |
| Retry | 1 reintento con backoff de 1 segundo |

### 8.2 Flujo

1. El sistema necesita validar si una fecha es festivo.
2. Consulta `FestivoRepositoryPort.findByYear(year)`.
3. Si la tabla tiene registros para ese anio → usa datos de BD.
4. Si la tabla esta vacia para ese anio → llama a Nager.Date API.
5. Almacena los festivos obtenidos en BD.
6. Si la API falla y no hay datos en BD → log WARN y asume que no hay festivos para ese anio (fallback graceful).

### 8.3 Idempotency

- La carga de festivos es idempotente por la constraint UNIQUE(date, year, country_code).
- Si se intenta insertar un festivo duplicado, se ignora (INSERT ... ON CONFLICT DO NOTHING o validacion previa).

### 8.4 Observability

- Log INFO cuando se cargan festivos desde API: `"Holiday cache loaded from Nager.Date for year={year}, count={count}"`.
- Log WARN cuando API falla: `"Nager.Date API unavailable for year={year}, falling back to no-holiday assumption"`.

---

## 9. Convenciones

### 9.1 Naming

| Contexto | Convencion | Ejemplo |
|---|---|---|
| Base de datos | `snake_case` | `nombre_completo`, `fecha_hora` |
| Java (clases, metodos, variables) | `camelCase` / `PascalCase` | `nombreCompleto`, `fechaHora` |
| JSON API | `snake_case` | `"nombre_completo"`, `"fecha_hora"` |
| Enum values | `UPPER_SNAKE_CASE` | `PROGRAMADA`, `CANCELADA` |
| URL paths | `kebab-case` o plural | `/api/v1/medicos`, `/api/v1/citas` |

### 9.2 Configuracion Jackson

- `PropertyNamingStrategies.SNAKE_CASE` para serializacion/deserializacion.
- `JavaTimeModule` registrado para soporte de `LocalDate`, `LocalDateTime`, `OffsetDateTime`, `Instant`.
- Fechas serializadas como ISO 8601 strings (no timestamps numericos).

### 9.3 Configuracion de Perfiles

| Perfil | BD | Uso |
|---|---|---|
| `dev` (default) | PostgreSQL localhost:5432/medisalud | Desarrollo local |
| `test` | H2 in-memory (modo PostgreSQL) | Tests de integracion |

---

## 10. Estrategia de Tests

### 10.1 Piramide de Pruebas

| Tipo | Herramientas | Cobertura Objetivo |
|---|---|---|
| **Unit Tests** | JUnit 5, Mockito, AssertJ | Domain services, use cases, validators |
| **Integration Tests** | Spring Boot Test, MockMvc, DataJpaTest, H2 | Controllers, repositories, Flyway |
| **Architecture Tests** | ArchUnit | Verificar boundaries hexagonales |
| **Contract Tests** | MockMvc + OpenAPI | Status codes, error shape, schemas |

### 10.2 Cobertura JaCoCo

- **Umbral minimo:** 85% por archivo testable.
- **Exclusiones:**
  - `**/dto/**` (DTOs sin logica)
  - `**/config/**` (configuraciones Spring)
  - `**/entity/**` (entidades JPA sin logica)
  - `**/exception/**` (excepciones simples)
  - `*MapperImpl*` (MapStruct generado)
  - `**/AppointmentApplication.java` (bootstrap)

### 10.3 Tests de Arquitectura (ArchUnit)

- `domain` no depende de `infrastructure` ni de Spring.
- `domain` no usa anotaciones JPA ni Jackson.
- `application` no depende de `infrastructure`.
- Controllers residen en `infrastructure.web`.
- Use cases residen en `application.service`.
- Output ports residen en `application.port.output`.

### 10.4 Tests Obligatorios por Regla de Negocio

| Regla | Tests |
|---|---|
| RN-01 | Franja valida, franja en domingo, franja en festivo, franja fuera de horario, franja no alineada (:15) |
| RN-02 | Medico con conflicto, medico sin conflicto |
| RN-03 | Birth date futura, birth date valida, birth date null |
| RN-04 | Paciente con conflicto, paciente sin conflicto |
| RN-05 | Cancelacion <= 2h (penaliza), cancelacion > 2h (no penaliza), 3 penalizaciones (bloquea), 2 penalizaciones (permite) |
| RN-06 | Reprogramacion exitosa, reprogramacion de cita cancelada, reprogramacion con conflicto |

---

## 11. Datos Iniciales (Seed)

Migracion `V1.0.7__seed_medicos.sql` con tres medicos:

```sql
INSERT INTO medicos (id, nombre_completo, especialidad, telefono, email)
VALUES
  (gen_random_uuid(), 'Dra. Maria Gonzalez', 'Cardiologia', '555-1001', 'maria.gonzalez@medisalud.com'),
  (gen_random_uuid(), 'Dr. Carlos Ruiz', 'Pediatria', '555-1002', 'carlos.ruiz@medisalud.com'),
  (gen_random_uuid(), 'Dra. Ana Lopez', 'Dermatologia', '555-1003', 'ana.lopez@medisalud.com');
```

> **Nota:** Los UUIDs se generan con `gen_random_uuid()` de PostgreSQL para el seed. En runtime, la aplicacion genera UUIDs con `UUID.randomUUID()`.

---

## 12. Despliegue en AWS ECS Fargate

### 12.1 Arquitectura de Despliegue

```
                         ┌─────────────┐
                         │  Route 53    │
                         └──────┬──────┘
                                │
                         ┌──────▼──────┐
                         │  ALB (HTTPS)│
                         └──────┬──────┘
                                │
                    ┌───────────┴───────────┐
                    │    ECS Fargate         │
                    │  ms-medical-appointment│
                    └───────────┬───────────┘
                                │
                    ┌───────────▼───────────┐
                    │  RDS PostgreSQL        │
                    └───────────────────────┘
```

### 12.2 Componentes AWS

| Componente | Servicio | Detalle |
|---|---|---|
| Computo | ECS Fargate | Serverless, sin gestion de servidores |
| Base de datos | RDS PostgreSQL | db.t3.small, Multi-AZ opcional |
| Balanceador | Application Load Balancer | HTTPS con ACM (SSL gratuito) |
| Contenedor | ECR | Imagen Docker almacenada en Elastic Container Registry |
| Secretos | AWS Secrets Manager / SSM | Credenciales de BD y configuracion sensible |
| DNS | Route 53 | Dominio con certificado SSL |
| Logs | CloudWatch Logs | Logs estructurados en JSON |
| CI/CD | GitHub Actions | Build → Test → Push ECR → Deploy ECS |

### 12.3 Configuracion de Perfil

Crear archivo `application-aws.yaml` para el perfil `aws`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 20000
      max-lifetime: 1800000
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
logging:
  pattern:
    console: '{"timestamp": "%d{yyyy-MM-dd}T%d{HH:mm:ss.SSS}Z", "level": "%p", "message": "%m"}%n'
  level:
    com.medisalud: INFO
```

### 12.4 Dockerfile

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew dependencies
COPY src src
RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 12.5 Health Check

ECS necesita un health check para determinar si el contenedor esta vivo. Se configura en la Task Definition:

```json
{
  "healthCheck": {
    "command": ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"],
    "interval": 30,
    "timeout": 5,
    "retries": 3,
    "startPeriod": 60
  }
}
```

Se recomienda incluir `spring-boot-starter-actuator` en las dependencias de Gradle.

### 12.6 CI/CD Pipeline (GitHub Actions)

```yaml
name: Deploy to ECS
on:
  push:
    branches: [main]

jobs:
  test-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - run: ./gradlew build test jacocoTestReport
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1
      - name: Login to Amazon ECR
        id: ecr
        uses: aws-actions/amazon-ecr-login@v2
      - name: Build and push Docker image
        env:
          ECR_REGISTRY: ${{ steps.ecr.outputs.registry }}
          ECR_REPOSITORY: ms-medical-appointment
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
      - name: Deploy to ECS
        run: |
          aws ecs update-service --cluster medisalud-cluster \
            --service ms-medical-appointment-service \
            --force-new-deployment
```

### 12.7 Checklist Pre-Despliegue

- [ ] Docker build local: `docker build -t ms-medical-appointment .`
- [ ] Perfil aws funciona localmente con variables de entorno
- [ ] Flyway migraciones validas (spring.flyway.enabled=true)
- [ ] Health endpoint responde 200 OK
- [ ] No hay hardcode de credenciales en el codigo
- [ ] Secretos en AWS Secrets Manager / Parameter Store creados
- [ ] Security Group de ECS permite trafico desde ALB
- [ ] Security Group de RDS permite trafico desde ECS
- [ ] Logging estructurado en JSON para CloudWatch

### 12.8 Archivos Relacionados con el Despliegue

| Archivo | Proposito |
|---|---|
| `Dockerfile` | Build multi-stage de la imagen |
| `.dockerignore` | Excluir archivos innecesarios del build |
| `src/main/resources/application-aws.yaml` | Perfil de produccion para AWS |
| `.github/workflows/deploy.yml` | Pipeline CI/CD a ECS |

---

## 13. Decisiones Tecnicas

| # | Decision | Justificacion |
|---|---|---|
| D-01 | UUID v4 para todas las entidades | Descentralizacion, no expone secuencia, compatible con REST |
| D-02 | snake_case en JSON API | Convencion restful-standard y openapi-standard |
| D-03 | DELETE /citas/{id} para cancelacion | Semantica REST: el recurso "cita activa" se elimina (cambia a CANCELADA). Retorna body con estado actualizado. |
| D-04 | POST /citas/{id}/reprogramar como endpoint dedicado | Atomicidad: cancelar + crear en una transaccion. Evita estado inconsistente si el cliente olvida crear la nueva cita. |
| D-05 | GET /disponibilidad como recurso independiente | La disponibilidad no es un subrecurso de cita ni de medico; es una consulta derivada. |
| D-06 | Fallback graceful para Nager.Date | Si API falla y no hay cache, se asume sin festivos. Log WARN. No bloquea la operacion. |
| D-07 | Sin paginacion en esta version | El volumen de datos es bajo (contexto de prueba). Mejora propuesta. |
| D-08 | Sin autenticacion en esta version | No requerido en el enunciado. Mejora propuesta. |
| D-09 | Transaccion en reprogramacion | `@Transactional` en use case de reprogramacion garantiza atomicidad. |
| D-10 | Ordenamiento citas: fecha_hora DESC | Las citas mas recientes primero. Convencion para listados temporales. |
| D-11 | H2 modo PostgreSQL para tests | Compatibilidad con sintaxis PostgreSQL sin requerir Docker/infraestructura. |
| D-12 | OpenAPI Generator interfaceOnly=true | Genera interfaces de controllers y DTOs; la implementacion es manual. |
| D-13 | MapStruct para mapeo entre capas | Estandar java-stack para conversiones Entity <-> Domain y DTO <-> Command/Result. |
| D-14 | Virtual Threads habilitados | `spring.threads.virtual.enabled=true` para mejor throughput en I/O. |
| D-15 | Lombok para reducir boilerplate en DTOs, entidades JPA y builders | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` en DTOs y entidades de infraestructura. Domain NO usa Lombok (debe ser Java puro). |
| D-16 | CORS `*` (permitir todos los origenes) | MVP sin frontend definido. Cuando se implemente un frontend, debe restringirse al origen especifico mediante propiedades por perfil. |
| D-17 | Timezone UTC forzado via JVM | `-Duser.timezone=UTC` en el entrypoint del Dockerfile. Todos los `OffsetDateTime.now()` y `LocalDate.now()` en la aplicacion usan UTC. Decisión global para evitar inconsistencias entre capas y entornos. |
| D-18 | HTTP 422 (UNPROCESSABLE_ENTITY) para errores de reglas de negocio | El PDF de la prueba menciona 400 como codigo generico para errores de validacion de negocio, pero el estandar REST (RFC 4918, RFC 7231) define 422 como el codigo apropiado para errores semanticos (reglas de negocio), mientras que 400 corresponde a errores de sintaxis/malformacion. Se utiliza 422 para BusinessException y 409 para ConflictException. Ver documentacion de la decision en README. |
| D-19 | Estado ATENDIDA para citas | El PDF de la prueba incluye ATENDIDA como estado valido en el filtro de listado de citas (RF-06). Se agrega al enum de dominio, al schema OpenAPI y al constraint CHECK de la BD. No se implementa transicion automatica a ATENDIDA (no hay endpoint para marcarla); queda como mejora futura agregar esa transicion. |
| D-20 | Reprogramacion como cancel+create | La implementacion sigue el PDF (RN-06): cancelar cita original aplicando RN-05 si corresponde, validar nuevo horario con RN-01/RN-02/RN-04, bloquear paciente si excede limite de penalizaciones, y crear nueva cita con nuevo UUID. Operacion atomica en una sola transaccion. |

---

## 14. Criterios de Aceptacion del Incremento

### Funcionales

- [ ] CRUD parcial de medicos: crear, listar, consultar, actualizar.
- [ ] CRUD parcial de pacientes: crear, listar, consultar, actualizar.
- [ ] Reserva de citas con validacion de RN-01 a RN-05.
- [ ] Consulta de disponibilidad con exclusion de domingos, festivos y franjas ocupadas.
- [ ] Cancelacion de citas con penalizacion si aplica (RN-05).
- [ ] Reprogramacion atomica de citas (RN-06).
- [ ] Listado de citas con filtros combinables.
- [ ] Consulta de cita individual.
- [ ] Integracion con Nager.Date API (carga lazy/first-access).
- [ ] Tres medicos seed disponibles tras migracion.

### Tecnicos

- [ ] OpenAPI 3.1 valido y consistente con implementacion.
- [ ] Migraciones Flyway ejecutadas sin errores.
- [ ] JPA ddl-auto=validate pasa sin errores.
- [ ] GlobalExceptionHandler con ApiErrorResponse para todos los errores.
- [ ] JaCoCo >= 85% por archivo testable.
- [ ] ArchUnit: boundaries hexagonales verificados.
- [ ] Tests unitarios para todas las reglas de negocio.
- [ ] Tests de integracion para controllers y repositories.
- [ ] Perfil test con H2 funcional.

### No Funcionales

- [ ] Sin autenticacion (por diseno).
- [ ] Sin paginacion (por diseno).
- [ ] Sin eliminacion fisica de entidades.

---

## 15. Mejoras Propuestas (Fuera de Alcance)

Las siguientes mejoras estan fuera del alcance de este incremento pero fueron identificadas como valiosas. El despliegue en AWS ECS Fargate es parte de los entregables del proyecto (ver seccion 12) y no se lista aqui.

1. Paginacion en listados.
2. Autenticacion y autorizacion (JWT/Keycloak).
3. Notificaciones (email/SMS).
4. Calendario de festivos configurable.
5. Cancelacion masiva.
6. Logs de auditoria.
7. Internacionalizacion (i18n).
8. Pruebas E2E con frontend.
9. Frontend con React o Angular (interfaz de usuario para pacientes y administradores).
10. Restringir CORS por origen en produccion (D-16).

---

*Fin de la Master Spec.*
