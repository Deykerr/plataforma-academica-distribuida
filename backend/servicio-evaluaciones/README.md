# Servicio de Evaluaciones

Microservicio responsable de componentes de evaluación, calificaciones, promedios ponderados, resultados finales e historial académico. Usa el puerto `8084` y administra exclusivamente `evaluaciones_db`.

## Funcionalidades

- evaluaciones por sección con código, tipo, fecha, escala y ponderación;
- control de ponderación acumulada máxima de 100%;
- estados `BORRADOR`, `PUBLICADA`, `CERRADA` y `ANULADA`;
- registro individual y carga en lote de calificaciones;
- validación REST de matrícula y pertenencia a la sección;
- permisos por docente responsable y administrador;
- notas publicadas visibles únicamente al estudiante titular;
- promedio acumulado normalizado a escala vigesimal;
- resultado `EN_PROCESO`, `APROBADO` o `DESAPROBADO`;
- consulta distribuida de prerrequisitos aprobados para el Servicio de Matrículas;
- reportes de resultados, aprobados, desaprobados y promedio por sección;
- auditoría, control de concurrencia y errores JSON uniformes.

## Endpoints principales

| Método | Ruta | Acceso |
|---|---|---|
| POST/GET | `/api/v1/evaluaciones` | Administrador o docente responsable |
| PUT | `/api/v1/evaluaciones/{id}` | Administrador o docente; solo borrador |
| PATCH | `/api/v1/evaluaciones/{id}/estado` | Administrador o docente responsable |
| POST | `/api/v1/calificaciones` | Administrador o docente responsable |
| POST | `/api/v1/calificaciones/lote` | Administrador o docente responsable |
| PUT | `/api/v1/calificaciones/{id}` | Mientras la evaluación no esté cerrada |
| GET | `/api/v1/historial/matriculas/{id}` | Titular o administrador |
| POST | `/api/v1/resultados/prerrequisitos/validacion` | Estudiante titular o administrador |
| GET | `/api/v1/reportes/secciones/{id}/resultados` | Administrador o docente responsable |
| GET | `/api/v1/reportes/secciones/{id}/resumen` | Administrador o docente responsable |

## Ejecución

Desde la raíz se pueden levantar las dependencias y la base:

```powershell
docker compose -f infraestructura\docker-compose.yml up -d postgres-usuarios postgres-cursos postgres-matriculas postgres-evaluaciones servicio-usuarios servicio-cursos servicio-matriculas
cd backend\servicio-evaluaciones
.\mvnw.cmd spring-boot:run
```

Swagger: `http://localhost:8084/swagger-ui.html`.

Obtenga primero el JWT en `POST /api/v1/auth/login` del Servicio de Usuarios y péguelo en **Authorize** sin escribir `Bearer`.

## Orden de prueba manual

1. Tener una sección y una matrícula válidas en Matrículas.
2. Crear evaluaciones cuya suma de ponderaciones sea exactamente 100%.
3. Registrar notas individualmente o mediante `/calificaciones/lote`.
4. Cambiar las evaluaciones a `PUBLICADA` para que aparezcan en el historial.
5. Consultar el historial con el JWT del estudiante titular.
6. Consultar resultados y resumen con el docente responsable o administrador.
7. Cambiar una evaluación a `CERRADA`; desde entonces sus notas son inmutables.

La nota aprobatoria predeterminada es `11.00` sobre 20 y puede cambiarse mediante `NOTA_APROBATORIA`.

## Pruebas

```powershell
.\mvnw.cmd test
```

La prueba cubre JWT, roles, docente responsable, ponderaciones, duplicados, carga en lote, publicación, promedio, historial privado, reportes, cierre y OpenAPI.
