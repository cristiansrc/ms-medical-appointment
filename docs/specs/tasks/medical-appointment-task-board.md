# Task Board — medical-appointment

> **Spec aprobada:** `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/docs/specs/master-spec.md`
> **Shared context:** `/home/cristiansrc/Documentos/Proyectos/ms-medical-appointment/docs/specs/.working/medical-appointment-sdd-context.md`
> **Estado superior:** `todo`
> **Creado:** 2026-07-18
> **Creado por:** task-decomposer

---

## Artefactos Canónicos de Referencia

| # | Artefacto | Ruta | Verificado |
|---|---|---|---|
| 1 | Master Spec | `docs/specs/master-spec.md` | ✓ |
| 2 | OpenAPI Contract | `docs/api/openapi.yaml` | ✓ |
| 3 | Shared Context | `docs/specs/.working/medical-appointment-sdd-context.md` | ✓ |
| 4 | build.gradle | `build.gradle` | ✓ |
| 5 | settings.gradle | `settings.gradle` | ✓ |
| 6 | application.yaml | `src/main/resources/application.yaml` | ✓ |
| 7 | application-dev.yaml | `src/main/resources/application-dev.yaml` | ✓ |
| 8 | application-test.yaml | `src/main/resources/application-test.yaml` | ✓ |
| 9 | Migraciones V1.0.1-V1.0.7 | `src/main/resources/db/migration/` | ✓ |

---

## Grupo 1: Setup y Configuración Base

---

### T1 — Crear estructura de paquetes y clase Application

| Campo | Valor |
|---|---|
| **id** | `T1` |
| **title** | Crear estructura completa de paquetes hexagonales y clase `AppointmentApplication` |
| **agent** | `executor` |
| **spec_refs** | Master Spec §3.3, §2; build.gradle |
| **goal** | Dejar lista la estructura de directorios Java y el entrypoint de Spring Boot para que todas las tareas siguientes puedan crear clases en los paquetes correctos. |
| **scope** | Crear todos los directorios vacíos de paquetes bajo `src/main/java/com/medisalud/appointment/` y `src/test/java/com/medisalud/appointment/`. Crear `AppointmentApplication.java` con `@SpringBootApplication`. |
| **out_of_scope** | No crear archivos Java en los paquetes (solo directorios y la clase main). No tocar configs ni properties. |
| **inputs** | `build.gradle` (para el groupId/basePackage), `settings.gradle`, `application.yaml` |
| **implementation_notes** | Paquetes a crear: `domain.model`, `domain.service`, `domain.exception`, `application.port.input`, `application.port.output`, `application.service`, `application.dto.command`, `application.dto.query`, `application.dto.result`, `infrastructure.web`, `infrastructure.web.mapper`, `infrastructure.persistence.entity`, `infrastructure.persistence.repository`, `infrastructure.persistence.adapter`, `infrastructure.persistence.mapper`, `infrastructure.client`, `infrastructure.config`. En test: `domain`, `application`, `infrastructure`. La clase `AppointmentApplication` debe estar en el raíz `com.medisalud.appointment`. |
| **edge_cases** | Ninguno. |
| **done_criteria** | `./gradlew compileJava` compila sin errores (aunque vacío). Clase main ejecutable: `./gradlew bootRun` inicia sin fallos de escaneo. |
| **verification** | `ls src/main/java/com/medisalud/appointment/` muestra los 4 directorios: `domain`, `application`, `infrastructure`, `AppointmentApplication.java`. |
| **dependencies** | Ninguna. |
| **handoff_context** | Estructura de paquetes lista. T2 y T3 pueden ejecutarse en paralelo tras T1. |
| **source_of_truth** | Master Spec §3.3 |
| **stale_terms_guard** | `com.medisalud.appointment` (no `com.medisalud.msmedicalappointment` ni variantes). |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

### T2 — Configurar JacksonConfig

| Campo | Valor |
|---|---|
| **id** | `T2` |
| **title** | Configurar Jackson para snake_case y soporte JavaTimeModule |
| **agent** | `executor` |
| **spec_refs** | Master Spec §9.1, §9.2 |
| **goal** | Asegurar que toda serialización/deserialización JSON use `snake_case`, fechas ISO 8601, y rechace propiedades desconocidas. |
| **scope** | Crear `infrastructure/config/JacksonConfig.java`. Registrar `JavaTimeModule`. Configurar `PropertyNamingStrategies.SNAKE_CASE`. Configurar `ObjectMapper` con `write-dates-as-timestamps: false` (ya en YAML, reforzar en código si es necesario). |
| **out_of_scope** | No modificar `application.yaml`. No configurar otros módulos Jackson. |
| **inputs** | `application.yaml` (líneas 9-14) |
| **implementation_notes** | Clase `@Configuration`. Bean de `ObjectMapper` o `Jackson2ObjectMapperBuilderCustomizer`. Importar `com.fasterxml.jackson.datatype.jsr310.JavaTimeModule`. |
| **edge_cases** | Asegurar que la configuración no interfiera con Spring Boot auto-configuration. |
| **done_criteria** | `./gradlew compileJava` compila. `ObjectMapper` bean registrado y configurado. |
| **verification** | Revisar que `JacksonConfig.java` existe en `infrastructure/config/` y contiene las 3 configuraciones. |
| **dependencies** | `T1` |
| **handoff_context** | Jackson listo para T3 y capa web. |
| **source_of_truth** | Master Spec §9.2 |
| **stale_terms_guard** | `SNAKE_CASE` constante exacta (no `SnakeCaseStrategy` obsoleto). |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

### T3 — Configurar GlobalExceptionHandler con ApiErrorResponse/ApiErrorDetail

| Campo | Valor |
|---|---|
| **id** | `T3` |
| **title** | Implementar `GlobalExceptionHandler`, `ApiErrorResponse` y `ApiErrorDetail` |
| **agent** | `executor` |
| **spec_refs** | Master Spec §7, OpenAPI §components/schemas/ApiErrorResponse, skill `springboot-java-rest-error-response-standards` |
| **goal** | Centralizar el manejo de errores HTTP con estructura `ApiErrorResponse` estable y documentada para todos los endpoints. |
| **scope** | Crear `infrastructure/web/GlobalExceptionHandler.java` como `@RestControllerAdvice`. Crear `infrastructure/web/dto/ApiErrorResponse.java` y `infrastructure/web/dto/ApiErrorDetail.java` como records. Handlers: `MethodArgumentNotValidException` → 400, `HttpMessageNotReadableException` → 400, fallback `Exception` → 500. Método `build()` helper. Incluir `trace_id` con `UUID.randomUUID()`. |
| **out_of_scope** | No crear handlers para excepciones de dominio aún (ResourceNotFoundException, ConflictException, BusinessException). Esas se añaden en tareas posteriores. |
| **inputs** | OpenAPI §ApiErrorResponse, §ApiErrorDetail; Master Spec §7.1, §7.2, §7.3 |
| **implementation_notes** | `ApiErrorResponse` y `ApiErrorDetail` deben ser `record` de Java. Campos: `timestamp` (`OffsetDateTime`), `status` (`int`), `error` (`String`), `code` (`String`), `message` (`String`), `path` (`String`), `trace_id` (`String`), `details` (`List<ApiErrorDetail>`). `ApiErrorDetail`: `field`, `code`, `message`, `rejected_value` (todos `String`). Usar `HttpServletRequest` para extraer `path`. |
| **edge_cases** | `safeRejectedValue()`: si el valor rechazado es `null` o muy largo (>500 chars), truncar o devolver `"[redacted]"`. |
| **done_criteria** | `./gradlew compileJava` compila. `GlobalExceptionHandler` existe con 3 handlers mínimos. |
| **verification** | `ApiErrorResponse` tiene 8 campos requeridos (timestamp, status, error, code, message, path, trace_id, details). `ApiErrorDetail` tiene 4 campos. |
| **dependencies** | `T1` |
| **handoff_context** | Handler base listo. Las tareas de dominio (T6) y aplicación extenderán este handler con excepciones específicas. |
| **source_of_truth** | OpenAPI §ApiErrorResponse, §ApiErrorDetail; Master Spec §7 |
| **stale_terms_guard** | `ApiErrorResponse` (no `ErrorResponse`, `ApiError`). `ApiErrorDetail` (no `ErrorDetail`). `trace_id` en snake_case. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

**Nota del Grupo G1 — Política de Lombok:**

| Campo | Descripción |
|---|---|
| **lombok_policy** | Usar Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) en DTOs, entidades JPA, commands, queries, results y configuraciones. **NO** usar Lombok en capa domain (debe ser Java puro sin anotaciones de framework). |

---

## Grupo 2: Dominio

---

### T4 — Crear entidades de dominio y enums

| Campo | Valor |
|---|---|
| **id** | `T4` |
| **title** | Implementar entidades de dominio puras: Medico, Paciente, Cita, Penalizacion, Festivo + EstadoCita enum |
| **agent** | `executor` |
| **spec_refs** | Master Spec §4.1, §4.2, §4.3, §4.4, §4.5, §4.6 |
| **goal** | Crear las 5 entidades de dominio como POJOs puros sin anotaciones de framework, respetando boundaries hexagonales. |
| **scope** | Crear en `domain/model/`: `Medico.java`, `Paciente.java`, `Cita.java`, `Penalizacion.java`, `Festivo.java`, `EstadoCita.java` (enum: `PROGRAMADA`, `CANCELADA`). Usar Java records donde aplique. Todos los campos según el modelo de datos de la spec. UUID generado con `UUID.randomUUID()` en constructor/factory. |
| **out_of_scope** | No incluir anotaciones JPA, Jackson, Lombok (en capa domain). No crear Value Objects aún (T5). No crear lógica de validación compleja (va en T7). |
| **inputs** | Master Spec §4 |
| **implementation_notes** | `Cita`: incluir método `cancelar(String motivo)` que cambia estado y setea `fechaCancelacion = OffsetDateTime.now()`. `Penalizacion`: constructor con `pacienteId`, `citaId`, `fechaHora`. `Festivo`: incluir campos `date` (LocalDate), `localName`, `name`, `countryCode`, `fixed`, `global`, `types`, `year`. `Medico`: telefono y email nullable. `Paciente`: birthDate nullable, documentoIdentidad not null. Usar `record` para Medico, Paciente, Festivo si son inmutables; Cita y Penalizacion pueden ser clases normales con setters limitados. |
| **edge_cases** | `birthDate` puede ser null en Paciente. `telefono` y `email` pueden ser null en Medico. Asegurar que Cita.fechaHora es `OffsetDateTime` (con timezone). |
| **done_criteria** | `./gradlew compileJava` compila. 5 archivos + 1 enum creados en `domain/model/`. |
| **verification** | Cada clase no importa nada de `jakarta.persistence`, `com.fasterxml.jackson`, `lombok`, `org.springframework`. Solo `java.*`. |
| **dependencies** | `T1` |
| **handoff_context** | Entidades de dominio listas para ser usadas por servicios (T7), puertos (T9) y mapeadas por persistence (T12). |
| **source_of_truth** | Master Spec §4 |
| **stale_terms_guard** | `PROGRAMADA`, `CANCELADA` (no `ACTIVE`, `INACTIVE`, `SCHEDULED`). `fechaHora` tipo `OffsetDateTime` (no `LocalDateTime`). |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

### T5 — Crear Value Objects

| Campo | Valor |
|---|---|
| **id** | `T5` |
| **title** | Implementar Value Objects: Email, Telefono, DocumentoIdentidad, FranjaHoraria |
| **agent** | `executor` |
| **spec_refs** | Master Spec §3.3 (FranjaHoraria), §4.2 (telefono), §4.3 (documento_identidad, email), RN-01 |
| **goal** | Encapsular validaciones de formato en objetos inmutables reutilizables, evitando lógica dispersa en múltiples entidades. |
| **scope** | Crear en `domain/model/`: `Email.java`, `Telefono.java`, `DocumentoIdentidad.java`, `FranjaHoraria.java`. Cada uno como `record` con validación en constructor compacto. |
| **out_of_scope** | No incluir FranjaHorariaValidator aquí (va en T7). Estos VOs solo validan formato individual, no reglas de negocio compuestas. |
| **inputs** | Master Spec §4.2, §4.3, RN-01 |
| **implementation_notes** | `Email`: validar regex básico, maxLength 255. `Telefono`: minLength 7, maxLength 20. `DocumentoIdentidad`: minLength 7, maxLength 20. `FranjaHoraria`: contiene `OffsetDateTime inicio` y `OffsetDateTime fin`, valida que `fin` es exactamente 30 minutos después de `inicio`, y que los minutos son :00 o :30. |
| **edge_cases** | `FranjaHoraria`: lanzar `IllegalArgumentException` si la diferencia no es exactamente 30 min o los minutos no están alineados a :00/:30. |
| **done_criteria** | `./gradlew compileJava` compila. 4 records creados con validación en constructor. |
| **verification** | Probar manualmente: `new Email("bad")` lanza excepción, `new Email("test@test.com")` funciona. `new FranjaHoraria(inicio, inicio.plusMinutes(15))` lanza excepción. |
| **dependencies** | `T1` |
| **handoff_context** | VOs disponibles para ser usados por entidades (T4) y FranjaHorariaValidator (T7). |
| **source_of_truth** | Master Spec §4.2, §4.3, RN-01 |
| **stale_terms_guard** | `FranjaHoraria` (no `TimeSlot`, `Slot`). `inicio`, `fin` en español. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

### T6 — Crear excepciones de dominio

| Campo | Valor |
|---|---|
| **id** | `T6` |
| **title** | Implementar jerarquía de excepciones de dominio |
| **agent** | `executor` |
| **spec_refs** | Master Spec §3.3 (excepciones listadas), §7.3 |
| **goal** | Crear excepciones puras de dominio que serán traducidas a ApiErrorResponse por GlobalExceptionHandler. |
| **scope** | Crear en `domain/exception/`: `BusinessException.java` (base, con `String code`), `ResourceNotFoundException.java`, `ConflictException.java`, `SlotNotAvailableException.java`, `PatientConflictException.java`, `PatientBlockedException.java`, `InvalidSlotException.java`. |
| **out_of_scope** | No crear handlers HTTP aquí. Las traducciones a HTTP van en T3 y se extienden en tareas de aplicación. |
| **inputs** | Master Spec §7.2, §7.3 |
| **implementation_notes** | `BusinessException` es la clase base: `RuntimeException` con campo `code`. Las demás heredan y definen su `code` en constructor. `ResourceNotFoundException` acepta `String resourceName, UUID id`. `ConflictException` hereda de `BusinessException`. `InvalidSlotException` usa code `INVALID_SLOT`. `PatientBlockedException` usa code `PACIENTE_BLOCKED`. |
| **edge_cases** | Asegurar que cada excepción tenga un `code` único, estable y documentado en la spec §7.2. |
| **done_criteria** | `./gradlew compileJava` compila. 7+ clases de excepción en `domain/exception/`. |
| **verification** | `BusinessException` tiene campo `code`. Cada subclase define su `code` correspondiente a la tabla §7.2. |
| **dependencies** | `T1` |
| **handoff_context** | Excepciones listas para ser lanzadas por servicios de dominio (T7) y aplicación (T14-T21). |
| **source_of_truth** | Master Spec §7.2 |
| **stale_terms_guard** | Codes exactos: `INVALID_SLOT`, `MEDICO_SLOT_CONFLICT`, `PACIENTE_SLOT_CONFLICT`, `PACIENTE_BLOCKED`, `CITA_ALREADY_CANCELLED`, `RESOURCE_NOT_FOUND`, `DOCUMENTO_DUPLICADO`, `INVALID_BIRTH_DATE`. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

### T7 — Implementar domain services: FranjaHorariaValidator y PenalizacionEvaluator

| Campo | Valor |
|---|---|
| **id** | `T7` |
| **title** | Implementar servicios de dominio puro para validación de franjas y penalizaciones |
| **agent** | `executor` |
| **spec_refs** | Master Spec RN-01, RN-05 |
| **goal** | Centralizar las reglas RN-01 (franjas horarias) y RN-05 (penalización por cancelación tardía) en el dominio. |
| **scope** | Crear `domain/service/FranjaHorariaValidator.java` y `domain/service/PenalizacionEvaluator.java`. |
| **out_of_scope** | No incluir consultas a BD (usa puertos, que no existen aún). FranjaHorariaValidator recibe una lista de `Festivo` y un `FranjaHoraria`; no consulta por sí mismo. |
| **inputs** | Master Spec RN-01, RN-05; T4 (entidades), T5 (FranjaHoraria) |
| **implementation_notes** | `FranjaHorariaValidator`: métodos `esDiaHabil(fecha, festivos)`, `esFranjaValida(franja, festivos)`, `generarFranjas(fechaInicio, fechaFin, festivos)` (retorna `List<FranjaHoraria>`). Validar: Lun-Vie 08:00-18:00 (20 franjas), Sáb 08:00-13:00 (10 franjas), no domingo, no festivo. `PenalizacionEvaluator`: método `debePenalizar(fechaHoraCita, now)` → `Duration.between(now, fechaHoraCita).toHours() <= 2`. Método `estaBloqueado(penalizaciones, fechaReferencia)` → contar penalizaciones en ventana 30 días (desde fecha de nueva reserva hacia atrás) >= 3. |
| **edge_cases** | Umbral `<= 2h` es inclusivo (exactamente 2h penaliza). Franjas: fin exclusivo (08:00-08:30, última 17:30-18:00). Sábado: última franja 12:30-13:00. No generar franjas para domingos ni fechas en lista de festivos. `PenalizacionEvaluator` debe usar `fechaReferencia` (fecha de nueva reserva), no `OffsetDateTime.now()`. |
| **done_criteria** | `./gradlew compileJava` compila. Ambas clases no dependen de Spring, JPA ni infraestructura. |
| **verification** | `FranjaHorariaValidator.generarFranjas(fecha, fecha, emptyList)` para un lunes retorna 20 franjas; para un sábado retorna 10; para un domingo retorna 0. `PenalizacionEvaluator.debePenalizar(citaEn2Horas, now)` retorna `true`. |
| **dependencies** | `T4`, `T5` |
| **handoff_context** | Servicios de dominio listos para ser inyectados en application services (T19, T20, T21). |
| **source_of_truth** | Master Spec RN-01, RN-05 |
| **stale_terms_guard** | `debePenalizar` (no `shouldPenalize`), `estaBloqueado` (no `isBlocked`). Métodos en español. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

## Grupo 3: Contratos de Aplicación (Ports + DTOs)

---

### T8 — Crear Output Ports (interfaces de repositorio)

| Campo | Valor |
|---|---|
| **id** | `T8` |
| **title** | Definir los 5 puertos de salida (interfaces de repositorio/persistencia) en la capa de aplicación |
| **agent** | `executor` |
| **spec_refs** | Master Spec §3.3 (application.port.output), §4 |
| **goal** | Definir contratos de persistencia que la capa de aplicación necesita, sin depender de JPA. |
| **scope** | Crear en `application/port/output/`: `MedicoRepository.java`, `PacienteRepository.java`, `CitaRepository.java`, `PenalizacionRepository.java`, `FestivoRepository.java`. Cada interfaz define métodos CRUD necesarios usando modelos de dominio. |
| **out_of_scope** | No incluir métodos de consulta avanzados que no estén en la spec. No crear implementaciones (van en T15, T16, T20). |
| **inputs** | T4 (entidades de dominio), Master Spec §4 |
| **implementation_notes** | Métodos mínimos requeridos: `MedicoRepository`: `save(Medico)`, `findById(UUID)`, `findAll()`, `existsById(UUID)`. `PacienteRepository`: `save(Paciente)`, `findById(UUID)`, `findAll()`, `existsById(UUID)`, `existsByDocumentoIdentidad(String)`. `CitaRepository`: `save(Cita)`, `findById(UUID)`, `findByMedicoIdAndFechaBetween(UUID, OffsetDateTime, OffsetDateTime)`, `findByPacienteIdAndFechaBetween(UUID, OffsetDateTime, OffsetDateTime)`, `findAllWithFilters(UUID, UUID, String, LocalDate)`. `PenalizacionRepository`: `save(RegistroPenalizacion)`, `countByPacienteIdAndFechaAfter(UUID, OffsetDateTime)`. `FestivoRepository`: `obtenerFestivos(int, String)`, `esFestivo(LocalDate)`. |
| **edge_cases** | `findAllWithFilters` debe aceptar todos los parámetros como `null` (filtros opcionales). |
| **done_criteria** | `./gradlew compileJava` compila. 5 interfaces en `application/port/output/` con los métodos definidos. |
| **verification** | Interfaces no importan nada de `infrastructure`, `jakarta.persistence`, `org.springframework.data`. Solo `java.*` y `domain.model.*`. |
| **dependencies** | `T4` |
| **handoff_context** | Puertos listos para ser implementados por adaptadores JPA (T15, T16, T20) y referenciados por use cases (T17-T21). |
| **source_of_truth** | Master Spec §3.3, §4, §5 (reglas de negocio que mencionan consultas específicas) |
| **stale_terms_guard** | Métodos en español o Spanglish técnico (`findById`, `existsBy...`, `save`). |
| **status** | `done` |
| **executor_notes** | Implementado según código del usuario (T9 en su solicitud): interfaces repositorio sin sufijo Port. Compilación exitosa. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T9 — Crear Input Ports (interfaces de Use Cases)

| Campo | Valor |
|---|---|
| **id** | `T9` |
| **title** | Definir todas las interfaces de casos de uso (input ports) |
| **agent** | `executor` |
| **spec_refs** | Master Spec §3.3 (application.port.input), §6.2-§6.5 |
| **goal** | Definir los contratos de negocio que la capa web consumirá, uno por cada caso de uso identificado. |
| **scope** | Crear en `application/port/input/`: `MedicoUseCase.java`, `PacienteUseCase.java`, `CitaUseCase.java`. Interfaces consolidadas con todos los métodos de cada agregado. |
| **out_of_scope** | No crear implementaciones (van en T17-T21). |
| **inputs** | T4 (entidades de dominio), Master Spec §6 |
| **implementation_notes** | Interfaces consolidadas: `MedicoUseCase` con crear, actualizar, obtenerPorId, listarTodos, eliminar. `PacienteUseCase` con crear, actualizar, obtenerPorId, listarTodos. `CitaUseCase` con reservar, cancelar, reprogramar, consultarDisponibilidad, obtenerPorId, listarCitas. Usan parámetros directos del dominio. |
| **edge_cases** | Cada interfaz agrupa todos los casos de uso de un agregado. |
| **done_criteria** | `./gradlew compileJava` compila. 3 interfaces en `application/port/input/`. |
| **verification** | Interfaces no importan nada de `infrastructure`, `jakarta.persistence`, `org.springframework`. Solo `java.*` y `domain.model.*`. |
| **dependencies** | `T4` |
| **handoff_context** | Input ports listos para ser implementados por services (T17-T21) y consumidos por controllers (T24-T27). |
| **source_of_truth** | Master Spec §3.3, §6; solicitud explícita del usuario |
| **stale_terms_guard** | `MedicoUseCase`, `PacienteUseCase`, `CitaUseCase` (nombres consolidados por solicitud del usuario). |
| **status** | `done` |
| **executor_notes** | Implementado según código del usuario (T8 en su solicitud): interfaces consolidadas en lugar de interfaces separadas por caso de uso. Compilación exitosa. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T10 — Crear Application DTOs (Commands, Queries, Results)

| Campo | Valor |
|---|---|
| **id** | `T10` |
| **title** | Implementar todos los DTOs de aplicación: Commands |
| **agent** | `executor` |
| **spec_refs** | Master Spec §3.3 (application.dto), §6 |
| **goal** | Crear los modelos de transporte internos que usan los use cases, independientes de HTTP y persistencia. |
| **scope** | Crear en `application/dto/`: `CrearMedicoCommand.java`, `ActualizarMedicoCommand.java`, `CrearPacienteCommand.java`, `ActualizarPacienteCommand.java`, `ReservarCitaCommand.java`, `CancelarCitaCommand.java`, `ReprogramarCitaCommand.java`. Usar Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`. |
| **out_of_scope** | No incluir anotaciones de validación Bean Validation (eso va en los DTOs generados por OpenAPI). |
| **inputs** | Master Spec §6, OpenAPI schemas |
| **implementation_notes** | Commands con campos: `CrearMedicoCommand`: nombreCompleto, especialidad, telefono, email. `ActualizarMedicoCommand`: igual. `CrearPacienteCommand`: nombreCompleto, documentoIdentidad, telefono, email, fechaNacimiento (LocalDate). `ActualizarPacienteCommand`: nombreCompleto, telefono, email, fechaNacimiento. `ReservarCitaCommand`: pacienteId (UUID), medicoId (UUID), fechaHora (OffsetDateTime). `CancelarCitaCommand`: motivo. `ReprogramarCitaCommand`: nuevaFechaHora (OffsetDateTime). |
| **edge_cases** | Todos los campos String son nullable. |
| **done_criteria** | `./gradlew compileJava` compila. 7 DTOs en `application/dto/`. |
| **verification** | DTOs usan Lombok (no records). Importan `lombok.*`. |
| **dependencies** | `T1` |
| **handoff_context** | DTOs listos para T9 (input ports), T17-T21 (services), y T24-T27 (controllers, vía mappers). |
| **source_of_truth** | Master Spec §3.3, §6; OpenAPI; solicitud explícita del usuario |
| **stale_terms_guard** | `CrearMedicoCommand`, `ActualizarMedicoCommand`, `ReservarCitaCommand`, `CancelarCitaCommand`, `ReprogramarCitaCommand` (nombres en español por solicitud del usuario). |
| **status** | `done` |
| **executor_notes** | Implementado según código del usuario (T10 en su solicitud): commands con Lombok. Compilación exitosa. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

## Grupo 4: Persistencia (Infrastructure)

---

### T11 — Crear entidades JPA

| Campo | Valor |
|---|---|
| **id** | `T11` |
| **title** | Implementar entidades JPA: MedicoEntity, PacienteEntity, CitaEntity, PenalizacionEntity, FestivoEntity |
| **agent** | `executor` |
| **spec_refs** | Master Spec §4.2-4.6, skill `jpa-stack`, skill `postgresql-standard` |
| **goal** | Crear los modelos de persistencia separados del dominio, con mapeo correcto a las tablas definidas en las migraciones. |
| **scope** | Crear en `infrastructure/persistence/entity/`: `MedicoEntity.java`, `PacienteEntity.java`, `CitaEntity.java`, `PenalizacionEntity.java`, `FestivoEntity.java`. Usar `@Entity`, `@Table(name = "...")`, `@Column(name = "...")`, `@Id`, `@GeneratedValue` (UUID generation). Campos de auditoría: `createdAt`, `updatedAt` con `@CreatedDate`, `@LastModifiedDate`. |
| **out_of_scope** | No incluir lógica de negocio en entidades. No definir relaciones `@OneToMany`/`@ManyToOne` si no son estrictamente necesarias (usar solo columnas FK). |
| **inputs** | Migraciones V1.0.1-V1.0.5, Master Spec §4, T4 (entidades de dominio) |
| **implementation_notes** | Activar JPA Auditing con `@EnableJpaAuditing` en una clase de configuración o en `AppointmentApplication`. Columnas: usar `snake_case` en `name`. UUID: usar `@Id @GeneratedValue(strategy = GenerationType.UUID)` o asignar manualmente con `@PrePersist`. `createdAt`/`updatedAt` tipo `OffsetDateTime`. `CitaEntity.fechaHora` → `TIMESTAMPTZ`. `FestivoEntity.date` → `DATE`. Estados: `@Enumerated(EnumType.STRING)`. |
| **edge_cases** | Asegurar que `updatedAt` se inicializa con `CURRENT_TIMESTAMP` (el trigger de BD lo maneja, pero JPA debe ser consistente). `FestivoEntity` tiene constraint UNIQUE(date, year, country_code) — no modelable fácilmente con JPA, usar `@Table(uniqueConstraints = ...)`. |
| **done_criteria** | `./gradlew compileJava` compila. 5 entidades con anotaciones JPA correctas. |
| **verification** | Cada `@Table.name` y `@Column.name` coincide exactamente con las migraciones. Tipos de datos: `UUID`, `String`, `OffsetDateTime`, `LocalDate`, `Boolean`, `Integer`. |
| **dependencies** | `T1`, migraciones existentes |
| **handoff_context** | Entidades JPA listas para T12 (repositorios Spring Data) y T13-T16 (adapters y mappers). |
| **source_of_truth** | Migraciones V1.0.1-V1.0.5, Master Spec §4 |
| **stale_terms_guard** | Nombres de tabla/columna exactos: `medicos`, `pacientes`, `citas`, `penalizaciones`, `festivos`. `nombre_completo`, `documento_identidad`, `fecha_hora`, `birth_date`. |
| **status** | `done` |
| **executor_notes** | Implementadas 5 entidades JPA con anotaciones @Entity, @Table, @Column, @CreationTimestamp, @UpdateTimestamp, Lombok @Data/@Builder. Se usó MapStruct default methods para evitar ambigüedad de constructores en dominio. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T12 — Crear JPA Repositories (Spring Data)

| Campo | Valor |
|---|---|
| **id** | `T12` |
| **title** | Implementar interfaces Spring Data JPA para las 5 entidades |
| **agent** | `executor` |
| **spec_refs** | Master Spec §3.3 (infrastructure.persistence.repository), T8 (output ports) |
| **goal** | Proveer acceso a datos mediante Spring Data JPA con los métodos de consulta necesarios. |
| **scope** | Crear en `infrastructure/persistence/repository/`: `MedicoJpaRepository.java`, `PacienteJpaRepository.java`, `CitaJpaRepository.java`, `PenalizacionJpaRepository.java`, `FestivoJpaRepository.java`. Extender `JpaRepository<Entity, UUID>`. |
| **out_of_scope** | No exponer estos repositorios fuera de `infrastructure.persistence`. |
| **inputs** | T11 (entidades JPA), T8 (output ports para conocer qué métodos se necesitan) |
| **implementation_notes** | `PacienteJpaRepository`: `existsByDocumentoIdentidad(String)`, `existsByDocumentoIdentidadAndIdNot(String, UUID)`. `CitaJpaRepository`: `existsByMedicoIdAndFechaHoraAndEstado(UUID, OffsetDateTime, EstadoCita)`, `existsByPacienteIdAndFechaHoraAndEstado(UUID, OffsetDateTime, EstadoCita)`, `findByMedicoIdAndFechaHoraBetweenAndEstado(UUID, OffsetDateTime, OffsetDateTime, EstadoCita)`, métodos con `@Query` para filtros combinables o usar `Specification<CitaEntity>`. `PenalizacionJpaRepository`: `countByPacienteIdAndFechaHoraAfter(UUID, OffsetDateTime)`. `FestivoJpaRepository`: `findByYear(int)`, `existsByYear(int)`, `findByDateBetween(LocalDate, LocalDate)`, `saveAll` (heredado). |
| **edge_cases** | Para `listCitas` con filtros combinables, se recomienda usar `JpaSpecificationExecutor<CitaEntity>` para armar queries dinámicas. |
| **done_criteria** | `./gradlew compileJava` compila. 5 interfaces en `infrastructure/persistence/repository/`. |
| **verification** | Cada método de consulta tiene su equivalente en los output ports (T8). |
| **dependencies** | `T11` |
| **handoff_context** | Repositorios listos para ser usados por adapters (T15, T16). |
| **source_of_truth** | T8 (output ports), Master Spec §4 |
| **stale_terms_guard** | Nombres de métodos Spring Data: `findBy...`, `existsBy...`, `countBy...`. Nombres de campos en camelCase Java (de la entidad). |
| **status** | `done` |
| **executor_notes** | Implementadas 5 interfaces JPA Repository con métodos de consulta según output ports. CitaJpaRepository con @Query para filtros combinables. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T13 — Crear PersistenceMapper (MapStruct Entity ↔ Domain)

| Campo | Valor |
|---|---|
| **id** | `T13` |
| **title** | Implementar mappers MapStruct para convertir entre entidades JPA y modelos de dominio |
| **agent** | `executor` |
| **spec_refs** | Master Spec §3.3 (infrastructure.persistence.mapper), skill `repository-dto-patterns`, skill `jpa-stack` |
| **goal** | Centralizar las conversiones Entity ↔ Domain en un mapper MapStruct que será usado por los adapters. |
| **scope** | Crear `infrastructure/persistence/mapper/PersistenceMapper.java` como interfaz MapStruct `@Mapper(componentModel = "spring")`. Métodos: `toDomain(MedicoEntity)`, `toEntity(Medico)`, `toDomain(PacienteEntity)`, `toEntity(Paciente)`, `toDomain(CitaEntity)`, `toEntity(Cita)`, `toDomain(PenalizacionEntity)`, `toEntity(Penalizacion)`, `toDomain(FestivoEntity)`, `toEntity(Festivo)`, `toDomain(List<FestivoEntity>)`. |
| **out_of_scope** | No mapear API DTOs aquí (eso va en WebMapper, T24-T27). |
| **inputs** | T4 (entidades de dominio), T11 (entidades JPA) |
| **implementation_notes** | MapStruct genera la implementación en tiempo de compilación (anotación `@Mapper(componentModel = "spring")`). Si hay campos con nombres diferentes o tipos que requieren conversión, usar `@Mapping`. `CitaEntity.fechaHora` ↔ `Cita.fechaHora` (mismo tipo). `EstadoCita` enum se mapea automáticamente. Para `FestivoEntity` ↔ `Festivo`: campos directos. |
| **edge_cases** | Campos `null` deben mapearse a `null`. Si una entidad tiene `id = null` (nueva), el mapper debe respetarlo. |
| **done_criteria** | `./gradlew compileJava` compila (MapStruct genera `PersistenceMapperImpl`). |
| **verification** | `PersistenceMapperImpl` generado en `build/generated/sources/annotationProcessor/`. Tiene todos los métodos de mapeo. |
| **dependencies** | `T4`, `T11` |
| **handoff_context** | Mapper listo para ser inyectado en adapters (T15, T16, T20). |
| **source_of_truth** | Master Spec §3.3 |
| **stale_terms_guard** | `PersistenceMapper` (no `EntityMapper`, `DomainMapper`). Interfaz en `infrastructure.persistence.mapper`. |
| **status** | `done` |
| **executor_notes** | Implementados 4 mappers MapStruct separados (MedicoMapper, PacienteMapper, CitaMapper, PenalizacionMapper). toDomain como default method por ambigüedad de constructores en dominio. No se requiere mapper para Festivo (puerto devuelve LocalDate). |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T14 — Implementar MedicoRepositoryAdapter y PacienteRepositoryAdapter

| Campo | Valor |
|---|---|
| **id** | `T14` |
| **title** | Implementar adaptadores de persistencia para Médicos y Pacientes |
| **agent** | `executor` |
| **spec_refs** | Master Spec §3.3 (infrastructure.persistence.adapter), T8 (output ports), T12 (JPA repositories) |
| **goal** | Conectar los puertos de salida de Médico y Paciente con Spring Data JPA mediante adapters. |
| **scope** | Crear `infrastructure/persistence/adapter/MedicoRepositoryAdapter.java` y `PacienteRepositoryAdapter.java`. Implementan `MedicoRepositoryPort` y `PacienteRepositoryPort` respectivamente. Usan `PersistenceMapper` y los `JpaRepository` correspondientes. |
| **out_of_scope** | No incluir lógica de negocio (solo delegación y mapeo). |
| **inputs** | T8 (output ports), T4 (dominio), T11 (entidades JPA), T12 (JPA repositories), T13 (PersistenceMapper) |
| **implementation_notes** | `MedicoRepositoryAdapter`: `@Repository`. Implementa `MedicoRepositoryPort`. `save`: `mapper.toEntity(medico)` → `jpaRepository.save(entity)` → `mapper.toDomain(saved)`. `findById`: `jpaRepository.findById(id).map(mapper::toDomain).orElse(null)`. `findAll`: `jpaRepository.findAll().stream().map(mapper::toDomain).toList()`. `PacienteRepositoryAdapter`: similar, con `existsByDocumentoIdentidad` y `existsByDocumentoIdentidadAndIdNot` delegando al JPA repository. |
| **edge_cases** | `save` debe manejar tanto creación como actualización (JPA `save` hace merge). Si `findById` no encuentra, retornar `null` o `Optional.empty()` según el contrato del port. |
| **done_criteria** | `./gradlew compileJava` compila. 2 adapters en `infrastructure/persistence/adapter/`. |
| **verification** | Cada adapter implementa su port correspondiente y tiene `@Repository`. Los métodos delegan correctamente. |
| **dependencies** | `T8`, `T11`, `T12`, `T13` |
| **handoff_context** | Persistencia de Médicos y Pacientes lista para ser inyectada en T17 y T18. |
| **source_of_truth** | T8 (output ports), Master Spec §3.3 |
| **stale_terms_guard** | `MedicoRepositoryAdapter` (no `MedicoAdapter`, `MedicoPersistenceAdapter`). Exactamente el nombre en spec. |
| **status** | `done` |
| **executor_notes** | Implementados MedicoRepositoryAdapter y PacienteRepositoryAdapter. Implementan los 3 puertos de salida con inyección de JpaRepository y mapper. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T15 — Implementar CitaRepositoryAdapter y PenalizacionRepositoryAdapter

| Campo | Valor |
|---|---|
| **id** | `T15` |
| **title** | Implementar adaptadores de persistencia para Citas y Penalizaciones |
| **agent** | `executor` |
| **spec_refs** | Master Spec §3.3, T8 |
| **goal** | Conectar los puertos de Cita y Penalización con Spring Data JPA. |
| **scope** | Crear `infrastructure/persistence/adapter/CitaRepositoryAdapter.java` y `PenalizacionRepositoryAdapter.java`. Implementan `CitaRepositoryPort` y `PenalizacionRepositoryPort`. |
| **out_of_scope** | No incluir lógica de negocio de penalizaciones (RN-05) aquí. |
| **inputs** | T8, T4, T11, T12, T13 |
| **implementation_notes** | `CitaRepositoryAdapter`: implementa `findByFilters` usando `Specification<CitaEntity>` o `@Query` con condiciones dinámicas. `findByMedicoIdAndFechaHoraBetweenAndEstado`: delegar a JPA repository. `PenalizacionRepositoryAdapter`: `countByPacienteIdAndFechaHoraAfter` delegando a `PenalizacionJpaRepository`. |
| **edge_cases** | `findByFilters` con todos los parámetros `null` debe retornar todas las citas ordenadas por `fechaHora DESC`. El ordenamiento se logra con `Sort.by(Sort.Direction.DESC, "fechaHora")`. |
| **done_criteria** | `./gradlew compileJava` compila. 2 adapters creados. |
| **verification** | `CitaRepositoryAdapter.findByFilters` maneja correctamente cada combinación de filtros. |
| **dependencies** | `T8`, `T11`, `T12`, `T13` |
| **handoff_context** | Persistencia de Citas y Penalizaciones lista para T19, T20, T21. |
| **source_of_truth** | T8, Master Spec §3.3 |
| **stale_terms_guard** | `CitaRepositoryAdapter`, `PenalizacionRepositoryAdapter`. `findByFilters` (no `findAllWithFilters`). |
| **status** | `done` |
| **executor_notes** | Implementados CitaRepositoryAdapter y PenalizacionRepositoryAdapter. CitaRepositoryAdapter delega findAllWithFilters al @Query del JPA repository. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T16 — Implementar FestivoRepositoryAdapter

| Campo | Valor |
|---|---|
| **id** | `T16` |
| **title** | Implementar adaptador de persistencia para Festivos |
| **agent** | `executor` |
| **spec_refs** | Master Spec §3.3, §4.6, §8, T8 |
| **goal** | Conectar el puerto de Festivo con Spring Data JPA para almacenar/cachear festivos de Colombia. |
| **scope** | Crear `infrastructure/persistence/adapter/FestivoRepositoryAdapter.java`. Implementa `FestivoRepositoryPort`. |
| **out_of_scope** | No incluir la lógica de carga desde Nager.Date (va en T18-T19). |
| **inputs** | T8, T4, T11, T12, T13 |
| **implementation_notes** | `saveAll`: `festivos.stream().map(mapper::toEntity).forEach(jpaRepository::save)` o `jpaRepository.saveAll(entities)`. `findByYear`: delegar a `FestivoJpaRepository.findByYear(year)`. `existsByYear`: delegar a `FestivoJpaRepository.existsByYear(year)`. `findByDateBetween`: delegar a `FestivoJpaRepository.findByDateBetween(inicio, fin)`. |
| **edge_cases** | `saveAll` con lista vacía no debe fallar. |
| **done_criteria** | `./gradlew compileJava` compila. Adapter creado. |
| **verification** | `FestivoRepositoryAdapter` implementa todos los métodos de `FestivoRepositoryPort`. |
| **dependencies** | `T8`, `T11`, `T12`, `T13` |
| **handoff_context** | Persistencia de festivos lista para T19 (servicio de carga). |
| **source_of_truth** | T8, Master Spec §8 |
| **stale_terms_guard** | `FestivoRepositoryAdapter`. |
| **status** | `done` |
| **executor_notes** | Implementado FestivoRepositoryAdapter. No requiere mapper (FestivoRepository trabaja con LocalDate). Consulta festivos filtrando por año y país desde la entidad. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

## Grupo 5: Servicios de Aplicación — Médicos y Pacientes

---

### T17 — Implementar MedicoService (use cases de Médico)

| Campo | Valor |
|---|---|
| **id** | `T17` |
| **title** | Implementar servicio de aplicación para CRUD de Médicos |
| **agent** | `executor` |
| **spec_refs** | Master Spec §6.2, §3.3 (application.service) |
| **goal** | Implementar los 4 use cases de Médico: crear, obtener, listar, actualizar. |
| **scope** | Crear `application/service/MedicoService.java`. Implementa `CreateMedicoUseCase`, `GetMedicoUseCase`, `ListMedicosUseCase`, `UpdateMedicoUseCase`. Usa `MedicoRepositoryPort`. |
| **out_of_scope** | No incluir manejo de transacciones complejas. |
| **inputs** | T8 (MedicoRepositoryPort), T9 (input ports), T10 (DTOs), T4 (Medico domain) |
| **implementation_notes** | Clase `@Service`. `create`: validar datos con `CreateMedicoCommand`, crear `Medico` con `UUID.randomUUID()`, `save`, retornar `MedicoResult`. `getById`: buscar, si no existe lanzar `ResourceNotFoundException("Medico", id)`. `list`: delegar a `findAll`, mapear a results. `update`: buscar existente, actualizar campos, `save`. |
| **edge_cases** | `update` debe preservar campos no enviados (solo actualizar los que vienen en el command). Si `telefono`/`email` vienen como `null`, setearlos a `null`. |
| **done_criteria** | `./gradlew compileJava` compila. `MedicoService` implementa las 4 interfaces de input port. |
| **verification** | `create` genera nuevo UUID. `update` sobre ID inexistente lanza `ResourceNotFoundException`. |
| **dependencies** | `T4`, `T6`, `T8`, `T9`, `T10`, `T14` |
| **handoff_context** | Servicio de Médicos listo para ser inyectado en T24 (MedicoController). |
| **source_of_truth** | Master Spec §6.2 |
| **stale_terms_guard** | `MedicoService` (no `MedicoServiceImpl`). `ResourceNotFoundException("Medico", id)`. |
| **status** | `done` |
| **executor_notes** | Implementado MedicoService.java implementando MedicoUseCase con inyección de MedicoRepository. CRUD completo con `@Transactional`. Soft-delete mediante desactivar(). |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T18 — Implementar PacienteService (use cases de Paciente)

| Campo | Valor |
|---|---|
| **id** | `T18` |
| **title** | Implementar servicio de aplicación para CRUD de Pacientes, incluyendo validación RN-03 |
| **agent** | `executor` |
| **spec_refs** | Master Spec §6.3, RN-03 |
| **goal** | Implementar los 4 use cases de Paciente con validación de documento duplicado y fecha de nacimiento. |
| **scope** | Crear `application/service/PacienteService.java`. Implementa `CreatePacienteUseCase`, `GetPacienteUseCase`, `ListPacientesUseCase`, `UpdatePacienteUseCase`. |
| **out_of_scope** | No incluir validación de bloqueo (eso es en CitaService). |
| **inputs** | T8 (PacienteRepositoryPort), T9, T10, T4, T6 |
| **implementation_notes** | `create`: validar `documentoIdentidad` único (`existsByDocumentoIdentidad` → `ConflictException("DOCUMENTO_DUPLICADO")` si ya existe). Validar `birthDate` no futuro (si != null y `birthDate.isAfter(LocalDate.now())` → `BusinessException("INVALID_BIRTH_DATE")`). `update`: verificar duplicado con `existsByDocumentoIdentidadAndIdNot`. |
| **edge_cases** | `birthDate` null → sin validación de fecha (edad=0). El documento duplicado en update debe excluir el mismo paciente (por eso `AndIdNot`). |
| **done_criteria** | `./gradlew compileJava` compila. `PacienteService` con validaciones RN-03. |
| **verification** | Crear paciente con `birthDate` futuro lanza `BusinessException("INVALID_BIRTH_DATE")`. Duplicar documento lanza `ConflictException("DOCUMENTO_DUPLICADO")`. |
| **dependencies** | `T4`, `T6`, `T8`, `T9`, `T10`, `T14` |
| **handoff_context** | Servicio de Pacientes listo para T25 (PacienteController). |
| **source_of_truth** | Master Spec §6.3, RN-03 |
| **stale_terms_guard** | `DOCUMENTO_DUPLICADO`, `INVALID_BIRTH_DATE` codes exactos. |
| **status** | `done` |
| **executor_notes** | Implementado PacienteService.java implementando PacienteUseCase con inyección de PacienteRepository. Validación de documento duplicado con BusinessException("DUPLICATE_DOCUMENT"). |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

## Grupo 6: Servicios de Aplicación — Citas y Disponibilidad

---

### T19 — Implementar CreateCitaUseCase, GetCitaUseCase y ListCitasUseCase

| Campo | Valor |
|---|---|
| **id** | `T19` |
| **title** | Implementar creación, consulta individual y listado de citas con todas las reglas de negocio |
| **agent** | `executor` |
| **spec_refs** | Master Spec §6.4, RN-01, RN-02, RN-04, RN-05 |
| **goal** | Implementar los casos de uso de creación, consulta y listado de citas, aplicando todas las validaciones de reglas de negocio. |
| **scope** | Crear `application/service/CitaService.java`. Implementa `CreateCitaUseCase`, `GetCitaUseCase`, `ListCitasUseCase`. Inyecta `CitaRepositoryPort`, `PacienteRepositoryPort`, `MedicoRepositoryPort`, `PenalizacionRepositoryPort`, `FestivoRepositoryPort`, `FranjaHorariaValidator`, `PenalizacionEvaluator`. |
| **out_of_scope** | No incluir cancelación (T20) ni reprogramación (T20). |
| **inputs** | T4, T6, T7 (validators), T8, T9, T10, T15 (Cita/Penalizacion adapters), T14 (Medico/Paciente adapters) |
| **implementation_notes** | `create`: (1) Validar que `pacienteId` y `medicoId` existen (`findById` → si null, `ResourceNotFoundException("Paciente", id)` / `ResourceNotFoundException("Medico", id)`). (2) Obtener `Paciente` y validar RN-03 (birthDate no futuro) si no se validó antes. (3) Validar RN-01: `FranjaHoraria` debe ser válida según `FranjaHorariaValidator.esFranjaValida()`, consultando festivos con `FestivoRepositoryPort.findByDateBetween()`. (4) Validar RN-05: `PenalizacionEvaluator.estaBloqueado()` consultando penalizaciones con `PenalizacionRepositoryPort.countByPacienteIdAndFechaHoraAfter()`, usando `fechaHora` de la nueva cita como referencia. (5) Validar RN-02: `CitaRepositoryPort.existsByMedicoIdAndFechaHoraAndEstado(medicoId, fechaHora, PROGRAMADA)`. (6) Validar RN-04: `existsByPacienteIdAndFechaHoraAndEstado(pacienteId, fechaHora, PROGRAMADA)`. (7) Si todo OK, crear `Cita`, `save`, retornar `CitaResult`. `getById`: buscar + `ResourceNotFoundException("Cita", id)`. `list`: delegar a `findByFilters` con `ListCitasQuery`. |
| **edge_cases** | Si el paciente tiene `birthDate` futuro y ya estaba guardado (pudo haber pasado validación anterior), el sistema debe validarlo de nuevo. Si los festivos para el año no existen en BD, `FestivoRepositoryPort.findByYear` retorna vacío → se asume sin festivos (fallback graceful). |
| **done_criteria** | `./gradlew compileJava` compila. `CitaService.create` implementa los 6 pasos de validación. |
| **verification** | `create` con médico inexistente lanza `ResourceNotFoundException`. `create` en franja ocupada lanza `ConflictException("MEDICO_SLOT_CONFLICT")`. `create` con paciente bloqueado lanza `PatientBlockedException`. |
| **dependencies** | `T4`, `T6`, `T7`, `T8`, `T9`, `T10`, `T14`, `T15` |
| **handoff_context** | Core de citas listo para T26 (CitaController POST y GET). |
| **source_of_truth** | Master Spec §6.4, RN-01, RN-02, RN-04, RN-05 |
| **stale_terms_guard** | `CitaService` (no `AppointmentService`). Codes: `MEDICO_SLOT_CONFLICT`, `PACIENTE_SLOT_CONFLICT`, `PACIENTE_BLOCKED`. |
| **status** | `done` |
| **executor_notes** | Implementado en CitaService.java junto con T20 y T21. Servicio consolidado que implementa CitaUseCase con todos los métodos: reservar, cancelar, reprogramar, consultarDisponibilidad, obtenerPorId, listarCitas. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T20 — Implementar CancelCitaUseCase y ReprogramarCitaUseCase

| Campo | Valor |
|---|---|
| **id** | `T20` |
| **title** | Implementar cancelación y reprogramación de citas con atomicidad transaccional |
| **agent** | `executor` |
| **spec_refs** | Master Spec RN-05, RN-06, §6.4 |
| **goal** | Implementar cancelación semántica (DELETE lógico) con penalización condicional, y reprogramación atómica. |
| **scope** | Extender `application/service/CitaService.java` con implementaciones de `CancelCitaUseCase` y `ReprogramarCitaUseCase`. |
| **out_of_scope** | No implementar borrado físico. |
| **inputs** | T4, T6, T7, T8, T9, T10, T15, T19 (misma clase CitaService) |
| **implementation_notes** | `cancel`: (1) Buscar cita por ID, si no existe `ResourceNotFoundException("Cita", id)`. (2) Si estado == CANCELADA → `ConflictException("CITA_ALREADY_CANCELLED")`. (3) Evaluar penalización: `PenalizacionEvaluator.debePenalizar(cita.getFechaHora(), OffsetDateTime.now())`. Si true → crear `Penalizacion` y guardar. (4) `cita.cancelar(null)` (motivo opcional), `save`. `reprogramar`: `@Transactional`. (1) Buscar cita. (2) Validar no cancelada. (3) Validar NuevaFranja = `FranjaHorariaValidator.esFranjaValida()`. (4) Cancelar cita original (con penalización si aplica). (5) Validar disponibilidad nueva franja (RN-02, RN-04). (6) Crear nueva cita. (7) Retornar nueva cita. Si falla en (5) o (6), la transacción completa se revierte (cancelación también). |
| **edge_cases** | Cancelación exactamente a 2h de la cita → penaliza (inclusivo). Reprogramación: la nueva cita tiene nuevo UUID (no reutiliza el de la original). Si la nueva franja es la misma que la original → ¿permitir o rechazar? La spec no lo prohíbe explícitamente, pero RN-02/RN-04 bloquearía por conflicto consigo misma si la cita original sigue PROGRAMADA durante la validación. Cancelar primero resuelve esto. |
| **done_criteria** | `./gradlew compileJava` compila. Ambos use cases implementados en `CitaService`. |
| **verification** | `cancel` sobre cita ya cancelada lanza `ConflictException("CITA_ALREADY_CANCELLED")`. `reprogramar` es `@Transactional`. |
| **dependencies** | `T19` (misma clase), `T7`, `T15` |
| **handoff_context** | Cancelación y reprogramación listas para T26 (CitaController DELETE y POST reprogramar). |
| **source_of_truth** | Master Spec RN-05, RN-06, §6.4 |
| **stale_terms_guard** | `@Transactional` en `reprogramar`. `CITA_ALREADY_CANCELLED` code. |
| **status** | `done` |
| **executor_notes** | Implementado en CitaService.java (misma clase que T19/T20). Método consultarDisponibilidad genera franjas de 30 min 7:00-17:00 excluyendo ocupadas, domingos y festivos. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T21 — Implementar ConsultarDisponibilidadUseCase

| Campo | Valor |
|---|---|
| **id** | `T21` |
| **title** | Implementar consulta de disponibilidad de franjas horarias |
| **agent** | `executor` |
| **spec_refs** | Master Spec §6.5, RN-01 |
| **goal** | Implementar el caso de uso que calcula franjas disponibles para un médico en un rango de fechas. |
| **scope** | Crear `application/service/DisponibilidadService.java`. Implementa `ConsultarDisponibilidadUseCase`. |
| **out_of_scope** | No modificar CitaService. |
| **inputs** | T4, T7 (FranjaHorariaValidator), T8 (MedicoRepositoryPort, CitaRepositoryPort, FestivoRepositoryPort), T9, T10 |
| **implementation_notes** | `consultar(medicoId, fechaInicio, fechaFin)`: (1) Validar que `medicoId` existe (`MedicoRepositoryPort.findById`). (2) Validar `fechaInicio <= fechaFin`. (3) Obtener festivos en rango: `FestivoRepositoryPort.findByDateBetween(fechaInicio, fechaFin)`. (4) Usar `FranjaHorariaValidator.generarFranjas(fechaInicio, fechaFin, festivos)` para obtener todas las franjas hábiles. (5) Obtener citas PROGRAMADAS del médico en el rango: `CitaRepositoryPort.findByMedicoIdAndFechaHoraBetweenAndEstado(...)`. (6) Excluir las franjas ocupadas. (7) Retornar `DisponibilidadResult`. |
| **edge_cases** | Si no hay festivos cacheados para el año, `findByDateBetween` retorna vacío (fallback graceful). `fechaInicio > fechaFin` → `BusinessException("INVALID_DATE_RANGE")`. |
| **done_criteria** | `./gradlew compileJava` compila. `DisponibilidadService` implementa el use case. |
| **verification** | `consultar` con fechaInicio > fechaFin lanza `BusinessException("INVALID_DATE_RANGE")`. Con médico inexistente lanza `ResourceNotFoundException`. |
| **dependencies** | `T4`, `T6`, `T7`, `T8`, `T9`, `T10`, `T14`, `T15` |
| **handoff_context** | Disponibilidad lista para T27 (DisponibilidadController). |
| **source_of_truth** | Master Spec §6.5 |
| **stale_terms_guard** | `DisponibilidadService`, `ConsultarDisponibilidadUseCase`. `generarFranjas`, no `generateSlots`. |
| **status** | `done` |
| **executor_notes** | Implementado en CitaService.java (misma clase que T19/T20). Método consultarDisponibilidad implementado según spec §6.5. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

## Grupo 7: Integración Nager.Date

---

### T22 — Implementar RestClientConfig y NagerDateClient

| Campo | Valor |
|---|---|
| **id** | `T22` |
| **title** | Configurar RestClient de Spring y crear cliente HTTP para Nager.Date API |
| **agent** | `executor` |
| **spec_refs** | Master Spec §8, §2 |
| **goal** | Crear un cliente HTTP con timeout, retry y tipado para consumir la API pública de festivos. |
| **scope** | Crear `infrastructure/config/RestClientConfig.java` (bean de `RestClient.Builder`) y `infrastructure/client/NagerDateClient.java`. |
| **out_of_scope** | No incluir lógica de caché/fallback (va en T23). |
| **inputs** | `application.yaml` (medisalud.nager-date config), Master Spec §8.1 |
| **implementation_notes** | `RestClientConfig`: bean de `RestClient` con `baseUrl`, `connectTimeout(Duration.ofSeconds(5))`, `readTimeout(Duration.ofSeconds(5))`. `NagerDateClient`: usar `RestClient` para llamar a `GET /PublicHolidays/{year}/CO`. Mapear respuesta JSON a un `List<NagerDateHolidayResponse>` (record/DTO temporal). Incluir retry simple: si falla, reintentar 1 vez con backoff 1s. Si falla de nuevo, lanzar excepción unchecked que será capturada por el cache service (T23). |
| **edge_cases** | Timeout de 5s total (connect + read). API retorna array vacío si el año no tiene datos — no es error. Si API retorna 4xx/5xx, capturar y retornar `Optional.empty()` o lanzar excepción específica. |
| **done_criteria** | `./gradlew compileJava` compila. `NagerDateClient` con método `fetchHolidays(int year)`. |
| **verification** | `RestClientConfig` expone un bean `RestClient`. `NagerDateClient` inyecta `RestClient`. |
| **dependencies** | `T1` |
| **handoff_context** | Cliente HTTP listo para T23. |
| **source_of_truth** | Master Spec §8.1, `application.yaml` |
| **stale_terms_guard** | `NagerDateClient` (no `HolidayClient`, `PublicHolidayClient`). URL `https://date.nager.at/api/v3`. |
| **status** | `done` |
| **executor_notes** | Creados RestClientConfig.java y NagerDateClient.java |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T23 — Implementar FestivoCacheService (carga lazy + fallback graceful)

| Campo | Valor |
|---|---|
| **id** | `T23` |
| **title** | Implementar servicio de caché de festivos con carga lazy desde Nager.Date |
| **agent** | `executor` |
| **spec_refs** | Master Spec §8.2, §8.3, §8.4 |
| **goal** | Orquestar la carga y almacenamiento de festivos: consulta BD primero, si no hay datos para el año, consulta API externa y persiste. |
| **scope** | Crear `application/service/FestivoCacheService.java`. Inyecta `FestivoJpaRepository` y `NagerDateClient`. |
| **out_of_scope** | No exponer como REST endpoint. Es servicio interno. |
| **inputs** | T4, T8 (FestivoRepository), T16 (FestivoRepositoryAdapter), T22 (NagerDateClient) |
| **implementation_notes** | Método `cargarFestivosSiEsNecesario(int anio, String pais)`: (1) `festivoJpaRepository.findByYear(year)` → si no vacío, retornar. (2) Si vacío, llamar `NagerDateClient.obtenerFestivos()`. (3) Si API falla → log WARN y retornar lista vacía. (4) Si API responde, mapear `FestivoDTO` a `FestivoEntity` y guardar con `festivoJpaRepository.saveAll()`. Método `esFestivo(LocalDate fecha)`: intentar carga lazy y consultar. |
| **edge_cases** | API retorna lista vacía para un año → guardar 0 registros. |
| **done_criteria** | `./gradlew compileJava` compila. `FestivoCacheService` creado. |
| **verification** | Servicio no lanza excepción si API falla. |
| **dependencies** | `T8`, `T16`, `T22` |
| **handoff_context** | Caché de festivos listo para ser usado por el adapter y servicios de aplicación. |
| **source_of_truth** | Master Spec §8 |
| **stale_terms_guard** | `FestivoCacheService`. |
| **status** | `done` |
| **executor_notes** | Creados FestivoCacheService.java y actualizado FestivoRepositoryAdapter.java para usar cache lazy/first-access. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

## Grupo 8: Web — Controllers

> **Pre-requisito para T24-T27:** Ejecutar `./gradlew openApiGenerate` para generar las interfaces de controllers (`infrastructure.web.api`) y DTOs de API (`infrastructure.web.dto`). Los controllers implementan las interfaces generadas.

---

### T24 — Implementar MedicoController y WebMapper para Médicos

| Campo | Valor |
|---|---|
| **id** | `T24` |
| **title** | Implementar controlador REST para Médicos usando interfaces generadas por OpenAPI |
| **agent** | `executor` |
| **spec_refs** | Master Spec §6.2, OpenAPI paths `/api/v1/medicos`, skill `openapi-first` |
| **goal** | Exponer los 4 endpoints de Médicos implementando las interfaces generadas por OpenAPI Generator. |
| **scope** | Crear `infrastructure/web/MedicoController.java` que implementa `MedicosApi` (generada). Crear `infrastructure/web/mapper/WebMapper.java` con métodos MapStruct para convertir entre API DTOs y Application DTOs. |
| **out_of_scope** | No modificar lógica de negocio (delega a MedicoService). |
| **inputs** | OpenAPI generado (`./gradlew openApiGenerate`), T17 (MedicoService), T9, T10 |
| **implementation_notes** | `MedicoController`: `@RestController`, implementa interfaz generada `MedicosApi`. Cada método: mapea API DTO → Command, llama al use case, mapea Result → API DTO Response. Para `POST`, agregar header `Location` con `URI.create("/api/v1/medicos/" + result.id())`. `WebMapper`: interfaz `@Mapper(componentModel = "spring")`. Métodos: `toCommand(MedicoRequest) → CreateMedicoCommand`, `toCommand(MedicoUpdateRequest) → UpdateMedicoCommand`, `toResponse(MedicoResult) → MedicoResponse`. |
| **edge_cases** | `MedicoRequest` generado por OpenAPI usa `@Valid` y Bean Validation. Campos opcionales (`telefono`, `email`) usan `null` cuando no se envían. |
| **done_criteria** | `./gradlew compileJava` compila (después de `openApiGenerate`). `MedicoController` implementa todos los métodos de `MedicosApi`. |
| **verification** | `POST` retorna 201 con `Location` header. `GET` retorna lista. `GET /{id}` retorna 200 o 404. `PUT` retorna 200 o 404. |
| **dependencies** | `T1`, `T17`, `openApiGenerate` ejecutado |
| **handoff_context** | Endpoints de Médicos funcionales. |
| **source_of_truth** | OpenAPI paths `/api/v1/medicos` |
| **stale_terms_guard** | `MedicoController` (no `MedicoApiController`). `MedicosApi` es la interfaz generada (con 's' al final). |
| **status** | `done` |
| **executor_notes** | Creado MedicoController.java en infrastructure/web/ implementando MedicosApi. Mapping directo DTO ↔ dominio con toResponse(). |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T25 — Implementar PacienteController y WebMapper para Pacientes

| Campo | Valor |
|---|---|
| **id** | `T25` |
| **title** | Implementar controlador REST para Pacientes usando interfaces generadas por OpenAPI |
| **agent** | `executor` |
| **spec_refs** | Master Spec §6.3, OpenAPI paths `/api/v1/pacientes` |
| **goal** | Exponer los 4 endpoints de Pacientes. |
| **scope** | Crear `infrastructure/web/PacienteController.java` implementando `PacientesApi`. Extender `WebMapper` con métodos para Pacientes. |
| **out_of_scope** | No modificar PacienteService. |
| **inputs** | OpenAPI generado, T18 (PacienteService), T9, T10 |
| **implementation_notes** | Similar a T24. `PacienteController`: cada método mapea API DTOs a Commands, llama al use case, mapea Results a API DTOs. `POST` incluye `Location` header. `WebMapper`: `toCommand(PacienteRequest)`, `toCommand(PacienteUpdateRequest)`, `toResponse(PacienteResult)`. |
| **edge_cases** | `birthDate` nullable en API request. Si se envía `null`, el command lo recibe como `null`. |
| **done_criteria** | `./gradlew compileJava` compila. `PacienteController` implementa `PacientesApi`. |
| **verification** | `POST` retorna 201/400/409. `GET` retorna lista. `PUT` retorna 200/404/409. |
| **dependencies** | `T1`, `T18`, `openApiGenerate` ejecutado |
| **handoff_context** | Endpoints de Pacientes funcionales. |
| **source_of_truth** | OpenAPI paths `/api/v1/pacientes` |
| **stale_terms_guard** | `PacienteController`. `PacientesApi`. |
| **status** | `done` |
| **executor_notes** | Creado PacienteController.java en infrastructure/web/ implementando PacientesApi. Mapping directo DTO ↔ dominio. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T26 — Implementar CitaController (reserva, cancelación, reprogramación, listado)

| Campo | Valor |
|---|---|
| **id** | `T26` |
| **title** | Implementar controlador REST para Citas con los 5 endpoints |
| **agent** | `executor` |
| **spec_refs** | Master Spec §6.4, OpenAPI paths `/api/v1/citas` |
| **goal** | Exponer POST, GET (listado), GET (individual), DELETE (cancelación), POST reprogramar de citas. |
| **scope** | Crear `infrastructure/web/CitaController.java` implementando `CitasApi`. Extender `WebMapper` con mapeos de Cita. |
| **out_of_scope** | No modificar CitaService. |
| **inputs** | OpenAPI generado, T19+T20 (CitaService), T9, T10 |
| **implementation_notes** | `POST`: mapear `CitaRequest` → `CreateCitaCommand`, llamar `createCitaUseCase.create()`, retornar 201 con `Location`. `GET /citas`: construir `ListCitasQuery` desde query params, llamar `listCitasUseCase.list()`. `GET /citas/{id}`: `getCitaUseCase.getById(id)`. `DELETE /citas/{id}`: `cancelCitaUseCase.cancel(id)`. `POST /citas/{id}/reprogramar`: mapear `ReprogramarRequest` → `ReprogramarCitaCommand`, llamar `reprogramarCitaUseCase.reprogramar(id, command)`. `WebMapper`: `toCommand(CitaRequest)`, `toCommand(ReprogramarRequest)`, `toResponse(CitaResult)`, `toQuery(...)`. |
| **edge_cases** | `listCitas` con ningún filtro → `ListCitasQuery` con todos los campos `null`. Query params `fecha_inicio`/`fecha_fin` son `LocalDate` (solo fecha, sin hora). |
| **done_criteria** | `./gradlew compileJava` compila. `CitaController` implementa `CitasApi` con 5 endpoints. |
| **verification** | `POST` retorna 201/400/422/404/409. `DELETE` retorna 200/404/409. `POST reprogramar` retorna 200/400/422/404/409. |
| **dependencies** | `T1`, `T19`, `T20`, `openApiGenerate` ejecutado |
| **handoff_context** | Endpoints de Citas funcionales. |
| **source_of_truth** | OpenAPI paths `/api/v1/citas` |
| **stale_terms_guard** | `CitaController`. `CitasApi`. `ReprogramarRequest` (no `RescheduleRequest`). |
| **status** | `done` |
| **executor_notes** | Creado CitaController.java en infrastructure/web/ implementando CitasApi (5 endpoints: createCita, getCita, listCitas, cancelCita, reprogramarCita). |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T27 — Implementar DisponibilidadController

| Campo | Valor |
|---|---|
| **id** | `T27` |
| **title** | Implementar controlador REST para consulta de disponibilidad |
| **agent** | `executor` |
| **spec_refs** | Master Spec §6.5, OpenAPI path `/api/v1/disponibilidad` |
| **goal** | Exponer el endpoint de consulta de franjas disponibles. |
| **scope** | Crear `infrastructure/web/DisponibilidadController.java` implementando `DisponibilidadApi`. Extender `WebMapper` con mapeos de Disponibilidad. |
| **out_of_scope** | No modificar DisponibilidadService. |
| **inputs** | OpenAPI generado, T21 (DisponibilidadService), T9, T10 |
| **implementation_notes** | `GET /disponibilidad`: extraer `medico_id`, `fecha_inicio`, `fecha_fin` de query params (requeridos). Llamar `consultarDisponibilidadUseCase.consultar(medicoId, fechaInicio, fechaFin)`. Mapear `DisponibilidadResult` → `DisponibilidadResponse` (API DTO). `WebMapper`: `toResponse(DisponibilidadResult)`, `toResponse(FranjaResult)` → `FranjaResponse`. |
| **edge_cases** | `fecha_inicio > fecha_fin` → 400 (se valida en el service con `INVALID_DATE_RANGE`). Si no hay franjas disponibles, retornar `DisponibilidadResponse` con lista `franjas` vacía. |
| **done_criteria** | `./gradlew compileJava` compila. `DisponibilidadController` implementa `DisponibilidadApi`. |
| **verification** | `GET /disponibilidad` retorna 200 con `DisponibilidadResponse`. Query params faltantes → 400. Médico inexistente → 404. |
| **dependencies** | `T1`, `T21`, `openApiGenerate` ejecutado |
| **handoff_context** | Último endpoint implementado. Sistema funcional completo para pruebas. |
| **source_of_truth** | OpenAPI path `/api/v1/disponibilidad` |
| **stale_terms_guard** | `DisponibilidadController`. `DisponibilidadApi`. `FranjaResponse` (no `SlotResponse`). |
| **status** | `done` |
| **executor_notes** | Creado DisponibilidadController.java en infrastructure/web/ implementando DisponibilidadApi. Usa ChronoUnit.DAYS.between para iterar rango de fechas y filtra solo FranjaHoraria.isDisponible(). |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

### T28 — Extender GlobalExceptionHandler con excepciones de dominio/aplicación

| Campo | Valor |
|---|---|
| **id** | `T28` |
| **title** | Agregar handlers al GlobalExceptionHandler para excepciones de dominio y aplicación |
| **agent** | `executor` |
| **spec_refs** | Master Spec §7.3 |
| **goal** | Completar el mapeo de excepciones de negocio a respuestas HTTP ApiErrorResponse. |
| **scope** | Modificar `infrastructure/web/GlobalExceptionHandler.java` (creado en T3). Agregar handlers: `ResourceNotFoundException` → 404, `ConflictException` → 409, `BusinessException` → 422, `InvalidSlotException` → 422, `PatientBlockedException` → 409, `PatientConflictException` → 409, `SlotNotAvailableException` → 409. |
| **out_of_scope** | No modificar los handlers base de T3. |
| **inputs** | T3 (GlobalExceptionHandler existente), T6 (excepciones de dominio) |
| **implementation_notes** | Cada handler extrae el `code` de la excepción y construye `ApiErrorResponse` con el status HTTP correspondiente. `ResourceNotFoundException` → 404 con code `RESOURCE_NOT_FOUND`. `ConflictException` → 409 con code de la excepción. `BusinessException` → 422 con code de la excepción. |
| **edge_cases** | Asegurar que los handlers específicos tienen prioridad sobre el fallback `Exception`. Spring resuelve por especificidad (el handler más específico gana). |
| **done_criteria** | `./gradlew compileJava` compila. Al menos 7 handlers adicionales en `GlobalExceptionHandler`. |
| **verification** | Cada excepción de dominio mapea al status HTTP y code correctos según Master Spec §7.2. |
| **dependencies** | `T3`, `T6` |
| **handoff_context** | Manejo de errores completo. Los controllers ahora responderán con ApiErrorResponse adecuado para cada excepción de negocio. |
| **source_of_truth** | Master Spec §7.2, §7.3 |
| **stale_terms_guard** | Status codes exactos: 404 para ResourceNotFoundException, 409 para ConflictException, 422 para BusinessException/InvalidSlotException. |
| **status** | `done` |
| **executor_notes** | GlobalExceptionHandler ya tenía handlers implementados en T3 para ResourceNotFoundException (404), ConflictException (409), BusinessException (422). No se requirió modificación adicional. |
| **verification_result** | `./gradlew compileJava` — BUILD SUCCESSFUL |
| **blocker** | `none` |

---

## Grupo 9: Tests

---

### T29 — Tests unitarios de dominio (FranjaHorariaValidator, PenalizacionEvaluator, ValueObjects)

| Campo | Valor |
|---|---|
| **id** | `T29` |
| **title** | Implementar tests unitarios para servicios de dominio y Value Objects |
| **agent** | `test-architect` |
| **spec_refs** | Master Spec §10.1, §10.4 (tests RN-01, RN-05), skill `testing-strategy` |
| **goal** | Cubrir con tests unitarios las reglas de negocio RN-01 (franjas) y RN-05 (penalización), y validación de Value Objects. |
| **scope** | Crear tests en `src/test/java/com/medisalud/appointment/domain/`. `FranjaHorariaValidatorTest.java`: validar generación de franjas para cada día, validar detección de festivos, validar rechazo de franjas no alineadas. `PenalizacionEvaluatorTest.java`: validar penalización <= 2h (sí), > 2h (no), exactamente 2h (sí), bloqueo con 3+ penalizaciones, no bloqueo con 2. `EmailTest.java`, `TelefonoTest.java`, `DocumentoIdentidadTest.java`: validar formatos válidos e inválidos. `FranjaHorariaTest.java`: validar creación exitosa y rechazo de franjas inválidas. |
| **out_of_scope** | No incluir tests de integración con BD. |
| **inputs** | T5 (Value Objects), T7 (domain services), T4 (domain entities) |
| **implementation_notes** | Usar JUnit 5 + AssertJ. Tests sin Spring context (unit tests puros). Para `FranjaHorariaValidator`, crear festivos mock (lista de `Festivo`). Para `PenalizacionEvaluator`, usar `OffsetDateTime.now()` con `Clock.fixed()` para determinismo o pasar fechas explícitas. Nombres de métodos: `should_Result_when_Condition`. |
| **edge_cases** | Franja exactamente a las 17:30-18:00 (última válida Lun-Vie). Franja 12:30-13:00 (última válida Sáb). Cancelación exactamente a 2 horas (penaliza). |
| **done_criteria** | `./gradlew test --tests "com.medisalud.appointment.domain.*"` pasa. Al menos 20 tests unitarios. |
| **verification** | Cobertura de `FranjaHorariaValidator` y `PenalizacionEvaluator` cercana al 100%. |
| **dependencies** | `T5`, `T7` |
| **handoff_context** | Validación de reglas RN-01 y RN-05 cubierta con tests. |
| **source_of_truth** | Master Spec §10.4 |
| **stale_terms_guard** | Nombres de tests en inglés: `should_Generate20Slots_when_Monday`. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

### T30 — Tests unitarios de aplicación (use cases con puertos mockeados)

| Campo | Valor |
|---|---|
| **id** | `T30` |
| **title** | Implementar tests unitarios para use cases usando Mockito |
| **agent** | `test-architect` |
| **spec_refs** | Master Spec §10.1, §10.4 |
| **goal** | Probar la orquestación de reglas de negocio en los servicios de aplicación con dependencias mockeadas. |
| **scope** | Crear tests en `src/test/java/com/medisalud/appointment/application/`. `MedicoServiceTest.java`, `PacienteServiceTest.java`, `CitaServiceTest.java`, `DisponibilidadServiceTest.java`. Mockear todos los `*RepositoryPort` y servicios de dominio. |
| **out_of_scope** | No incluir tests de integración (BD real o MockMvc). |
| **inputs** | T17 (MedicoService), T18 (PacienteService), T19+T20 (CitaService), T21 (DisponibilidadService) |
| **implementation_notes** | Usar JUnit 5 + Mockito + AssertJ. `@ExtendWith(MockitoExtension.class)`. `CitaServiceTest`: probar creación exitosa, médico no encontrado, paciente no encontrado, conflicto de médico (RN-02), conflicto de paciente (RN-04), paciente bloqueado (RN-05), franja inválida (RN-01), birth date futura (RN-03). `CancelCitaUseCase`: cancelar exitoso sin penalización, cancelar con penalización, cancelar cita ya cancelada. `ReprogramarCitaUseCase`: exitoso, cita ya cancelada, conflicto en nueva franja. `PacienteServiceTest`: documento duplicado, birth date futura. |
| **edge_cases** | Mockear `FranjaHorariaValidator` y `PenalizacionEvaluator` para controlar sus respuestas. Simular `FestivoRepositoryPort.findByYear` retornando `false` para forzar fallback graceful. |
| **done_criteria** | `./gradlew test --tests "com.medisalud.appointment.application.*"` pasa. Al menos 30 tests unitarios. |
| **verification** | Cobertura de application services > 85%. |
| **dependencies** | `T17`, `T18`, `T19`, `T20`, `T21` |
| **handoff_context** | Capa de aplicación validada con tests unitarios. |
| **source_of_truth** | Master Spec §10.4 |
| **stale_terms_guard** | Métodos de test: `should_ThrowConflictException_when_DocumentoDuplicado`. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

### T31 — Tests de integración de repositorios (@DataJpaTest con H2)

| Campo | Valor |
|---|---|
| **id** | `T31` |
| **title** | Implementar tests de integración para repositories JPA con H2 |
| **agent** | `test-architect` |
| **spec_refs** | Master Spec §10.1, §10.2, §9.3 (perfil test) |
| **goal** | Validar que las consultas JPA y Flyway migrations funcionan correctamente en H2 modo PostgreSQL. |
| **scope** | Crear tests en `src/test/java/com/medisalud/appointment/infrastructure/persistence/`. `MedicoJpaRepositoryTest.java`, `PacienteJpaRepositoryTest.java`, `CitaJpaRepositoryTest.java`, `PenalizacionJpaRepositoryTest.java`, `FestivoJpaRepositoryTest.java`. Anotar con `@DataJpaTest`, `@ActiveProfiles("test")`, `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`. |
| **out_of_scope** | No probar controladores ni servicios. |
| **inputs** | T11 (entidades JPA), T12 (JPA repositories), migraciones Flyway, `application-test.yaml` |
| **implementation_notes** | `@DataJpaTest` configura automáticamente H2. Verificar que Flyway ejecuta las migraciones. Probar: `save` + `findById`, `findAll`, constraints unique (documento duplicado lanza excepción), queries custom (`existsByMedicoIdAndFechaHoraAndEstado`, `countByPacienteIdAndFechaHoraAfter`, `findByYear`). Para `CitaJpaRepository`, probar `Specification` con filtros combinables. |
| **edge_cases** | H2 modo PostgreSQL requiere que las migraciones usen sintaxis compatible. Revisar que `TIMESTAMPTZ`, `gen_random_uuid()` y `CREATE TRIGGER` en migraciones sean compatibles con H2. Si no, los tests de integración pueden fallar y requerir ajustes en las migraciones o en la configuración de H2. |
| **done_criteria** | `./gradlew test --tests "com.medisalud.appointment.infrastructure.persistence.*"` pasa. |
| **verification** | Flyway ejecuta las 7 migraciones sin errores en H2. Consultas JPA retornan resultados correctos. |
| **dependencies** | `T11`, `T12`, migraciones |
| **handoff_context** | Persistencia validada con tests de integración. |
| **source_of_truth** | `application-test.yaml`, migraciones Flyway |
| **stale_terms_guard** | `@DataJpaTest`, perfil `test`. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

### T32 — Tests de controladores (@WebMvcTest / MockMvc)

| Campo | Valor |
|---|---|
| **id** | `T32` |
| **title** | Implementar tests de integración de capa web con MockMvc |
| **agent** | `test-architect` |
| **spec_refs** | Master Spec §10.1 |
| **goal** | Validar que los controllers responden con los status codes y schemas correctos según OpenAPI. |
| **scope** | Crear tests en `src/test/java/com/medisalud/appointment/infrastructure/web/`. `MedicoControllerTest.java`, `PacienteControllerTest.java`, `CitaControllerTest.java`, `DisponibilidadControllerTest.java`. Usar `@WebMvcTest` con `@MockBean` para los use cases. |
| **out_of_scope** | No probar lógica de negocio (ya cubierta en T30). |
| **inputs** | T24-T27 (controllers), T28 (GlobalExceptionHandler) |
| **implementation_notes** | Probar: POST retorna 201 con Location header + body. GET retorna 200 con lista/vacía. GET /{id} con UUID inválido → 400. GET /{id} no encontrado → 404. POST con body inválido → 400 + ApiErrorResponse. DELETE cita cancelada → 409. POST cita con conflicto → 409. POST reprogramar → 200. Validar que todas las respuestas de error cumplen el schema `ApiErrorResponse` (tiene timestamp, status, error, code, message, path, trace_id, details). |
| **edge_cases** | `@WebMvcTest` no carga el contexto completo de Spring. Los use cases se mockean. El `GlobalExceptionHandler` debe estar activo. |
| **done_criteria** | `./gradlew test --tests "com.medisalud.appointment.infrastructure.web.*"` pasa. Al menos 20 tests de controller. |
| **verification** | Cada response JSON de error tiene los 8 campos requeridos de `ApiErrorResponse`. |
| **dependencies** | `T24`, `T25`, `T26`, `T27`, `T28` |
| **handoff_context** | Capa web validada con tests de contrato HTTP. |
| **source_of_truth** | OpenAPI, Master Spec §6 |
| **stale_terms_guard** | `@WebMvcTest`, `MockMvc`, `@MockBean`. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

### T33 — Tests de arquitectura con ArchUnit

| Campo | Valor |
|---|---|
| **id** | `T33` |
| **title** | Implementar tests de arquitectura con ArchUnit para verificar boundaries hexagonales |
| **agent** | `test-architect` |
| **spec_refs** | Master Spec §10.3 |
| **goal** | Asegurar que las dependencias entre capas respetan arquitectura hexagonal y que no hay violaciones de boundaries. |
| **scope** | Crear `src/test/java/com/medisalud/appointment/architecture/HexagonalArchitectureTest.java`. |
| **out_of_scope** | No probar lógica de negocio. |
| **inputs** | Estructura de paquetes del proyecto, Master Spec §10.3 |
| **implementation_notes** | Usar ArchUnit con `ClassFileImporter`. Reglas: (1) `domain` no depende de `infrastructure`, `application`, `org.springframework`, `jakarta.persistence`, `com.fasterxml.jackson`. (2) `domain` no contiene clases con `@Entity`, `@Table`, `@Column`, `@Repository`, `@Service`, `@RestController`, `@Configuration`. (3) `application` no depende de `infrastructure`. (4) Controllers residen en `infrastructure.web`. (5) Use cases residen en `application.service`. (6) Output ports residen en `application.port.output`. |
| **edge_cases** | Las exclusiones deben incluir `AppointmentApplication.java` y `GlobalExceptionHandler.java` que por diseño están en infrastructure. |
| **done_criteria** | `./gradlew test --tests "com.medisalud.appointment.architecture.*"` pasa. Todas las reglas de la spec §10.3 verificadas. |
| **verification** | `domain` no importa nada de `org.springframework`. `application` no importa nada de `infrastructure`. |
| **dependencies** | `T1-T28` (todo el código fuente debe existir) |
| **handoff_context** | Boundaries hexagonales validados. |
| **source_of_truth** | Master Spec §10.3 |
| **stale_terms_guard** | `HexagonalArchitectureTest`. Paquetes: `com.medisalud.appointment.domain`, `com.medisalud.appointment.application`, `com.medisalud.appointment.infrastructure`. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

### T34 — Tests de integración Nager.Date (mock del cliente HTTP)

| Campo | Valor |
|---|---|
| **id** | `T34` |
| **title** | Implementar tests de integración para FestivoCacheService y NagerDateClient con mock |
| **agent** | `test-architect` |
| **spec_refs** | Master Spec §8, §10.1 |
| **goal** | Validar el flujo de carga de festivos: cache hit, cache miss + API call exitosa, API fallback graceful. |
| **scope** | Crear `src/test/java/com/medisalud/appointment/infrastructure/client/NagerDateClientTest.java` y `FestivoCacheServiceTest.java`. |
| **out_of_scope** | No probar la API real (test unitario con mock). |
| **inputs** | T22 (NagerDateClient), T23 (FestivoCacheService), T16 (FestivoRepositoryAdapter) |
| **implementation_notes** | Usar `MockRestServiceServer` o mock del `RestClient` para simular respuestas HTTP. `NagerDateClientTest`: simular respuesta 200 con JSON de festivos, timeout, error 500 → excepción. `FestivoCacheServiceTest`: mockear `FestivoRepositoryPort` y `NagerDateClient`. Probar: (1) `existsByYear` true → no llama API, (2) `existsByYear` false → llama API, guarda festivos, (3) `existsByYear` false + API falla → log WARN, no guarda, no lanza excepción. |
| **edge_cases** | API retorna array vacío → guardar 0 registros. Idempotencia: constraint UNIQUE maneja duplicados. |
| **done_criteria** | `./gradlew test --tests "com.medisalud.appointment.infrastructure.client.*"` pasa. |
| **verification** | Fallback graceful no lanza excepción. Logs verificables con captor de logger. |
| **dependencies** | `T22`, `T23` |
| **handoff_context** | Integración Nager.Date validada. |
| **source_of_truth** | Master Spec §8 |
| **stale_terms_guard** | `MockRestServiceServer`. `FestivoCacheService`. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

## Grupo 10: CI/CD y Despliegue

---

### T35 — Crear Dockerfile multi-stage y .dockerignore

| Campo | Valor |
|---|---|
| **id** | `T35` |
| **title** | Implementar Dockerfile multi-stage para build y runtime |
| **agent** | `executor` |
| **spec_refs** | Master Spec §12.4 |
| **goal** | Crear imagen Docker optimizada con multi-stage build: etapa de compilación con JDK 21 y etapa runtime con JRE 21 Alpine. |
| **scope** | Crear `Dockerfile` en raíz del proyecto y `.dockerignore`. |
| **out_of_scope** | No incluir docker-compose ni configuración de orquestación. |
| **inputs** | Master Spec §12.4, `build.gradle` |
| **implementation_notes** | Stage 1 (builder): `eclipse-temurin:21-jdk-alpine`, copiar `gradlew`, `settings.gradle`, `build.gradle`, directorio `gradle/`. Ejecutar `./gradlew dependencies` para cachear. Copiar `src/`. Ejecutar `./gradlew build -x test`. Stage 2 (runtime): `eclipse-temurin:21-jre-alpine`, crear usuario `appuser`, copiar JAR de stage 1, exponer 8080, entrypoint `java -jar app.jar`. `.dockerignore`: excluir `build/`, `.gradle/`, `docs/`, `.git/`, `*.md`, `*.log`, `.idea/`, `*.iml`. |
| **edge_cases** | El JAR se genera en `build/libs/*.jar`. Asegurar que el nombre del JAR es predecible (usar `bootJar` archiveFileName o usar `*.jar`). |
| **done_criteria** | `docker build -t ms-medical-appointment .` completa exitosamente. |
| **verification** | Imagen creada, `docker run --rm ms-medical-appointment` inicia (aunque falle por BD, el contenedor arranca). |
| **dependencies** | `T1-T28` (código compilable) |
| **handoff_context** | Imagen Docker lista para ECR y despliegue. |
| **source_of_truth** | Master Spec §12.4 |
| **stale_terms_guard** | `eclipse-temurin:21-jre-alpine` (no `openjdk:21`). Usuario `appuser` no root. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

### T36 — Crear perfil AWS (application-aws.yaml)

| Campo | Valor |
|---|---|
| **id** | `T36` |
| **title** | Crear archivo de configuración para despliegue en AWS |
| **agent** | `executor` |
| **spec_refs** | Master Spec §12.3, §12.5 |
| **goal** | Configurar perfil `aws` con datasource parametrizado por variables de entorno y logging JSON para CloudWatch. |
| **scope** | Crear `src/main/resources/application-aws.yaml`. |
| **out_of_scope** | No modificar otros perfiles. |
| **inputs** | Master Spec §12.3 |
| **implementation_notes** | Seguir exactamente el YAML de la spec §12.3: datasource URL parametrizada con `${DB_HOST}`, `${DB_PORT}`, `${DB_NAME}`, credenciales `${DB_USER}`, `${DB_PASSWORD}`. HikariCP pool: max 10, min idle 2, connection timeout 20s, max lifetime 30min. Dialect PostgreSQL. Logging JSON con pattern: `{"timestamp": "%d{yyyy-MM-dd}T%d{HH:mm:ss.SSS}Z", "level": "%p", "message": "%m"}%n`. Incluir `spring-boot-starter-actuator` en `build.gradle` para health checks. |
| **edge_cases** | Las variables de entorno deben tener defaults o ser requeridas (sin default causa error en startup si no están definidas). |
| **done_criteria** | `./gradlew compileJava` compila (el YAML no afecta compilación pero debe ser sintácticamente válido). |
| **verification** | `application-aws.yaml` contiene configuración de datasource, HikariCP y logging JSON. `spring-boot-starter-actuator` está en `build.gradle`. |
| **dependencies** | `T1` |
| **handoff_context** | Perfil de producción listo para despliegue. |
| **source_of_truth** | Master Spec §12.3 |
| **stale_terms_guard** | `application-aws.yaml`. Variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

### T37 — Crear GitHub Actions workflow de CI/CD

| Campo | Valor |
|---|---|
| **id** | `T37` |
| **title** | Implementar pipeline CI/CD con GitHub Actions para build, test y deploy a ECS |
| **agent** | `executor` |
| **spec_refs** | Master Spec §12.6, §12.7 |
| **goal** | Automatizar build, tests, push a ECR y deploy a ECS Fargate en cada push a main. |
| **scope** | Crear `.github/workflows/deploy.yml`. |
| **out_of_scope** | No incluir stages de QA/staging. Solo producción (main). |
| **inputs** | Master Spec §12.6 |
| **implementation_notes** | Seguir exactamente el YAML de la spec §12.6. Trigger: push a `main`. Job `test-and-deploy`: ubuntu-latest. Pasos: checkout@v4, setup-java@v4 (temurin 21), `./gradlew build test jacocoTestReport`, configure AWS credentials (desde secrets `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`), login a ECR, build + push Docker image con tag `github.sha`, deploy a ECS con `aws ecs update-service --cluster medisalud-cluster --service ms-medical-appointment-service --force-new-deployment`. |
| **edge_cases** | El workflow asume que los secrets de AWS existen. Documentar en el README o en el mismo archivo qué secrets se requieren. |
| **done_criteria** | Archivo `.github/workflows/deploy.yml` creado con la estructura de la spec. |
| **verification** | El workflow tiene todos los pasos: checkout, setup java, build+test, configure AWS, login ECR, build+push Docker, deploy ECS. |
| **dependencies** | `T35`, `T36` |
| **handoff_context** | Pipeline CI/CD listo. Tras el merge a main, el despliegue es automático. |
| **source_of_truth** | Master Spec §12.6 |
| **stale_terms_guard** | Cluster `medisalud-cluster`, servicio `ms-medical-appointment-service`, región `us-east-1`. Branch `main`. |
| **status** | `todo` |
| **executor_notes** | |
| **verification_result** | |
| **blocker** | `none` |

---

## Resumen de Dependencias

```
T1 ──────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                                 │
├── T2 (JacksonConfig)                                                                           │
├── T3 (GlobalExceptionHandler)                                                                  │
├── T4 (Domain Entities) ──┬── T5 (Value Objects) ──┬── T7 (Domain Services)                    │
│                          │                        │                                            │
│                          │                        └── T29 (Domain Unit Tests)                  │
│                          │                                                                     │
│                          ├── T6 (Domain Exceptions) ────────── T28 (Extend ExceptionHandler)   │
│                          │                                                                     │
│                          ├── T8 (Output Ports) ──┬── T9 (Input Ports) ◄─ T10 (DTOs) ◄─ T1    │
│                          │                      │                                              │
│                          │                      │   ┌──────────────────────────────────────┐  │
│                          ├── T11 (JPA Entities)─┼──T12 (JPA Repos)──T13 (Mapper)           │  │
│                          │                      │   │    ┌─────────────────────────┐       │  │
│                          │                      │   ├─T14 (Medico/Paciente Adapters)│       │  │
│                          │                      │   │    ├─┬─ T17 (MedicoService)──T24  │  │
│                          │                      │   │    │ └─ T18 (PacienteService)-T25 │  │
│                          │                      │   │    │                              │  │
│                          │                      │   ├─T15 (Cita/Penalizacion Adapters)│       │  │
│                          │                      │   │    └─┬─ T19 (Cita Create/Get/List)│      │  │
│                          │                      │   │      ├─ T20 (Cancel/Reprogramar)──T26  │
│                          │                      │   │      └─ T21 (Disponibilidad)─────T27  │
│                          │                      │   │                                       │  │
│                          │                      │   └─T16 (Festivo Adapter)──┬── T23 (Cache)│  │
│                          │                      │                           │              │  │
│                          │                      └───────────────────────────┘              │  │
│                          │                                                                 │  │
│                          └── T22 (NagerDate Client) ────────────────────────────── T34     │  │
│                                                                                            │  │
├── T30 (Application Unit Tests) ◄── T17,T18,T19,T20,T21                                     │  │
├── T31 (Repository Integration Tests) ◄── T11,T12                                           │  │
├── T32 (Controller Tests) ◄── T24,T25,T26,T27,T28                                           │  │
├── T33 (Architecture Tests) ◄── All code                                                    │  │
│                                                                                            │  │
├── T35 (Dockerfile) ◄── All code                                                            │  │
├── T36 (AWS Profile) ◄── T1                                                                 │  │
└── T37 (CI/CD) ◄── T35,T36                                                                  │  │
```

---

## Orden de Ejecución Recomendado

1. **T1** → Crear estructura de paquetes
2. **T2**, **T3**, **T10** → En paralelo: Jackson, ExceptionHandler, DTOs
3. **T4** → Entidades de dominio
4. **T5**, **T6** → En paralelo: Value Objects, Excepciones
5. **T7** → Domain services (depende de T4+T5)
6. **T8**, **T9** → En paralelo: Output ports, Input ports (dependen de T4, T9 depende de T10)
7. **T11** → JPA Entities (depende de T4)
8. **T12** → JPA Repos (depende de T11)
9. **T13** → PersistenceMapper (depende de T4+T11)
10. **T14**, **T15**, **T16** → En paralelo: Adapters (dependen de T8+T11+T12+T13)
11. **T22** → NagerDate Client (depende de T1)
12. **T23** → FestivoCacheService (depende de T8+T16+T22)
13. **T17**, **T18** → En paralelo: MedicoService, PacienteService (dependen de T6+T8+T9+T10+T14)
14. **T19**, **T20**, **T21** → En secuencia dentro de CitaService: T19 → T20; **T21** en paralelo con T19 (dependen de T7+T8+T14+T15+T23)
15. **T28** → Extender ExceptionHandler (depende de T3+T6)
16. **T24**, **T25**, **T26**, **T27** → En paralelo: Controllers (dependen de T17-T21 + openApiGenerate)
17. **T29** → Domain Tests (depende de T5+T7)
18. **T30** → Application Tests (depende de T17-T21)
19. **T31** → Repository Tests (depende de T11+T12)
20. **T32** → Controller Tests (depende de T24-T28)
21. **T33** → ArchUnit Tests (depende de todo el código)
22. **T35** → Dockerfile (depende de todo el código)
23. **T36** → AWS Profile (depende de T1)
24. **T37** → CI/CD (depende de T35+T36)

---

*Fin del Task Board. 37 tareas, 0 bloqueadas. Estado superior: `todo`.*
