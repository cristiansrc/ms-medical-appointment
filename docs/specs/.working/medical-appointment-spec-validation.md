# Spec Validation Report — medical-appointment

> **Validator:** spec-validator
> **Date:** 2026-07-17
> **Increment:** medical-appointment
> **Verdict:** `ready`

---

## 1. Checklist de Validacion

### A. Consistencia Cross-Artifact

| # | Item | Status | Evidence |
|---|---|---|---|
| A1 | RF del requirements brief mapeados 1:1 en Master Spec | PASS | RF-01 a RF-06 cubiertos en Master Spec S6.2-S6.5 |
| A2 | RN del requirements brief mapeadas 1:1 en Master Spec | PASS | RN-01 a RN-06 cubiertas en Master Spec S5 |
| A3 | Endpoints del OpenAPI coinciden con Master Spec | PASS | 14 endpoints en ambos artefactos, paths y metodos identicos |
| A4 | Schemas del OpenAPI coinciden con entidades de Master Spec | PASS | Campos, tipos, nullability y required alineados |
| A5 | Migraciones Flyway crean tablas que Master Spec describe | PASS | 7 migraciones V1.0.1-V1.0.7, todas las tablas presentes |
| A6 | Campos de migraciones coinciden con schemas OpenAPI | PASS | Columnas DB mapean a propiedades JSON snake_case |
| A7 | Status codes del OpenAPI coinciden con Master Spec | PASS | OpenAPI es superconjunto consistente de Master Spec |
| A8 | ApiErrorResponse consistente entre OpenAPI y Master Spec | PASS | 8 campos obligatorios identicos en ambos artefactos |

### B. OpenAPI Contract

| # | Item | Status | Evidence |
|---|---|---|---|
| B1 | OpenAPI 3.1 valido | PASS | Line 1: `openapi: 3.1.0` |
| B2 | operationId unico por endpoint | PASS | 14 operationIds unicos verificados |
| B3 | tags, summary, description en todos los endpoints | PASS | Todos los endpoints tienen los tres campos |
| B4 | Schemas con restricciones (required, minLength, maxLength, format, enum) | PASS | MedicoRequest, PacienteRequest, CitaRequest con constraints |
| B5 | ApiErrorResponse con 8 campos obligatorios | PASS | timestamp, status, error, code, message, path, trace_id, details |
| B6 | ApiErrorDetail con field, code, message, rejected_value | PASS | Lineas 1027-1047 |
| B7 | snake_case en atributos JSON | PASS | Todos los schemas usan snake_case consistentemente |
| B8 | Respuestas 4xx/5xx referencian $ref al schema de error | PASS | Todas las respuestas de error usan $ref ApiErrorResponse o InternalServerError |
| B9 | Ejemplos representativos en request/responses | PASS | POST medicos, pacientes, citas, reprogramar tienen ejemplos |
| B10 | POST /medicos → 201 Created con Location header | PASS | Lineas 45-52 |
| B11 | POST /citas → 201, 409, 404 | PASS | Lineas 317-348 |
| B12 | DELETE /citas/{id} → 200 (con body), 404, 409 | PASS | Lineas 451-470 |
| B13 | GET /disponibilidad → 200 OK | PASS | Lineas 553-558 |
| B14 | GET /citas con filtros query params opcionales | PASS | Lineas 357-391, 5 filtros opcionales |

### C. Master Spec

| # | Item | Status | Evidence |
|---|---|---|---|
| C1 | Arquitectura Hexagonal descrita | PASS | Section 3 con diagrama ASCII y paquetes |
| C2 | Modelo de datos completo | PASS | Section 4: 5 tablas + relaciones + politica IDs |
| C3 | Relaciones, tipos, constraints documentados | PASS | Tablas S4.2-S4.6 con tipos, nullable, FK, CHECK, UNIQUE |
| C4 | RNs con estrategia de implementacion | PASS | Section 5: 6 RNs con strategy, error code y descripcion |
| C5 | Estrategia de errores definida | PASS | Section 7: ApiErrorResponse + 15 codigos estables + handler table |
| C6 | Integracion Nager.Date con politica de carga | PASS | Section 8: lazy/first-access, timeout, retry, fallback |
| C7 | Estrategia de testing definida | PASS | Section 10: piramide, JaCoCo, ArchUnit, tests por RN |
| C8 | JaCoCo 85% por archivo testable | PASS | Section 10.2 + build.gradle lines 135-157 |
| C9 | Decisiones tecnicas documentadas | PASS | Section 12: 14 decisiones con justificacion |

### D. Migraciones Flyway

| # | Item | Status | Evidence |
|---|---|---|---|
| D1 | Nombres versionados correctamente | PASS | V1.0.1 a V1.0.7 con formato `V<VERSION>__<desc>.sql` |
| D2 | Ubicacion estandar | PASS | `src/main/resources/db/migration/` |
| D3 | Tabla medicos completa | PASS | V1.0.1: UUID PK, nombre_completo, especialidad, telefono, email, created_at, updated_at |
| D4 | Tabla pacientes completa | PASS | V1.0.2: UUID PK, nombre_completo, documento_identidad UNIQUE, telefono, email, birth_date, timestamps |
| D5 | Tabla citas completa | PASS | V1.0.3: UUID PK, FKs, fecha_hora, estado CHECK, motivo_cancelacion, fecha_cancelacion, 3 indices |
| D6 | Tabla penalizaciones completa | PASS | V1.0.4: UUID PK, FKs, fecha_hora, indice compuesto |
| D7 | Tabla festivos completa | PASS | V1.0.5: UUID PK, todos los campos Nager.Date, UNIQUE(date,year,country_code), 2 indices |
| D8 | Trigger updated_at | PASS | V1.0.6: funcion + triggers en medicos, pacientes, citas, festivos |
| D9 | Seed de 3 medicos | PASS | V1.0.7: 3 INSERTs con gen_random_uuid() |

### E. Configuracion

| # | Item | Status | Evidence |
|---|---|---|---|
| E1 | build.gradle: openapi-generator interfaceOnly=true | PASS | Line 5 plugin, line 73 config |
| E2 | build.gradle: jacoco threshold 85% | PASS | Line 6 plugin, lines 135-157 verification |
| E3 | build.gradle: flyway plugin | PASS | Line 7 plugin v10.22.0 |
| E4 | build.gradle: dependencias completas | PASS | web, data-jpa, validation, postgresql, h2, lombok, mapstruct, flyway, springdoc, archunit |
| E5 | application.yaml: ddl-auto=validate, open-in-view=false, flyway enabled | PASS | Lines 17, 19, 24 |
| E6 | application-dev.yaml: PostgreSQL config | PASS | Lines 2-6: jdbc:postgresql://localhost:5432/medisalud |
| E7 | application-test.yaml: H2 modo PostgreSQL, flyway enabled | PASS | Line 3: MODE=PostgreSQL, line 12: flyway enabled |

### F. SDD Shared Context

| # | Item | Status | Evidence |
|---|---|---|---|
| F1 | Status: planning | PASS | Line 11: `planning` |
| F2 | Canonical artifacts con rutas absolutas | PASS | Lines 19-36: 16 artifacts |
| F3 | Decisions locked documentadas | PASS | Lines 78-99: 20 decisiones |
| F4 | Spec Validator Approval: pendiente | PASS | Lines 57-66: verdict pending |
| F5 | Human Plan Approval: pendiente | PASS | Lines 70-73 |
| F6 | Next action definido | PASS | Lines 138-140: spec-validator review |
| F7 | Headings obligatorios presentes y sin duplicar | PASS | 10 headings requeridos verificados |

### G. Gates SDD

| # | Item | Status | Evidence |
|---|---|---|---|
| G1 | Sin afirmaciones contradictorias de lifecycle | PASS | Master Spec `planning`, Shared Context `planning` |
| G2 | OpenAPI sin drift contra Master Spec | PASS | Endpoints, schemas, status codes alineados |
| G3 | Migraciones existen y son consistentes con Master Spec | PASS | 7 archivos verificados en disco |

---

## 2. Hallazgos Detallados

### Finding F-01 — Contradiccion fallback graceful vs codigo 503

- **Severidad:** `medium`
- **Seccion afectada:** Master Spec S7.2 (codigo `HOLIDAY_SERVICE_UNAVAILABLE` → 503) vs S8.2 (fallback graceful: "asume que no hay festivos")
- **Descripcion:** La Master Spec define dos comportamientos contradictorios para fallos de Nager.Date: (1) S8.2 dice "si la API falla y no hay datos en BD → log WARN y asume que no hay festivos", lo cual es un fallback graceful que nunca generaria un error al cliente; (2) S7.2 define el codigo `HOLIDAY_SERVICE_UNAVAILABLE` con HTTP 503, y S7.3 lista `HolidayServiceUnavailableException` en el GlobalExceptionHandler. Si el fallback siempre aplica, el codigo 503 y la excepcion son codigo muerto. Si 503 puede ocurrir en algun escenario, el OpenAPI debe declararlo en los endpoints afectados (GET /disponibilidad, POST /citas).
- **Cambio requerido:** Planner debe clarificar: (a) si el fallback graceful es absoluto → eliminar `HOLIDAY_SERVICE_UNAVAILABLE` de S7.2 y `HolidayServiceUnavailableException` de S7.3; o (b) si existen escenarios donde se retorna 503 → documentar cuales y agregar respuesta 503 en OpenAPI para los endpoints afectados.
- **Executor risk:** Executor no sabria si implementar la excepcion 503 o solo el fallback graceful. Podria implementar ambos de forma inconsistente.

### Finding F-02 — 400 vs 422 para violaciones de reglas de negocio

- **Severidad:** `medium`
- **Seccion afectada:** Master Spec S7.2, S5 (RN-01 → `INVALID_SLOT` 400, RN-03 → `INVALID_BIRTH_DATE` 400)
- **Descripcion:** Los codigos `INVALID_SLOT` (RN-01) e `INVALID_BIRTH_DATE` (RN-03) estan mapeados a HTTP 400. Sin embargo, las skills `restful-standard` y `springboot-java-rest-error-response-standards` definen: "400 = request mal formado, tipos invalidos, JSON invalido o Bean Validation de entrada" y "422 = regla de negocio invalida con request sintacticamente correcto". Las RN-01 y RN-03 son reglas de negocio con requests sintacticamente correctos (JSON valido, tipos validos). El requirements brief S9.3 agrupa todo bajo 400, pero el Planner deberia confirmar si sigue el brief o la skill.
- **Cambio requerido:** Planner debe decidir explicitamente: (a) mantener 400 para INVALID_SLOT e INVALID_BIRTH_DATE (siguiendo el requirements brief), documentando la justificacion en shared context; o (b) cambiar a 422 (siguiendo las skills de REST).
- **Executor risk:** Executor implementaria 400 segun la spec, pero un reviewer podria marcarlo como drift arquitectonico contra las skills activas.

### Finding F-03 — HolidayServiceUnavailableException faltante en estructura de paquetes

- **Severidad:** `medium`
- **Seccion afectada:** Master Spec S3.3 (domain.exception) vs S7.3 (GlobalExceptionHandler)
- **Descripcion:** La seccion S7.3 lista `HolidayServiceUnavailableException` en la tabla del GlobalExceptionHandler, pero esta excepcion no aparece en la estructura de paquetes de S3.3 (`domain.exception/`). Las excepciones listadas en S3.3 son: BusinessException, ResourceNotFoundException, ConflictException, SlotNotAvailableException, PatientConflictException, PatientBlockedException, InvalidSlotException.
- **Cambio requerido:** Agregar `HolidayServiceUnavailableException.java` a la lista de domain.exception en S3.3, o moverla a `infrastructure.client.exception` si es una excepcion tecnica de infraestructura (mas apropiado segun hexagonal-architecture, ya que es un fallo de cliente HTTP externo).
- **Executor risk:** Executor no sabria donde crear la clase de excepcion. Podria crearla en domain violando hexagonal-architecture (un fallo de cliente externo no es dominio puro).

### Finding F-04 — Master Spec omite codigos de error en GET /pacientes y GET /citas

- **Severidad:** `low`
- **Seccion afectada:** Master Spec S6.3 (GET /api/v1/pacientes) y S6.4 (GET /api/v1/citas)
- **Descripcion:** La Master Spec no lista codigos de error para GET /api/v1/pacientes (solo "200 OK") y GET /api/v1/citas (solo "200 OK"). El OpenAPI correctamente incluye 500 para GET /pacientes y 400+500 para GET /citas. La Master Spec deberia ser consistente con el OpenAPI.
- **Cambio requerido:** Agregar los codigos de error correspondientes en las tablas de Master Spec S6.3 y S6.4 para estos endpoints, alineando con OpenAPI.
- **Executor risk:** Bajo. Executor seguira el OpenAPI como fuente de verdad para el contrato.

### Finding F-05 — Indices no documentados en Master Spec

- **Severidad:** `low`
- **Seccion afectada:** Master Spec S4.4 (citas) y S4.6 (festivos)
- **Descripcion:** La migracion V1.0.3 incluye el indice `idx_citas_estado ON citas (estado)` que no esta documentado en Master Spec S4.4 (solo lista `idx_citas_medico_fecha` e `idx_citas_paciente_fecha`). La migracion V1.0.5 incluye `idx_festivos_year` e `idx_festivos_date` que no estan documentados en Master Spec S4.6.
- **Cambio requerido:** Agregar los indices faltantes a las secciones S4.4 y S4.6 de la Master Spec para mantener consistencia con las migraciones.
- **Executor risk:** Bajo. Los indices ya existen en las migraciones. Solo es un gap de documentacion.

### Finding F-06 — Version MapStruct: spec dice 1.5.x, build.gradle usa 1.6.3

- **Severidad:** `low`
- **Seccion afectada:** Master Spec S2 (tabla de stack) vs build.gradle line 48
- **Descripcion:** La Master Spec S2 lista "MapStruct | 1.5.x" pero el build.gradle usa `org.mapstruct:mapstruct:1.6.3`. La version 1.6.x es retrocompatible pero la spec deberia reflejar la version real.
- **Cambio requerido:** Actualizar Master Spec S2 para decir "MapStruct | 1.6.x" o la version exacta "1.6.3".
- **Executor risk:** Minimo. La version real esta en build.gradle que es la fuente de verdad para dependencias.

### Finding F-07 — OpenAPI usa `nullable: true` (deprecado en 3.1)

- **Severidad:** `low`
- **Seccion afectada:** OpenAPI schemas (MedicoRequest.telefono, MedicoRequest.email, PacienteRequest.birth_date, etc.)
- **Descripcion:** OpenAPI 3.1 deprecó `nullable: true` en favor de `type: [string, null]`. Sin embargo, el OpenAPI Generator 7.x y springdoc todian soportan `nullable: true`. El config `openApiNullable: false` indica que no se generaran wrappers Nullable.
- **Cambio requerido:** No es blocker. Si se desea migrar a sintaxis 3.1 pura, cambiar `nullable: true` a `type: [string, null]` en los schemas afectados.
- **Executor risk:** Minimo. El tooling soporta la sintaxis actual.

---

## 3. Veredicto Final

**`ready`**

No se encontraron findings de severidad `blocker` ni `high`. Los 3 findings `medium` son decisiones de disenio que el Planner debe clarificar pero no impiden que el Executor implemente correctamente:

- F-01: El fallback graceful (S8.2) es el comportamiento primario. El Executor puede implementar el fallback y omitir la excepcion 503 hasta que Planner clarifique.
- F-02: El Executor seguira los codigos de la Master Spec (400 para INVALID_SLOT e INVALID_BIRTH_DATE). Si Planner decide cambiar a 422, sera un cambio menor pre-descomposicion.
- F-03: El Executor creara la excepcion donde sea mas apropiado (probablemente infrastructure.client.exception). La clarificacion de Planner solo confirmara la ubicacion.

Los 4 findings `low` son gaps de documentacion que no afectan la implementacion.

---

## 4. Spec Validator Approval

```
verdict: ready
reviewed_at: 2026-07-17T12:00:00Z
validator_agent: spec-validator
artifact_set_reviewed:
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/docs/specs/master-spec.md
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/docs/api/openapi.yaml
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/docs/specs/.working/medical-appointment-sdd-context.md
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/build.gradle
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/settings.gradle
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/application.yaml
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/application-dev.yaml
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/application-test.yaml
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.1__create_medicos_table.sql
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.2__create_pacientes_table.sql
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.3__create_citas_table.sql
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.4__create_penalizaciones_table.sql
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.5__create_festivos_table.sql
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.6__create_trigger_updated_at.sql
  - /home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.7__seed_medicos.sql
summary: "Spec validada contra 15 artefactos en disco. 14 endpoints OpenAPI consistentes con Master Spec. 7 migraciones Flyway alineadas con modelo de datos. 6 reglas de negocio con estrategia de implementacion. 3 findings medium (contradiccion fallback/503, 400 vs 422 para RN, excepcion faltante en paquetes) y 4 findings low (indices no documentados, version MapStruct, nullable 3.1, errores omitidos en GET endpoints). Sin blockers."
invalidated_by_changes_since: none
```
