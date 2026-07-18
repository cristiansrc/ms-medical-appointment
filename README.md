# MediSalud - Sistema de Agendamiento de Citas Medicas

API REST para agendar citas medicas con validacion de reglas de negocio, gestion de penalizaciones por cancelacion tardia e integracion con la API de festivos de Colombia (Nager.Date).

---

## Tecnologias

| Tecnologia | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.1 |
| Gradle | 8.x |
| PostgreSQL | 15+ |
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

## Cobertura de Pruebas (JaCoCo)

La cobertura se evalua sobre el codigo testable, excluyendo DTOs, config, entities, exceptions, mappers generados y clases de infraestructura generada.

| Metrica | Resultado | Umbral |
|---|---|---|
| Instrucciones | **90%** | 85% |
| Ramas | **76%** | - |
| Lineas | **91%** | - |

El umbral minimo del 85% en instrucciones se verifica en cada build mediante `jacocoTestCoverageVerification`.

**Exclusiones:** `**/dto/**`, `**/config/**`, `**/entity/**`, `**/exception/**`, `**/*MapperImpl*`, `**/mapper/*Mapper`, `**/domain/model/EstadoCita`, `**/domain/model/Especialidad`, `**/AppointmentApplication*`, `**/infrastructure/web/api/**`

---

## Tests

La solucion cuenta con **176 tests automatizados** (0 fallos, 0 ignorados, ejecucion en ~2.5s) distribuidos en:

- **Tests Unitarios (Dominio):** `src/test/java/.../domain/` — Validacion de reglas de negocio (RN-01 a RN-06), modelos de dominio (Cita, Medico, Paciente, FranjaHoraria, RegistroPenalizacion).
- **Tests Unitarios (Aplicacion):** `src/test/java/.../application/` — Servicios de aplicacion con Mockito (CitaService, MedicoService, PacienteService).
- **Tests de Integracion:** `src/test/java/.../infrastructure/` — Repositorios JPA con `@DataJpaTest`, controladores REST con `@WebMvcTest`, adaptadores de persistencia.
- **Tests de Arquitectura:** `src/test/java/.../archunit/HexagonalArchitectureTest.java` — 6 reglas ArchUnit que verifican los boundaries hexagonales:
  - Domain no depende de application ni infrastructure.
  - Application no depende de infrastructure.
  - Infrastructure depende de application (puertos).
  - Los nombres de paquetes respetan la convencion hexagonal.
- **Tests de GlobalExceptionHandler:** Cobertura completa del manejo global de errores con todas las excepciones del dominio (BusinessException, ConflictException, ResourceNotFoundException).

---

## Instalacion y Ejecucion Local

### Prerrequisitos

- Java 21 (Temurin recomendado)
- PostgreSQL 15+ corriendo en `localhost:5432`
- Docker (opcional, para contenedor local)

### Pasos

1. Clonar el repositorio:

```bash
git clone <repo-url>
cd ms-medical-appointment
```

2. Crear la base de datos PostgreSQL:

```sql
CREATE DATABASE medisalud;
```

3. Configurar credenciales (valores por defecto: `postgres`/`postgres`). Editar `src/main/resources/application-dev.yaml` si es necesario.

4. Ejecutar la aplicacion:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

5. La API estara disponible en: `http://localhost:8080`
6. Swagger UI: `http://localhost:8080/swagger-ui.html`
7. Health check: `http://localhost:8080/actuator/health`

### Ejecutar tests

```bash
./gradlew clean build
```

### Construir imagen Docker

```bash
docker build -t ms-medical-appointment .
docker run -p 8080:8080 --env-file .env ms-medical-appointment
```

---

## Endpoints

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
| GET | `/api/v1/citas` | Listar citas (filtros opcionales: medico_id, paciente_id, estado, fecha_inicio, fecha_fin) |
| GET | `/api/v1/citas/{id}` | Consultar cita |
| DELETE | `/api/v1/citas/{id}` | Cancelar cita |
| POST | `/api/v1/citas/{id}/reprogramar` | Reprogramar cita |
| GET | `/api/v1/disponibilidad` | Consultar disponibilidad de franjas (requiere medico_id, fecha_inicio, fecha_fin) |

> **Nota:** La documentacion interactiva completa (request/response schemas, ejemplos, codigos HTTP) esta disponible en Swagger UI.

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
