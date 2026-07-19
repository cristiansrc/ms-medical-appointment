# MediSalud - Sistema de Agendamiento de Citas Medicas

API REST para agendar citas medicas con validacion de reglas de negocio, gestion de penalizaciones por cancelacion tardia e integracion con la API de festivos de Colombia (Nager.Date).

---

## Tabla de Contenido

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

---

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
  "telefono": "555-2001",
  "email": "juan.perez@medisalud.com"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombre_completo": "Dr. Juan Perez",
  "especialidad": "Cardiologia",
  "telefono": "555-2001",
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

La aplicacion esta desplegada en **AWS ECS Fargate** con el siguiente stack:

```
Cliente HTTPS → ALB (HTTPS/443) → ECS Fargate → RDS PostgreSQL
```

**CI/CD:** GitHub Actions — build → test → push a ECR → deploy a ECS.

**Perfil de produccion:** `aws` — usa variables de entorno para configurar base de datos, logging estructurado en JSON para CloudWatch, health checks con Kubernetes probes y desactivacion de Swagger UI.

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

---

## Licencia

Proyecto de prueba tecnica.
