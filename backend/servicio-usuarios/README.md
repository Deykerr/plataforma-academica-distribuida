# Servicio de Usuarios

Microservicio responsable de credenciales, roles, estudiantes, docentes y validación de identidades. Se ejecuta en el puerto `8081` y administra exclusivamente la base `usuarios_db`.

## Endpoints principales

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Público | Obtiene un JWT |
| POST | `/api/v1/estudiantes` | Público | Registra una cuenta con rol Estudiante |
| GET | `/api/v1/estudiantes/me` | Estudiante | Consulta el perfil propio |
| PUT | `/api/v1/estudiantes/me` | Estudiante | Actualiza el perfil propio |
| GET | `/api/v1/estudiantes` | Administrador/Docente | Lista y busca estudiantes |
| GET | `/api/v1/estudiantes/{id}` | Administrador/Docente | Consulta un estudiante |
| PUT | `/api/v1/estudiantes/{id}` | Administrador | Actualiza un estudiante |
| DELETE | `/api/v1/estudiantes/{id}` | Administrador | Realiza una baja lógica |
| POST | `/api/v1/docentes` | Administrador | Registra un docente |
| GET | `/api/v1/docentes` | Administrador/Docente | Lista docentes |
| PUT | `/api/v1/docentes/{id}` | Administrador | Actualiza un docente |
| DELETE | `/api/v1/docentes/{id}` | Administrador | Realiza una baja lógica |
| GET | `/api/v1/usuarios/{id}/validacion` | Autenticado | Valida existencia, estado y rol opcional |
| PATCH | `/api/v1/usuarios/{id}/estado` | Administrador | Activa o desactiva un usuario |
| PATCH | `/api/v1/usuarios/{id}/roles` | Administrador | Actualiza sus roles |

Los listados aceptan `busqueda`, `page`, `size` y `sort`. Todas las solicitudes y respuestas usan JSON.

## Variables de entorno

| Variable | Valor local predeterminado |
|---|---|
| `SERVER_PORT` | `8081` |
| `DB_URL` | `jdbc:postgresql://localhost:5433/usuarios_db` |
| `DB_USERNAME` | `usuarios_app` |
| `DB_PASSWORD` | `usuarios_password` |
| `JWT_SECRET` | Clave local incluida en `application.properties` |
| `JWT_EXPIRATION_MINUTES` | `120` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` |
| `BOOTSTRAP_ADMIN_ENABLED` | `true` |
| `ADMIN_EMAIL` | `admin@academica.local` |
| `ADMIN_PASSWORD` | `Admin123*` |

En producción se deben inyectar secretos seguros y establecer `BOOTSTRAP_ADMIN_ENABLED=false` después de provisionar el administrador.

## Comandos

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Swagger queda disponible en `http://localhost:8081/swagger-ui.html`.
