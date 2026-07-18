# Spec Re-Validation Report — medical-appointment

> **Validator:** spec-validator
> **Date:** 2026-07-17
> **Increment:** medical-appointment
> **Re-validation type:** Post-correction verification (7 findings from previous validation)
> **Verdict:** `not ready`

---

## 1. Checklist de Correcciones

### F-01: Contradiccion fallback/503

| # | Item | Status | Evidence |
|---|---|---|---|
| F-01a | NO existe codigo 503 `HOLIDAY_SERVICE_UNAVAILABLE` en Master Spec S7.2 | **PASS** | Master Spec lines 608-624: tabla de codigos no contiene 503 ni HOLIDAY_SERVICE_UNAVAILABLE. Ultima entrada: `500 \| INTERNAL_ERROR`. |
| F-01b | NO existe handler para `HolidayServiceUnavailableException` en Master Spec S7.3 | **PASS** | Master Spec lines 626-638: tabla de handlers lista 6 entradas (MethodArgumentNotValidException, HttpMessageNotReadableException, ResourceNotFoundException, ConflictException, BusinessException, Exception fallback). Ninguna es HolidayServiceUnavailableException. |
| F-01c | S8.2 describe fallback graceful (log WARN, asumir sin festivos) | **PASS** | Master Spec line 659, step 6: "Si la API falla y no hay datos en BD → log WARN y asume que no hay festivos para ese anio (fallback graceful)." Decision D-06 (line 769) confirma. |

**F-01 verdict: PASS — Correcion completa.**

---

### F-02: 400 vs 422 para RN

| # | Item | Status | Evidence |
|---|---|---|---|
| F-02a | `INVALID_SLOT` (RN-01) usa codigo 422 en S7.2 | **PASS** | Master Spec line 612: `\| 422 \| INVALID_SLOT \| Franja horaria invalida (RN-01) \|` |
| F-02b | `INVALID_BIRTH_DATE` (RN-03) usa codigo 422 en S7.2 | **PASS** | Master Spec line 613: `\| 422 \| INVALID_BIRTH_DATE \| Fecha de nacimiento futura (RN-03) \|` |
| F-02c | `BusinessException` handler usa 422 en S7.3 | **PASS** | Master Spec line 636: `\| BusinessException \| handleBusiness \| 422 \|` |
| F-02d | RN-01 error description dice 422 UNPROCESSABLE_ENTITY | **PASS** | Master Spec line 373: `\| **Error** \| InvalidSlotException → 422 UNPROCESSABLE_ENTITY, code: INVALID_SLOT \|` |
| F-02e | RN-03 error description dice 422 UNPROCESSABLE_ENTITY | **PASS** | Master Spec line 389: `\| **Error** \| BusinessException → 422 UNPROCESSABLE_ENTITY, code: INVALID_BIRTH_DATE \|` |
| F-02f | **NEW — S6.4 POST /citas Errors consistente con S7.2** | **FAIL** | Master Spec line 523: `\| Errors \| \`400\` (validacion/RN-01/RN-03), ...` — Sigue diciendo 400 para RN-01/RN-03, contradice S7.2 (422). |
| F-02g | **NEW — OpenAPI POST /citas declara 422 response** | **FAIL** | OpenAPI line 330: `description: Error de validacion o regla de negocio (RN-01, RN-03)` bajo respuesta `'400'`. No existe respuesta `'422'` declarada para POST /api/v1/citas. |

**F-02 verdict: PARTIAL PASS — Correccion incompleta. S7.2, S5 (RN-01/RN-03) y S7.3 fueron corregidos a 422, pero S6.4 (descripcion de endpoint POST /citas) y OpenAPI aun referencian 400 para las mismas reglas de negocio.**

---

### F-03: HolidayServiceUnavailableException

| # | Item | Status | Evidence |
|---|---|---|---|
| F-03a | Excepcion eliminada con F-01 | **PASS** | Master Spec S3.3 (lines 130-137): domain.exception lista 7 excepciones, ninguna es HolidayServiceUnavailableException. S7.3 (lines 626-638): handler table no la menciona. |

**F-03 verdict: PASS — Resuelto con F-01.**

---

### F-04: GET endpoints sin errores

| # | Item | Status | Evidence |
|---|---|---|---|
| F-04a | GET /api/v1/pacientes tiene `Errors: 500 (inesperado)` | **PASS** | Master Spec line 493: `\| Errors \| \`500\` (inesperado) \|` |
| F-04b | GET /api/v1/citas tiene `Errors: 500 (inesperado)` | **PASS** | Master Spec line 535: `\| Errors \| \`500\` (inesperado) \|` |

**F-04 verdict: PASS — Correcion completa.**

---

### F-05: Indices no documentados

| # | Item | Status | Evidence |
|---|---|---|---|
| F-05a | `idx_citas_estado` documentado en S4.4 | **PASS** | Master Spec line 317: `- \`idx_citas_estado\` ON (estado)`. Migracion V1.0.3 line 25: `CREATE INDEX idx_citas_estado ON citas (estado);` |
| F-05b | `idx_festivos_year` documentado en S4.6 | **PASS** | Master Spec line 352: `- \`idx_festivos_year\` ON (year)`. Migracion V1.0.5 line 19: `CREATE INDEX idx_festivos_year ON festivos (year);` |
| F-05c | `idx_festivos_date` documentado en S4.6 | **PASS** | Master Spec line 353: `- \`idx_festivos_date\` ON (date)`. Migracion V1.0.5 line 22: `CREATE INDEX idx_festivos_date ON festivos (date);` |

**F-05 verdict: PASS — Correcion completa.**

---

### F-06: Version MapStruct

| # | Item | Status | Evidence |
|---|---|---|---|
| F-06a | Master Spec S2 dice MapStruct 1.6.x | **PASS** | Master Spec line 39: `\| Mapping \| MapStruct \| 1.6.x \|`. build.gradle line 48: `implementation 'org.mapstruct:mapstruct:1.6.3'`. Consistentes. |

**F-06 verdict: PASS — Correcion completa.**

---

### F-07: nullable: true en OpenAPI 3.1

| # | Item | Status | Evidence |
|---|---|---|---|
| F-07a | NO existen ocurrencias de `nullable: true` en openapi.yaml | **PASS** | grep `nullable:\s*true` en openapi.yaml: 0 resultados. |
| F-07b | Campos opcionales usan `type: [string, 'null']` | **PASS** | Verificado en 13 campos: MedicoRequest.telefono (L625), MedicoRequest.email (L631), MedicoUpdateRequest.telefono (L656), MedicoUpdateRequest.email (L662), MedicoResponse.telefono (L691), MedicoResponse.email (L695), PacienteRequest.birth_date (L744), PacienteUpdateRequest.birth_date (L782), PacienteResponse.birth_date (L820), CitaResponse.motivo_cancelacion (L905), CitaResponse.fecha_cancelacion (L909), ApiErrorDetail.field (L1020), ApiErrorDetail.rejected_value (L1032). |

**F-07 verdict: PASS — Correcion completa.**

---

## 2. Resumen del Checklist

| Finding | Descripcion | Verdict |
|---|---|---|
| F-01 | Contradiccion fallback/503 | **PASS** |
| F-02 | 400 vs 422 para RN | **PARTIAL PASS** (correcion incompleta) |
| F-03 | HolidayServiceUnavailableException | **PASS** (resuelto con F-01) |
| F-04 | GET endpoints sin errores | **PASS** |
| F-05 | Indices no documentados | **PASS** |
| F-06 | Version MapStruct | **PASS** |
| F-07 | nullable: true en OpenAPI 3.1 | **PASS** |

**Correcciones completas: 6/7. Correcciones parciales: 1/7.**

---

## 3. Nuevos Hallazgos

### NEW-F-01 — Desincronizacion S6.4 vs S7.2: POST /citas dice 400 para RN-01/RN-03 pero S7.2 dice 422

- **Severidad:** `high`
- **Seccion afectada:** Master Spec S6.4 (line 523) vs Master Spec S7.2 (lines 612-613) y OpenAPI (line 330)
- **Descripcion:** La correccion de F-02 actualizo correctamente las secciones S5 (RN-01 line 373, RN-03 line 389), S7.2 (lines 612-613) y S7.3 (line 636) para usar 422 UNPROCESSABLE_ENTITY. Sin embargo, la descripcion del endpoint POST /api/v1/citas en S6.4 (line 523) aun dice:

  ```
  | Errors | `400` (validacion/RN-01/RN-03), `404` (...), `409` (...), `500` |
  ```

  Esto contradice directamente la tabla de codigos de error S7.2 que define INVALID_SLOT → 422 e INVALID_BIRTH_DATE → 422. Adicionalmente, el OpenAPI (line 330) bajo la respuesta `'400'` de POST /api/v1/citas dice:

  ```yaml
  description: Error de validacion o regla de negocio (RN-01, RN-03)
  ```

  Y no existe una respuesta `'422'` declarada para este endpoint en OpenAPI.

- **Evidencia exacta:**
  - Master Spec line 523: `| Errors | \`400\` (validacion/RN-01/RN-03), \`404\` (paciente/medico inexistente), \`409\` (RN-02/RN-04/RN-05 bloqueo), \`500\` |`
  - Master Spec line 612: `| 422 | \`INVALID_SLOT\` | Franja horaria invalida (RN-01) |`
  - Master Spec line 613: `| 422 | \`INVALID_BIRTH_DATE\` | Fecha de nacimiento futura (RN-03) |`
  - OpenAPI line 330: `description: Error de validacion o regla de negocio (RN-01, RN-03)` (bajo `'400'`)
  - OpenAPI lines 329-334: Respuesta `'400'` es la unica que menciona RN-01/RN-03; no existe respuesta `'422'`
- **Cambio requerido:**
  1. Master Spec S6.4 POST /citas: Cambiar `Errors` a: `\`400\` (validacion), \`422\` (RN-01/RN-03), \`404\` (paciente/medico inexistente), \`409\` (RN-02/RN-04/RN-05 bloqueo), \`500\``
  2. OpenAPI POST /api/v1/citas: (a) Cambiar descripcion de `'400'` a `description: Error de validacion de entrada (Bean Validation, JSON malformado)`. (b) Agregar respuesta `'422'` con `$ref: '#/components/schemas/ApiErrorResponse'` y descripcion `description: Regla de negocio invalida (RN-01: franja horaria, RN-03: fecha de nacimiento futura)`.
- **Executor risk:** ALTO. El Executor lee S6.4 para conocer los errores del endpoint y OpenAPI para implementar el contrato. Si sigue S6.4/OpenAPI, implementa 400 para INVALID_SLOT e INVALID_BIRTH_DATE, contradiciendo S7.2. Si sigue S7.2, el OpenAPI generado no tendra respuesta 422 y el contrato sera inconsistente con la implementacion. Either way, habra un bug de status code incorrecto o un drift entre OpenAPI y runtime.

---

## 4. Validacion General Adicional

| # | Item | Status | Evidence |
|---|---|---|---|
| V1 | Endpoints del OpenAPI coinciden con Master Spec | **PASS** | 14 endpoints verificados: paths, metodos HTTP y operationIds identicos. |
| V2 | Schemas de error consistentes (ApiErrorResponse + ApiErrorDetail) | **PASS** | 8 campos obligatorios en ApiErrorResponse, 4 campos en ApiErrorDetail, identicos en Master Spec S7.1 y OpenAPI lines 969-1034. |
| V3 | Shared context refleja findings como resueltos | **PASS** | Shared context lines 118-132: "Todos los hallazgos han sido resueltos" con tabla de 7 resolved findings. |
| V4 | Shared context tiene `awaiting-human-plan-approval` como status | **PASS** | Shared context line 11: `\`awaiting-human-plan-approval\`` |
| V5 | Master Spec status consistente | **PASS** | Master Spec line 6: `Status: \`awaiting-human-plan-approval\`` |
| V6 | Migraciones alineadas con modelo de datos | **PASS** | 7 migraciones V1.0.1-V1.0.7 verificadas en disco, columnas/indices/constraints coinciden con S4.2-S4.6. |
| V7 | build.gradle consistente con Master Spec S2 | **PASS** | MapStruct 1.6.3 (S2 dice 1.6.x), JaCoCo 0.8.12, Flyway 10.22.0, OpenAPI Generator 7.10.0, ArchUnit 1.3.0. |
| V8 | Application YAMLs consistentes con Master Spec S9.3 | **PASS** | ddl-auto=validate, open-in-view=false, flyway enabled, H2 modo PostgreSQL para test, virtual threads enabled. |
| V9 | Stale terms guard verificado | **PASS** | EstadoCita (PROGRAMADA/CANCELADA), base path (/api/v1), naming (snake_case), ID type (UUID), error schema (ApiErrorResponse+ApiErrorDetail) — todos consistentes cross-artifact. |
| V10 | Spec Validator Approval block presente | **PASS** | Shared context lines 57-79: bloque completo con verdict: ready, reviewed_at, validator_agent, artifact_set_reviewed, summary, invalidated_by_changes_since. |
| V11 | Headings obligatorios sin duplicar | **PASS** | 10 headings requeridos presentes una sola vez: Current status, Canonical artifacts, Artifact evidence, Spec Validator Approval, Decisions locked, Validator findings, Resolved findings, Open questions, Stale terms guard, Next action. |
| V12 | Human Plan Approval heading presente | **PASS** | Shared context lines 83-86: heading presente, marcado como Pending. |

---

## 5. Veredicto Final

**`not ready`**

Existe 1 nuevo finding de severidad `high` que constituye una inconsistencia cross-artifact dentro de la Master Spec y entre Master Spec y OpenAPI:

- **NEW-F-01:** La descripcion del endpoint POST /api/v1/citas en Master Spec S6.4 (line 523) y el OpenAPI (line 330) aun referencian HTTP 400 para violaciones de RN-01/RN-03, mientras que la tabla de codigos de error S7.2 (lines 612-613) y las descripciones de RN en S5 (lines 373, 389) fueron correctamente actualizados a 422. El OpenAPI no declara respuesta 422 para POST /api/v1/citas.

Esta inconsistencia es `high` porque:
1. Es una contradiccion directa dentro de la Master Spec (S6.4 vs S7.2).
2. El OpenAPI — que es el contrato de implementacion y fuente de verdad para OpenAPI Generator — esta incorrecto.
3. El Executor implementara los status codes segun la descripcion del endpoint (S6.4) y el OpenAPI, resultando en 400 en lugar de 422 para INVALID_SLOT e INVALID_BIRTH_DATE.
4. Un reviewer marcara esto como drift arquitectonico contra las skills `restful-standard` y `springboot-java-rest-error-response-standards`.

**Next action:** `Planner corrections` — Planner debe propagar la correccion de 400→422 a Master Spec S6.4 y OpenAPI POST /api/v1/citas, luego volver a enviar a spec-validator para re-validacion.

---

## 6. Spec Validator Approval

NO APLICA — veredicto `not ready`. El bloque de aprobacion existente en shared context (lines 57-79) queda INVALIDADO por este nuevo finding hasta que Planner corrija y spec-validator re-valide.
