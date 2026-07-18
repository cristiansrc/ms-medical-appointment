# SDD Shared Context — medical-appointment

> **Increment:** medical-appointment
> **Created:** 2026-07-17
> **Last updated:** 2026-07-17

---

## Current status

`validated-not-executed`

Plan aprobado por el usuario el 2026-07-18. Pendiente descomposicion de tareas por task-decomposer.

---

## Canonical artifacts

| # | Artifact | Absolute Path | Status |
|---|---|---|---|
| 1 | Requirements Brief | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/docs/specs/requirements/medical-appointment-requirements-brief.md` | `ready-for-planner` |
| 2 | Master Spec | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/docs/specs/master-spec.md` | `planning` |
| 3 | OpenAPI Contract | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/docs/api/openapi.yaml` | `revision-needed` |
| 4 | Shared Context | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/docs/specs/.working/medical-appointment-sdd-context.md` | `revision-needed` |
| 5 | build.gradle | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/build.gradle` | `validated-not-executed` |
| 6 | settings.gradle | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/settings.gradle` | `validated-not-executed` |
| 7 | application.yaml | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/application.yaml` | `validated-not-executed` |
| 8 | application-dev.yaml | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/application-dev.yaml` | `validated-not-executed` |
| 9 | application-test.yaml | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/application-test.yaml` | `validated-not-executed` |
| 10 | V1.0.1 medicos | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.1__create_medicos_table.sql` | `validated-not-executed` |
| 11 | V1.0.2 pacientes | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.2__create_pacientes_table.sql` | `validated-not-executed` |
| 12 | V1.0.3 citas | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.3__create_citas_table.sql` | `validated-not-executed` |
| 13 | V1.0.4 penalizaciones | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.4__create_penalizaciones_table.sql` | `validated-not-executed` |
| 14 | V1.0.5 festivos | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.5__create_festivos_table.sql` | `validated-not-executed` |
| 15 | V1.0.6 trigger updated_at | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.6__create_trigger_updated_at.sql` | `validated-not-executed` |
| 16 | V1.0.7 seed medicos | `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/src/main/resources/db/migration/V1.0.7__seed_medicos.sql` | `validated-not-executed` |

---

## Artifact evidence

| # | Artifact | Verified on disk | Content check | Status |
|---|---|---|---|---|
| 1 | Requirements Brief | yes | 680 lines, status `ready-for-planner` | `pass` |
| 2 | Master Spec | yes | written this session | `pass` |
| 3 | OpenAPI Contract | yes | written this session, 3.1.0 | `pass` |
| 4 | Shared Context | yes | this file | `pass` |
| 5 | build.gradle | yes | written this session | `pass` |
| 6 | settings.gradle | yes | written this session | `pass` |
| 7 | application.yaml | yes | written this session | `pass` |
| 8 | application-dev.yaml | yes | written this session | `pass` |
| 9 | application-test.yaml | yes | written this session | `pass` |
| 10-16 | Flyway migrations | yes | written this session | `pass` |

---

## Spec Validator Approval

verdict: ready
reviewed_at: 2026-07-18T12:00:00Z
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
summary: "Validacion de confirmacion post-correcciones 2026-07-18. F-01 (status awaiting-human-plan-approval), F-02 (422 en reprogramar), F-03 (NEW-F-01 resuelto) verificados con evidencia directa. 0 hallazgos abiertos. Spec lista para aprobacion humana."
invalidated_by_changes_since: none

---

## Human Plan Approval

approved_by_user: true
approved_at: 2026-07-18T12:00:00Z
approved_by: cristiansrc

---

## Decisions locked

| # | Decision | Rationale | Source |
|---|---|---|---|
| D-01 | UUID v4 para todas las entidades | Descentralizacion, no expone secuencia, compatible con REST | Master Spec S4.7 |
| D-02 | snake_case en JSON API | Convencion restful-standard y openapi-standard | Master Spec S9.1 |
| D-03 | DELETE /citas/{id} para cancelacion | Semantica REST: el recurso "cita activa" se elimina logicamente | Master Spec S6.4, OpenAPI |
| D-04 | POST /citas/{id}/reprogramar como endpoint dedicado | Atomicidad transaccional: cancelar + crear | Master Spec S6.4, OpenAPI |
| D-05 | GET /disponibilidad como recurso independiente | Consulta derivada, no subrecurso | Master Spec S6.5, OpenAPI |
| D-06 | Fallback graceful para Nager.Date | Si API falla y no hay cache, asume sin festivos. Log WARN. | Master Spec S8.2 |
| D-07 | Sin paginacion en esta version | Volumen bajo, mejora propuesta | Requirements Brief P-07 |
| D-08 | Sin autenticacion en esta version | No requerido en enunciado | Requirements Brief P-01 |
| D-09 | Transaccion atomica en reprogramacion | @Transactional en use case | Master Spec S5 RN-06 |
| D-10 | Ordenamiento citas: fecha_hora DESC | Citas mas recientes primero | Master Spec S6.1 |
| D-11 | H2 modo PostgreSQL para tests | Compatibilidad sin Docker | Master Spec S9.3 |
| D-12 | OpenAPI Generator interfaceOnly=true | Genera interfaces y DTOs; implementacion manual | Master Spec S2 |
| D-13 | MapStruct para mapeo entre capas | Estandar java-stack | Master Spec S2 |
| D-14 | Virtual Threads habilitados | Mejor throughput en I/O | Master Spec S2 |
| D-15 | snake_case en BD, camelCase en Java | Convencion postgresql-standard y Java | Master Spec S9.1 |
| D-16 | Flyway como unico dueno del schema | ddl-auto=validate | Master Spec S2 |
| D-17 | JaCoCo 85% por archivo testable | Estandar testing-strategy | Master Spec S10.2 |
| D-18 | ArchUnit para boundaries hexagonales | Verificar que domain no depende de infrastructure | Master Spec S10.3 |
| D-19 | Ventana penalizaciones: 30 dias desde fecha de nueva reserva | Interpretacion mas natural de "ultimos 30 dias" | Requirements Brief S-10 |
| D-20 | Umbral penalizacion: <= 2h inclusivo | Resuelto en requirements brief P-06 | Requirements Brief P-06 |

---

## Validator findings

| # | Severity | Section | Description | Status |
|---|---|---|---|---|

---

## Resolved findings

| # | Severity | Section | Description | Resolution |
|---|---|---|---|---|
| F-01 | medium | Master Spec S7.2 vs S8.2 | Contradiccion fallback graceful vs codigo 503 HOLIDAY_SERVICE_UNAVAILABLE | Eliminado codigo 503 y excepcion. Prevalece fallback graceful. |
| F-02 | medium | Master Spec S7.2, S5 | 400 vs 422 para violaciones de RN (INVALID_SLOT, INVALID_BIRTH_DATE) | Cambiado a 422 UNPROCESSABLE_ENTITY para reglas de negocio. |
| F-03 | medium | Master Spec S3.3 vs S7.3 | HolidayServiceUnavailableException faltante en estructura de paquetes | Resuelto con F-01 (excepcion eliminada). |
| F-04 | low | Master Spec S6.3, S6.4 | GET /pacientes y GET /citas sin codigos de error | Agregado Errors: 500 a ambos endpoints. |
| F-05 | low | Master Spec S4.4, S4.6 | Indices no documentados | Agregados idx_citas_estado, idx_festivos_year, idx_festivos_date. |
| F-06 | low | Master Spec S2 vs build.gradle | Version MapStruct mismatch | Actualizado a 1.6.x en Master Spec. |
| F-07 | low | OpenAPI schemas | nullable: true deprecado en 3.1 | Reemplazado por type: [string, 'null'] en 13 campos. |
| NEW-F-01 | high | Master Spec S6.4, OpenAPI POST /citas | 422 no propagado a descripcion de endpoint POST /citas | Separados 400 (validacion) y 422 (RN-01/RN-03) en S6.4 y OpenAPI. |

---

## Open questions

| # | Question | Impact | Status |
|---|---|---|---|
| OQ-01 | Ninguna abierta actualmente | - | resolved |

---

## Stale terms guard

| Term | Expected | Verified |
|---|---|---|
| EstadoCita values | PROGRAMADA, CANCELADA | yes (OpenAPI + Master Spec + migrations) |
| API base path | /api/v1 | yes (OpenAPI + Master Spec) |
| JSON naming | snake_case | yes (OpenAPI schemas + Master Spec) |
| DB naming | snake_case | yes (migrations) |
| ID type | UUID v4 | yes (migrations + OpenAPI + Master Spec) |
| Error schema | ApiErrorResponse + ApiErrorDetail | yes (OpenAPI + Master Spec) |
| Flyway location | classpath:db/migration | yes (application.yaml + build.gradle) |
| JPA ddl-auto | validate | yes (application.yaml) |

---

## Next action

`Execution` — Task board creado en `docs/specs/tasks/medical-appointment-task-board.md` con 37 tareas atomicas. Listo para que executor comience con T1.
