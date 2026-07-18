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

La cobertura se evalua sobre el **codigo testable**, excluyendo clases que no contienen logica de negocio verificable. El umbral minimo del **85%** en instrucciones se verifica en cada build mediante `jacocoTestCoverageVerification`.

| Metrica | Resultado | Umbral |
|---|---|---|
| Instrucciones (líneas de codigo ejecutado) | **93%** | 85% |
| Ramas (condiciones if/else, switch) | **79%** | - |
| Lineas | **95%** | - |
| Metodos | **96%** | - |
| Clases | **91%** | - |

**176 tests automatizados**, 0 fallos, 0 ignorados.

### Exclusiones de JaCoCo

Se excluyen del analisis de cobertura las siguientes categorias por no contener logica de negocio:

| Categoria | Patron | Motivo |
|---|---|---|
| DTOs de API | `**/dto/**` | Clases de transporte de datos sin logica |
| Configuraciones Spring | `**/config/**` | Beans y configuraciones del framework |
| Entidades JPA | `**/entity/**` | Mapeo de tablas sin logica de negocio |
| Excepciones | `**/exception/**` | Clases simples con solo constructor |
| Mappers MapStruct generados | `**/*MapperImpl*` | Codigo generado automaticamente por MapStruct |
| Mappers de persistencia | `**/persistence/mapper/**` | Interfaces MapStruct que mapean Entity ↔ Domain (92 instrucciones, 0% cobertura por ser interfaces) |
| Enums de dominio | `**/domain/model/EstadoCita`, `**/domain/model/Especialidad` | Enumeraciones sin logica condicional |
| Clase principal | `**/AppointmentApplication*` | Bootstrap de Spring Boot |
| API generada por OpenAPI | `**/infrastructure/web/api/**` | Interfaces de controllers generadas automaticamente |

---

## Tests

El proyecto cuenta con **176 tests automatizados** (0 fallos, 0 ignorados, ejecucion en ~2.5s) organizados por tipo y capa:

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
