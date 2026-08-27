# Servicio de Cursos

Microservicio responsable del catálogo académico y la infraestructura física. Se ejecuta en el puerto `8082`, administra exclusivamente `cursos_db` y valida los JWT emitidos por `servicio-usuarios`.

## Funcionalidades

- CRUD y baja lógica de carreras y ciclos;
- cursos con créditos, horas y prerrequisitos;
- reglas que garantizan que ciclo y prerrequisitos correspondan a la carrera;
- aulas y laboratorios con capacidad y estado operativo;
- catálogo jerárquico de oferta activa;
- validación de cursos, disponibilidad y aforo para Matrículas;
- autorización por rol, Swagger, Flyway y errores JSON.

## Endpoints

| Recurso | Lectura | Escritura |
|---|---|---|
| `/api/v1/carreras` | Usuario autenticado | Administrador |
| `/api/v1/ciclos` | Usuario autenticado | Administrador |
| `/api/v1/cursos` | Usuario autenticado | Administrador |
| `/api/v1/aulas` | Usuario autenticado | Administrador |
| `/api/v1/catalogo` | Usuario autenticado | Solo lectura |
| `/api/v1/cursos/{id}/validacion` | Usuario autenticado | Solo lectura |
| `/api/v1/aulas/{id}/validacion` | Usuario autenticado | Solo lectura |

Los listados aceptan filtros y los parámetros `page`, `size` y `sort`.

## Ejecución local

Desde la raíz del repositorio:

```powershell
docker compose -f infraestructura\docker-compose.yml up -d postgres-usuarios postgres-cursos
```

En una terminal ejecute Usuarios y obtenga el JWT del administrador:

```powershell
cd backend\servicio-usuarios
.\mvnw.cmd spring-boot:run
```

En otra terminal ejecute Cursos:

```powershell
cd backend\servicio-cursos
.\mvnw.cmd spring-boot:run
```

- Swagger Usuarios: `http://localhost:8081/swagger-ui.html`
- Swagger Cursos: `http://localhost:8082/swagger-ui.html`

Copie el token obtenido en Usuarios y péguelo en **Authorize** dentro de Swagger de Cursos.

## Variables de entorno

| Variable | Predeterminado local |
|---|---|
| `SERVER_PORT` | `8082` |
| `DB_URL` | `jdbc:postgresql://localhost:5434/cursos_db` |
| `DB_USERNAME` | `cursos_app` |
| `DB_PASSWORD` | `cursos_password` |
| `JWT_SECRET` | Debe ser idéntico al configurado en Usuarios |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` |

## Pruebas

```powershell
.\mvnw.cmd test
```

La prueba HTTP cubre autenticación, autorización, carrera, ciclos, cursos, prerrequisitos, aula, aforo, catálogo y OpenAPI.
