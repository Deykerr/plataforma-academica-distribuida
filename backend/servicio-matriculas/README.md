# Servicio de Matrículas

Microservicio transaccional responsable de los períodos, la oferta de secciones, los horarios, las vacantes y las inscripciones oficiales. Se ejecuta en el puerto `8083` y administra exclusivamente `matriculas_db`.

## Funcionalidades

- períodos académicos y ventanas de matrícula;
- secciones vinculadas mediante identificadores a curso, aula y docente;
- varios horarios por sección;
- detección de choques de aula, docente y estudiante;
- validación remota de estudiantes, docentes, cursos y aulas;
- apertura, cierre, inicio, finalización y cancelación controladas;
- matrícula con bloqueo pesimista para evitar sobreventa de vacantes;
- prevención de duplicados por sección y curso en el mismo período;
- validación de que el estudiante aprobó todos los prerrequisitos del curso;
- retiro, anulación y finalización de matrículas;
- vistas personales de estudiantes y docentes;
- reportes de ocupación, vacantes y estudiantes únicos;
- endpoint de validación para el futuro Servicio de Evaluaciones.

Antes de crear una matrícula, Cursos entrega los identificadores de prerrequisitos y Evaluaciones confirma cuáles fueron aprobados por el estudiante. Si queda alguno pendiente, la operación se rechaza con `400` y no se reserva una vacante.

## Endpoints principales

| Recurso | Acceso de lectura | Escritura |
|---|---|---|
| `/api/v1/periodos` | Usuario autenticado | Administrador |
| `/api/v1/secciones` | Usuario autenticado | Administrador |
| `/api/v1/secciones/mias` | Docente | Solo lectura |
| `/api/v1/matriculas` | Administrador | Administrador o estudiante propietario |
| `/api/v1/matriculas/mias` | Estudiante | Solo lectura |
| `/api/v1/matriculas/{id}/validacion` | Usuario autenticado | Solo lectura |
| `/api/v1/reportes` | Administrador | Solo lectura |

## Ejecución local

Desde la raíz del repositorio, levante las bases y los dos servicios requeridos:

```powershell
docker compose -f infraestructura\docker-compose.yml up -d
```

Después, en otra terminal:

```powershell
cd backend\servicio-matriculas
.\mvnw.cmd spring-boot:run
```

- Swagger Usuarios: `http://localhost:8081/swagger-ui.html`
- Swagger Cursos: `http://localhost:8082/swagger-ui.html`
- Swagger Matrículas: `http://localhost:8083/swagger-ui.html`

Obtenga el JWT en Usuarios y péguelo en **Authorize** de los otros servicios.

## Orden de una prueba manual

1. Crear un período en estado `PLANIFICADO`.
2. Crear una sección con los identificadores reales de curso, aula y docente.
3. Cambiar el período a `MATRICULA_ABIERTA`.
4. Cambiar la sección a `ABIERTA`.
5. Crear una matrícula con `estudianteId` y `seccionId`.
6. Consultar el resumen del período y la ocupación por sección.
7. Probar un retiro y verificar que la vacante vuelva a estar disponible.

Las fechas de matrícula deben incluir la fecha actual para poder abrir el período.

## Variables de entorno

| Variable | Predeterminado local |
|---|---|
| `SERVER_PORT` | `8083` |
| `DB_URL` | `jdbc:postgresql://localhost:5435/matriculas_db` |
| `DB_USERNAME` | `matriculas_app` |
| `DB_PASSWORD` | `matriculas_password` |
| `USUARIOS_SERVICE_URL` | `http://localhost:8081` |
| `CURSOS_SERVICE_URL` | `http://localhost:8082` |
| `EVALUACIONES_SERVICE_URL` | `http://localhost:8084` |
| `JWT_SECRET` | Debe coincidir con Usuarios y Cursos |

## Pruebas

```powershell
.\mvnw.cmd test
```

La prueba cubre seguridad, estados, prerrequisitos aprobados y pendientes, conflictos de horario, cupos, duplicados, retiro, reportes, validación y OpenAPI.
