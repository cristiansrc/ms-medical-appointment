# MediSalud - Sistema de Agendamiento de Citas Medicas

API REST para agendar citas medicas con validacion de reglas de negocio, gestion de penalizaciones por cancelacion tardia e integracion con la API de festivos de Colombia (Nager.Date).

---

## Tabla de Contenido

- [Spec Driven Development](#spec-driven-development)
- [Tecnologias](#tecnologias)
- [Arquitectura](#arquitectura)
- [Decisiones Tecnicas](#decisiones-tecnicas)
- [Cobertura de Pruebas (JaCoCo)](#cobertura-de-pruebas-jacoco)
- [Tests](#tests)
- [Instalacion y Ejecucion Local](#instalacion-y-ejecucion-local)
- [Endpoints y Ejemplos](#endpoints-y-ejemplos)
- [Manejo de Errores](#manejo-de-errores)
- [Despliegue en AWS](#despliegue-en-aws)
- [Mejoras Propuestas](#mejoras-propuestas)

## Spec Driven Development

Este proyecto se desarrollo siguiendo la metodologia **Spec Driven Development (SDD)**, un enfoque donde las especificaciones detalladas son la fuente de verdad unica durante todo el ciclo de vida del desarrollo.

### ¿Por que SDD?

- **Base de conocimiento unificada:** Toda la informacion del proyecto (requerimientos, decisiones tecnicas, reglas de negocio, contratos API) esta documentada en archivos estructurados dentro del repositorio. Esto sirve como base de conocimiento tanto para desarrolladores humanos como para modelos de IA que necesiten familiarizarse con el proyecto.
- **Planificacion estructurada:** Antes de escribir codigo, se definen los requerimientos (Requirements Brief), se disena la arquitectura y los contratos (Master Spec + OpenAPI), y se descomponen las tareas (Task Board). Esto reduce la ambiguedad y los errores de implementacion.
- **Decisiones documentadas:** Cada decision tecnica y arquitectonica (D-01 a D-20) esta registrada en la Master Spec con su justificacion. Esto permite entender el "por que" detras de cada eleccion, no solo el "que".
- **Desarrollo asistido por IA:** Durante la fase de implementacion, los agentes de IA utilizan estos documentos como parte de su contexto (prompt). Al tener toda la informacion disponible en archivos de documentacion, el agente puede concentrarse en escribir codigo que cumpla exactamente con lo especificado, sin necesidad de inferir decisiones no documentadas.

### Archivos de documentacion en el repositorio

| Archivo | Proposito |
|---|---|
| `docs/enunciado-prueba-java.pdf` | Enunciado original de la prueba tecnica (PDF). Define los requerimientos funcionales, reglas de negocio, stack tecnologico y entregables obligatorios. |
| `docs/specs/requirements/medical-appointment-requirements-brief.md` | **Requirements Brief.** Analisis detallado de los requerimientos del PDF. Incluye alcance (RF-01 a RF-06), reglas de negocio (RN-01 a RN-06), entidades funcionales, edge cases, criterios de aceptacion, preguntas resueltas y supuestos. Es el insumo para el Planner. |
| `docs/specs/master-spec.md` | **Master Spec.** Documento maestro que contiene: stack tecnologico, arquitectura hexagonal, modelo de datos completo, contratos de API (14 endpoints), estrategia de errores (ApiErrorResponse), reglas de negocio detalladas, integracion con Nager.Date, despliegue en AWS ECS, decisiones tecnicas (D-01 a D-20), criterios de aceptacion del incremento y mejoras propuestas. Es la fuente de verdad para la implementacion. |
| `docs/specs/tasks/medical-appointment-task-board.md` | **Task Board.** Desglose de 37 tareas atomicas organizadas en 10 grupos (Setup, Dominio, Puertos, Persistencia, Servicios, Integracion, Web, Tests, CI/CD). Cada tarea incluye objetivo, alcance, criterios de aceptacion, dependencias y estado. |
| `docs/specs/.working/medical-appointment-sdd-context.md` | **SDD Shared Context.** Contexto compartido que mantiene el estado actual del incremento, las decisiones tomadas durante la ejecucion y el handoff entre agentes. Se actualiza continuamente durante el desarrollo. |
| `docs/specs/.working/medical-appointment-spec-validation.md` | **Validacion de especificacion.** Resultados de la validacion automatica de la Master Spec contra criterios de completitud, consistencia y ausencia de ambiguedad. |
| `docs/specs/.working/medical-appointment-spec-revalidation.md` | **Revalidacion de especificacion.** Resultados de revalidaciones posteriores a correcciones de hallazgos identificados en la validacion inicial. |
| `docs/api/openapi.yaml` | **Contrato OpenAPI 3.1.** Definicion completa de la API: 14 endpoints, schemas de request/response, codigos de error, ejemplos. Genera automaticamente las interfaces de los controladores y los DTOs via openapi-generator. |

---

## Tecnologias

| Tecnologia | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.1 |
| Gradle | 8.x |
| PostgreSQL | 17 |
| JPA / Hibernate | - |
| Flyway | 10.x |
| OpenAPI | 3.1 |
| MapStruct | 1.6.x |
| Lombok | 1.18.x |
| JUnit 5 + Mockito | - |
| ArchUnit | 1.3.x |
| JaCoCo | 0.8.12 |
| Docker | - |
| AWS ECS Fargate | - |

---

## Arquitectura

El proyecto sigue una **Arquitectura Hexagonal (Puertos y Adaptadores)** con tres capas bien diferenciadas:

```
domain (reglas de negocio puras)
  ^
  |
application (casos de uso, puertos de entrada/salida)
  ^
  |
infrastructure (adaptadores: web, BD, clientes externos)
```

**Principios aplicados:**
- **Domain** no depende de ningun framework. Contiene los modelos de negocio (Cita, Medico, Paciente, FranjaHoraria, RegistroPenalizacion) y el servicio ValidadorReglasNegocio.
- **Application** define los casos de uso y los puertos (interfaces) que la infraestructura implementa.
- **Infrastructure** contiene los adaptadores: controladores REST, repositorios JPA, mapeadores, cliente HTTP (Nager.Date) y configuracion.
- SOLID y DRY como principios rectores.
- Virtual Threads habilitados para mejor throughput en operaciones de I/O.

---

## Decisiones Tecnicas

### ¿Por que JPA / Hibernate?

Se eligio JPA (Jakarta Persistence) con Hibernate como ORM porque:

- **Productividad:** Mapeo automatico objeto-relacional que elimina la necesidad de escribir SQL repetitivo (CRUD basico). Las consultas personalizadas se definen con JPQL o Spring Data derived queries.
- **Integracion con Spring Boot:** Spring Data JPA ofrece repositorios con metodos generados automaticamente (`findById`, `findAll`, `save`) y capacidad de definir consultas por nombre de metodo.
- **Portabilidad:** Cambiar de base de datos (PostgreSQL → H2) solo requiere cambiar el dialecto y driver, sin modificar codigo de persistencia. Esto es clave para usar H2 en tests de integracion.
- **Proteccion contra SQL injection:** Todas las consultas se construyen mediante JPQL parametrizado o Spring Data derived queries, eliminando el riesgo de concatenacion manual de SQL.

### ¿Por que Flyway?

Flyway se eligio para gestion de migraciones de base de datos porque:

- **Versionado explicito:** Cada migracion es un archivo SQL con numero de version (V1.0.1, V1.0.2, etc.) que se aplica en orden y solo una vez.
- **Estado deterministico:** El schema de la base de datos queda reflejado en el codigo fuente. Cualquier entorno (local, test, produccion) obtiene exactamente el mismo schema.
- **Integracion con JPA + ddl-auto=validate:** Flyway crea las tablas y JPA valida que las entidades coincidan con el schema existente. Esto evita desincronizaciones entre el codigo y la base de datos.
- **Rollback implicito:** Como cada migracion es incremental y esta versionada, reconstruir una base de datos desde cero ejecuta todas las migraciones en orden.

### ¿Por que API Design First con OpenAPI?

Se adopto el enfoque **API Design First** (primero el contrato, luego la implementacion) porque:

- **Contrato como fuente de verdad:** El archivo `docs/api/openapi.yaml` define el contrato de la API (endpoints, schemas, codigos de error) antes de escribir cualquier codigo. Esto garantiza que la implementacion respete el diseno acordado.
- **Generacion automatica de interfaces:** OpenAPI Generator (plugin de Gradle) genera las interfaces de los controladores y los DTOs a partir del contrato. Esto elimina errores de tipeo en nombres de campos y endpoints, y asegura consistencia entre la documentacion y el codigo.
- **Documentacion viva:** El mismo contrato OpenAPI alimenta Swagger UI, que ofrece documentacion interactiva actualizada automaticamente.
- **Facilidad para evolucion:** Cambiar un endpoint solo requiere modificar el contrato y regenerar. Los errores de compilacion aparecen si la implementacion no coincide con el contrato.

**Flujo de trabajo:**

```
editar openapi.yaml → ./gradlew build (genera interfaces) → implementar interfaces en controllers
```

Las interfaces se generan automaticamente con Gradle en cada build (tarea `openApiGenerate`). No se versionan en el repositorio (estan en `build/generated/`).

### ¿Por que @RestControllerAdvice (GlobalExceptionHandler)?

Se creo un manejador global de excepciones con `@RestControllerAdvice` porque:

- **Consistencia en errores:** Todas las respuestas de error siguen el mismo formato `ApiErrorResponse` con los campos `timestamp`, `status`, `error`, `code`, `message`, `path`, `trace_id` y `details`. Sin un handler global, cada controlador podria devolver errores en formatos distintos.
- **Separacion de concerns:** La logica de mapeo excepcion → codigo HTTP esta centralizada en un solo lugar, no dispersa en los controladores. Las excepciones de dominio (`BusinessException`, `ConflictException`, `ResourceNotFoundException`) solo contienen el codigo de error y el mensaje; el handler decide el status HTTP.
- **Cobertura total:** Cualquier excepcion no esperada es capturada por el handler generico (`Exception.class`) que devuelve 500 Internal Server Error sin exponer detalles internos.
- **Trace ID:** Cada error incluye un `trace_id` unico (UUID) que permite correlacionar errores en los logs sin exponer informacion sensible.

### ¿Por que el telefono solo acepta digitos?

El telefono de medicos y pacientes se almacena **solo con digitos** (sin guiones, espacios, ni caracteres especiales) porque:

- **Dato limpio:** Almacenar solo digitos facilita busquedas, comparaciones y futuras integraciones con servicios de mensajeria (SMS, WhatsApp).
- **Formato desacoplado:** El frontend es responsable de formatear el numero para visualizacion (ej: "300 123 4567" o "+57 3001234567"). El backend guarda la representacion canonica.
- **Validacion en API:** El contrato OpenAPI define `pattern: '^\d{7,20}$'` para que la validacion ocurra en la capa HTTP (via Bean Validation) antes de llegar al servicio.
- **Defensa en profundidad:** Los servicios (`MedicoService`, `PacienteService`) aplican `replaceAll("\\D", "")` para eliminar cualquier caracter no numerico antes de persistir, como capa adicional de seguridad.

**Regla:** Solo se persisten los digitos (0-9). Cualquier otro caracter es rechazado en la API o eliminado en el servicio.

### ¿Por que UUID v4 generado por la aplicacion con Persistable?

Los IDs de todas las entidades usan **UUID v4 generado por la aplicacion** (`UUID.randomUUID()` en el dominio) y las entidades JPA implementan `Persistable<UUID>` porque:

- **Descentralizacion:** No depende de secuencias de base de datos ni de generadores externos. Cualquier capa puede crear una entidad con su ID antes de persistirla.
- **Compatibilidad REST:** Los UUIDs son ideales para URLs de recursos REST (`/api/v1/medicos/{id}`) porque no exponen informacion secuencial ni son adivinables.
- **Arquitectura Hexagonal:** El dominio genera el ID antes de llamar al repositorio, lo que permite trabajar con entidades completamente formadas sin depender de la infraestructura de persistencia.
- **Persistable flag:** Spring Data JPA usa el metodo `isNew()` de `Persistable` para determinar si una entidad es nueva (debe hacer `persist()`) o existente (debe hacer `merge()`). El flag `@Transient @Builder.Default private boolean isNew = true;` se inicializa en `true` para nuevas entidades y se cambia a `false` con `@PrePersist`/`@PostLoad` despues de la insercion o al cargar desde BD.
- **Eliminacion de @GeneratedValue:** Se removio `@GeneratedValue(strategy = GenerationType.UUID)` de las entidades porque el ID ya lo asigna el dominio. Esto evita que Hibernate confunda entidades nuevas con detached y lance `StaleObjectStateException`.

**Flujo de creacion:**

```
domain: new Medico(UUID.randomUUID(), ...) → adapter: mapper.toEntity(dom) → entity con ID + isNew=true → jpaRepository.save() → persist() (no merge) → @PrePersist marca isNew=false
```

### ¿Por que triggers de base de datos para updated_at?

Se creo una funcion y triggers en PostgreSQL (`V1.0.6__create_trigger_updated_at.sql`) para actualizar automaticamente la columna `updated_at` en todas las tablas:

- **Doble capa de seguridad:** La aplicacion usa `@UpdateTimestamp` de Hibernate para mantener `updated_at` al guardar via JPA. Los triggers de BD actuan como respaldo ante actualizaciones directas en la base de datos (consola, pgadmin, scripts).
- **Consistencia en produccion:** Si un operador modifica datos directamente en PostgreSQL, el `updated_at` se actualiza automaticamente sin depender de la aplicacion.
- **Compatibilidad con H2 en tests:** Flyway esta deshabilitado en el perfil `test` (`spring.flyway.enabled=false`) y JPA usa `ddl-auto=create-drop`. Los triggers de PostgreSQL nunca se ejecutan en H2, por lo que no afectan los tests de integracion.

**Regla:** `@UpdateTimestamp` para el dia a dia via JPA. Triggers de BD como red de seguridad en produccion.

### ¿Por que ValidadorReglasNegocio vive en el dominio?

`ValidadorReglasNegocio` es una clase **final** con **metodos estaticos puros** (`public static boolean`) ubicada en el paquete `domain.service`. Se tomo esta decision arquitectonica por:

- **Reglas de negocio en su lugar natural:** Las reglas RN-01 a RN-06 son parte del lenguaje ubiquo del dominio. Colocarlas en `domain.service` las mantiene cerca de los modelos de negocio (`Cita`, `Medico`, `Paciente`) y visibles para cualquier capa que las necesite.
- **Cero dependencias de framework:** La clase solo importa tipos de `java.time` y `java.util`. No usa Spring, JPA, Jackson ni ninguna otra dependencia de infraestructura. Esto garantiza que las reglas de negocio son 100% portables y testeables sin contexto de framework.
- **Arquitectura Hexagonal:** Cumple la regla ArchUnit #1: `domain` no depende de `infrastructure`, `application`, Spring, JPA ni Jackson. La clase es un **servicio de dominio puro** que puede ser llamado desde los casos de uso (`application.service`) sin acoplar el dominio a la infraestructura.
- **Testeable sin Spring:** Al ser metodos estaticos puros, los tests unitarios (`ValidadorReglasNegocioTest`) se ejecutan con JUnit 5 + AssertJ, sin necesidad de levantar contexto Spring, lo que los hace ultra-rapidos (~2.5s para 184 tests).
- **Estado global cero:** Al ser metodos estaticos sin estado mutable, la clase es inherentemente thread-safe y no requiere sincronizacion.

**Regla:** `ValidadorReglasNegocio` solo importa `java.*`. Ninguna anotacion de framework, ninguna dependencia externa. Solo reglas de negocio puras.

---
 
## Cobertura de Pruebas (JaCoCo)

La cobertura se evalua sobre el **codigo testable**, excluyendo clases que no contienen logica de negocio verificable. El umbral minimo del **85%** en instrucciones se verifica en cada build mediante `jacocoTestCoverageVerification`.

| Metrica | Resultado | Umbral |
|---|---|---|
| Instrucciones (lineas de codigo ejecutado) | **93%** | 85% |
| Ramas (condiciones if/else, switch) | **86%** | - |
| Lineas | **95%** | - |
| Metodos | **96%** | - |
| Clases | **91%** | - |

**184 tests automatizados**, 0 fallos, 0 ignorados.

### Exclusiones de JaCoCo

Se excluyen del analisis de cobertura las siguientes categorias por no contener logica de negocio:

| Categoria | Patron | Motivo |
|---|---|---|
| DTOs de API | `**/dto/**` | Clases de transporte de datos sin logica |
| Configuraciones Spring | `**/config/**` | Beans y configuraciones del framework |
| Entidades JPA | `**/entity/**` | Mapeo de tablas sin logica de negocio |
| Excepciones | `**/exception/**` | Clases simples con solo constructor |
| Mappers MapStruct generados | `**/*MapperImpl*` | Codigo generado automaticamente por MapStruct |
| Mappers de persistencia | `**/persistence/mapper/**` | Interfaces MapStruct que mapean Entity ↔ Domain sin logica de negocio verificable |
| Enums de dominio | `**/domain/model/EstadoCita`, `**/domain/model/Especialidad` | Enumeraciones sin logica condicional |
| Clase principal | `**/AppointmentApplication*` | Bootstrap de Spring Boot |
| API generada por OpenAPI | `**/infrastructure/web/api/**` | Interfaces de controllers generadas automaticamente |

---

## Tests

El proyecto cuenta con **184 tests automatizados** (0 fallos, 0 ignorados, ejecucion en ~2.8s) organizados por tipo y capa:

### Por tipo de prueba

| Tipo | Ubicacion | Framework | Proposito |
|---|---|---|---|
| **Unitarios (Dominio)** | `src/test/java/.../domain/` | JUnit 5 + AssertJ | Validar reglas de negocio RN-01 a RN-06, modelos y value objects |
| **Unitarios (Aplicacion)** | `src/test/java/.../application/` | JUnit 5 + Mockito | Probar servicios con dependencias mockeadas |
| **Integracion (Repositorios)** | `src/test/java/.../infrastructure/persistence/` | Spring Boot Test + DataJpaTest + H2 | Validar consultas JPA y migraciones Flyway |
| **Integracion (Controladores)** | `src/test/java/.../infrastructure/web/` | Spring Boot Test + WebMvcTest | Validar endpoints HTTP, status codes y schemas ApiErrorResponse |
| **Arquitectura** | `src/test/java/.../archunit/` | ArchUnit | Verificar boundaries hexagonales (6 reglas) |
| **Cliente HTTP** | `src/test/java/.../infrastructure/client/` | JUnit 5 + MockRestServiceServer | Validar integracion con Nager.Date (fallback graceful, timeout) |

### Cobertura por regla de negocio

| Regla | Tests | Estado |
|---|---|---|
| RN-01 (Franjas horarias) | FranjaHorariaValidatorTest | ✅ |
| RN-02 (No duplicidad medico) | CitaServiceTest | ✅ |
| RN-03 (Fecha de nacimiento) | ValidadorReglasNegocioTest | ✅ |
| RN-04 (Conflicto paciente) | CitaServiceTest | ✅ |
| RN-05 (Penalizacion) | ValidadorReglasNegocioTest + CitaServiceTest | ✅ |
| RN-06 (Reprogramacion) | CitaServiceTest | ✅ |

### Tests de arquitectura (ArchUnit — 6 reglas)

1. `domain` no depende de `infrastructure`, `application`, Spring, JPA ni Jackson.
2. `domain` no contiene clases con anotaciones de framework (`@Entity`, `@Service`, `@Repository`, etc.).
3. `application` no depende de `infrastructure`.
4. Los controladores residen unicamente en `infrastructure.web`.
5. Los casos de uso residen unicamente en `application.service`.
6. Los puertos de salida residen unicamente en `application.port.output`.

---

## Instalacion y Ejecucion Local

### Prerrequisitos

- Java 21 (Temurin recomendado)
- PostgreSQL 15+ corriendo en `localhost:5432` (o via Docker)
- Docker (opcional, para contenedor local)

### Pasos

1. Clonar el repositorio:

```bash
git clone https://github.com/cristiansrc/ms-medical-appointment.git
cd ms-medical-appointment
```

2. Crear la base de datos PostgreSQL (si no existe):

```bash
createdb ms-medical-appointment
# O via psql:
# psql -U postgres -c "CREATE DATABASE \"ms-medical-appointment\""
```

3. Configurar credenciales en `src/main/resources/application-dev.yaml` (valores por defecto: `postgres`/`postgres`).

4. Ejecutar la aplicacion:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

5. La API estara disponible en: `http://localhost:8081`
6. Swagger UI: `http://localhost:8081/swagger-ui/index.html`
7. Health check: `http://localhost:8081/actuator/health`

### Ejecutar tests

```bash
./gradlew clean build
```

### Construir imagen Docker

```bash
docker build -t ms-medical-appointment .
docker run -p 8081:8081 --env-file .env ms-medical-appointment
```

---

## Endpoints y Ejemplos

| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| POST | `/api/v1/medicos` | Registrar medico |
| GET | `/api/v1/medicos` | Listar medicos |
| GET | `/api/v1/medicos/{id}` | Consultar medico |
| PUT | `/api/v1/medicos/{id}` | Actualizar medico |
| POST | `/api/v1/pacientes` | Registrar paciente |
| GET | `/api/v1/pacientes` | Listar pacientes |
| GET | `/api/v1/pacientes/{id}` | Consultar paciente |
| PUT | `/api/v1/pacientes/{id}` | Actualizar paciente |
| POST | `/api/v1/citas` | Reservar cita |
| GET | `/api/v1/citas` | Listar citas |
| GET | `/api/v1/citas/{id}` | Consultar cita |
| DELETE | `/api/v1/citas/{id}` | Cancelar cita |
| POST | `/api/v1/citas/{id}/reprogramar` | Reprogramar cita |
| GET | `/api/v1/disponibilidad` | Disponibilidad de franjas |

### Ejemplos de Request/Response

#### ▶️ POST /api/v1/medicos — Registrar medico

**Request:**
```json
{
  "nombre_completo": "Dr. Juan Perez",
  "especialidad": "Cardiologia",
  "telefono": "5552001",
  "email": "juan.perez@medisalud.com"
}

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombre_completo": "Dr. Juan Perez",
  "especialidad": "Cardiologia",
  "telefono": "5552001",
  "email": "juan.perez@medisalud.com",
  "activo": true,
  "created_at": "2026-07-18T10:00:00Z",
  "updated_at": "2026-07-18T10:00:00Z"
}
```

#### ▶️ POST /api/v1/pacientes — Registrar paciente

**Request:**
```json
{
  "nombre_completo": "Maria Lopez",
  "documento_identidad": "CC-1234567",
  "telefono": "3001234567",
  "email": "maria@email.com",
  "fecha_nacimiento": "1990-05-15"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "nombre_completo": "Maria Lopez",
  "documento_identidad": "CC-1234567",
  "telefono": "3001234567",
  "email": "maria@email.com",
  "fecha_nacimiento": "1990-05-15",
  "created_at": "2026-07-18T10:00:00Z",
  "updated_at": "2026-07-18T10:00:00Z"
}
```

#### ▶️ POST /api/v1/citas — Reservar cita

**Request:**
```json
{
  "paciente_id": "550e8400-e29b-41d4-a716-446655440001",
  "medico_id": "550e8400-e29b-41d4-a716-446655440000",
  "fecha_hora": "2026-07-21T09:00:00-05:00"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440003",
  "paciente_id": "550e8400-e29b-41d4-a716-446655440001",
  "medico_id": "550e8400-e29b-41d4-a716-446655440000",
  "fecha_hora": "2026-07-21T09:00:00-05:00",
  "estado": "PROGRAMADA",
  "created_at": "2026-07-18T10:00:00Z",
  "updated_at": "2026-07-18T10:00:00Z"
}
```

#### ▶️ DELETE /api/v1/citas/{id} — Cancelar cita

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440003",
  "estado": "CANCELADA",
  "motivo_cancelacion": "Cancelado por el paciente",
  "fecha_cancelacion": "2026-07-18T10:30:00Z",
  ...
}
```

#### ▶️ GET /api/v1/disponibilidad?medico_id={id}&fecha_inicio=2026-07-20&fecha_fin=2026-07-25

**Response (200 OK):**
```json
{
  "franjas": [
    {
      "medico_id": "550e8400-e29b-41d4-a716-446655440000",
      "inicio": "2026-07-20T08:00:00-05:00",
      "fin": "2026-07-20T08:30:00-05:00",
      "disponible": true
    },
    {
      "medico_id": "550e8400-e29b-41d4-a716-446655440000",
      "inicio": "2026-07-20T08:30:00-05:00",
      "fin": "2026-07-20T09:00:00-05:00",
      "disponible": false
    }
  ]
}
```

> **Nota:** La documentacion interactiva completa con schemas, codigos HTTP y ejemplos para todos los endpoints esta disponible en **Swagger UI**: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

---

## Manejo de Errores

Todas las respuestas de error siguen el formato estandar `ApiErrorResponse`:

```json
{
  "timestamp": "2026-07-18T10:30:00Z",
  "status": 422,
  "error": "UNPROCESSABLE_ENTITY",
  "code": "INVALID_SLOT",
  "message": "Las citas solo estan disponibles de Lunes a Viernes 8:00-18:00 y Sabados 8:00-13:00",
  "path": "/api/v1/citas",
  "trace_id": "4f0f6d2c-6b1d-4c8a-9e3f-7a2b5c8d1e0f",
  "details": null
}
```

**Codigos de error estables:**

| HTTP | Code | Descripcion |
|------|------|-------------|
| 400 | `VALIDATION_ERROR` | Campos invalidos o desconocidos en la solicitud |
| 400 | `INVALID_REQUEST_BODY` | JSON malformado |
| 422 | `INVALID_SLOT` | Franja horaria fuera del horario de atencion (RN-01) |
| 422 | `INVALID_BIRTH_DATE` | Fecha de nacimiento futura (RN-03) |
| 404 | `RESOURCE_NOT_FOUND` | Recurso (medico, paciente, cita) no existe |
| 409 | `MEDICO_SLOT_CONFLICT` | Medico ya tiene cita en esa franja (RN-02) |
| 409 | `PACIENTE_SLOT_CONFLICT` | Paciente ya tiene cita en esa franja (RN-04) |
| 409 | `PACIENTE_BLOCKED` | Paciente bloqueado por penalizaciones (RN-05) |
| 409 | `CITA_ALREADY_CANCELLED` | La cita ya fue cancelada |
| 500 | `INTERNAL_ERROR` | Error inesperado del servidor |

---

## Despliegue en AWS

La aplicacion esta desplegada en **AWS Elastic Beanstalk** con el siguiente stack:

```
Cliente HTTPS → ALB (HTTPS/443) → Elastic Beanstalk → RDS PostgreSQL
```

**URL base:** [http://ms-medical-appointment-env.eba-meriebhu.us-east-2.elasticbeanstalk.com](http://ms-medical-appointment-env.eba-meriebhu.us-east-2.elasticbeanstalk.com)

**CI/CD:** GitHub Actions — build → test → deploy.

**Pendiente:** Configurar dominio personalizado y certificado SSL.

---

## Mejoras Propuestas

Estas mejoras estan fuera del alcance del MVP actual pero fueron identificadas como valiosas para futuras iteraciones:

1. **Autenticacion y autorizacion (JWT/Keycloak)** — Proteger endpoints por rol.
2. **Paginacion** — En listados de citas, medicos y pacientes.
3. **Notificaciones** — Email/SMS recordatorio de citas.
4. **Internacionalizacion (i18n)** — Mensajes multi-idioma.
5. **Frontend** — Interfaz de usuario con React o Angular.
6. **Logs de auditoria** — Registro de todas las operaciones.
7. **Restringir CORS** — Configurar origenes permitidos por perfil.
8. **Pruebas E2E** — Con frontend o herramientas como Postman/Newman.
9. **Solución óptima para race condition en reserva de citas** — La solución actual usa un índice único parcial (`UNIQUE INDEX ... WHERE estado = 'PROGRAMADA'`) que previene doble reserva a nivel de BD. Para escenarios de ultra-alta concurrencia, se recomienda implementar `pg_advisory_xact_lock(hashtext(medico_id || '-' || fecha_hora))` antes de la validación y el INSERT. Esto serializa las requests del mismo slot sin necesidad de capturar excepciones de integridad. Ver `docs/specs/master-spec.md` D-20 para más detalles.

10. **Soporte multi-pais** — La aplicacion actual esta configurada exclusivamente para Colombia (UTC-5, festivos CO, horarios fijos). Para escalar a multiples paises se requeriria:

    - **Entidad Sucursal/Clinica:** Nueva tabla con campos `pais` (ISO 3166-1 alpha-2), `zona_horaria` (IANA, ej: America/Bogota), `idioma`. Medicos y pacientes se asociarian a una sucursal.
    - **Festivos dinamicos:** Parametrizar el codigo de pais en la consulta a Nager.Date en vez de tener `CO` hardcodeado.
    - **Horarios configurables:** Tabla `horarios_atencion` con (`sucursal_id`, `dia_semana`, `hora_inicio`, `hora_fin`) reemplazando los horarios fijos L-V 08-18 / S 08-13.
    - **Zona horaria dinamica:** Usar `ZoneId` en vez de `ZoneOffset.ofHours(-5)` para soportar regiones con horario de verano.
    - **Telefono con prefijo:** Integrar `libphonenumber` de Google para validar telefonos con prefijo internacional segun el pais.
    - **i18n:** Externalizar mensajes de error y respuestas a bundles de idiomas (soporte multi-lenguaje).

    **Prioridad sugerida:** Sucursal + zona horaria → Festivos por pais → Horarios configurables → i18n → libphonenumber.

---

## Licencia

Proyecto de prueba tecnica.
