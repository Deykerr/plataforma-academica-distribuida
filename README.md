# Plataforma académica distribuida

Proyecto final del curso de Sistemas Distribuidos. La solución se construye con microservicios Java Spring Boot, PostgreSQL, APIs REST, JWT, Docker y un frontend React.

## Estado actual

Están implementados los cuatro microservicios del backend:

- `servicio-usuarios`: centraliza credenciales, roles y perfiles de estudiantes y docentes;
- `servicio-cursos`: administra carreras, ciclos, cursos, prerrequisitos, aulas y el catálogo académico;
- `servicio-matriculas`: gestiona períodos, secciones, horarios, vacantes, inscripciones y retiros;
- `servicio-evaluaciones`: administra evaluaciones, calificaciones, promedios e historial académico.

Funcionalidades disponibles:

- registro público de estudiantes;
- inicio de sesión y emisión de tokens JWT;
- consulta y actualización del perfil propio del estudiante;
- CRUD administrativo de estudiantes y docentes;
- baja lógica mediante el estado del usuario;
- actualización administrativa de estado y roles;
- validación de identidad para los futuros microservicios;
- validación de solicitudes, paginación y errores HTTP uniformes;
- migraciones PostgreSQL con Flyway;
- documentación interactiva OpenAPI/Swagger;
- pruebas de integración del flujo principal.
- validación distribuida de prerrequisitos aprobados antes de matricular;
- frontend React con autenticación JWT, rutas protegidas y paneles por rol.
- módulos React para usuarios, catálogo, operación académica, evaluaciones, notas, matrículas y perfil.

El Servicio de Cursos también valida las reglas de prerrequisitos, el aforo de aulas y la vigencia efectiva de los cursos. Reutiliza los JWT emitidos por Usuarios y conserva sus datos en una base PostgreSQL independiente.

El Servicio de Matrículas integra las APIs de Usuarios, Cursos y Evaluaciones, controla cupos concurrentes, choques de horario y prerrequisitos aprobados. Evaluaciones valida las matrículas por REST, controla ponderaciones, restringe el historial y calcula automáticamente el resultado final.

## Estructura

```text
backend/
  servicio-usuarios/       # Implementado en este avance (puerto 8081)
  servicio-cursos/         # Implementado (puerto 8082)
  servicio-matriculas/     # Implementado (puerto 8083)
  servicio-evaluaciones/   # Implementado (puerto 8084)
base-de-datos/
  diagramas-er/
  scripts-sql/
documentacion/
infraestructura/
  docker-compose.yml
frontend/
  app-web/                 # React: autenticación y paneles por rol
```

Cada microservicio tiene su propia base. `servicio-usuarios` únicamente conserva `carreraId` como identificador externo y no accede a `cursos_db`; la comunicación entre servicios se realiza mediante API y JWT.

## Ejecución rápida con Docker

Requisitos: Docker Desktop con Docker Compose.

Desde la raíz del repositorio:

```powershell
cd infraestructura
docker compose up --build
```

Cuando los contenedores estén listos:

- Swagger Usuarios: http://localhost:8081/swagger-ui.html
- Swagger Cursos: http://localhost:8082/swagger-ui.html
- Swagger Matrículas: http://localhost:8083/swagger-ui.html
- Swagger Evaluaciones: http://localhost:8084/swagger-ui.html
- PostgreSQL de Usuarios: `localhost:5433`
- PostgreSQL de Cursos: `localhost:5434`
- PostgreSQL de Matrículas: `localhost:5435`
- PostgreSQL de Evaluaciones: `localhost:5436`

Para detener los contenedores sin borrar los datos:

```powershell
docker compose down
```

En otra terminal, inicie el frontend:

```powershell
cd frontend\app-web
npm install
npm run dev
```

Abra `http://localhost:5173`. La cuenta local inicial es `admin@academica.local` con contraseña `Admin123*`.

## Ejecución para desarrollo

Primero levante las cuatro bases PostgreSQL:

```powershell
cd infraestructura
docker compose up -d postgres-usuarios postgres-cursos postgres-matriculas postgres-evaluaciones
cd ..\backend\servicio-usuarios
.\mvnw.cmd spring-boot:run
```

Luego, en otra terminal:

```powershell
cd backend\servicio-cursos
.\mvnw.cmd spring-boot:run
```

Y en una tercera terminal:

```powershell
cd backend\servicio-matriculas
.\mvnw.cmd spring-boot:run
```

Finalmente, en una cuarta terminal:

```powershell
cd backend\servicio-evaluaciones
.\mvnw.cmd spring-boot:run
```

Los servicios usan Java 21. Flyway crea y versiona las tablas automáticamente al iniciar.

## Primera prueba en Swagger

1. Abra `http://localhost:8081/swagger-ui.html`.
2. Ejecute `POST /api/v1/auth/login` con:

```json
{
  "correo": "admin@academica.local",
  "clave": "Admin123*"
}
```

3. Copie únicamente el valor de `token`.
4. Pulse **Authorize** y pegue el token.
5. Pruebe los endpoints administrativos de Usuarios. `POST /api/v1/estudiantes` también es público y siempre asigna únicamente el rol `ESTUDIANTE`.
6. Abra Swagger de Cursos, pulse **Authorize** y pegue el mismo token, sin escribir la palabra `Bearer`.
7. Cree en orden una carrera, sus ciclos, cursos y aulas, o consulte `GET /api/v1/catalogo`.
8. Abra Swagger de Matrículas, autorice el mismo token y cree un período y una sección.
9. Abra el período y la sección para registrar matrículas y consultar vacantes.
10. Abra Swagger de Evaluaciones, autorice el mismo token, configure evaluaciones que sumen 100%, registre las notas y publíquelas.

Las credenciales anteriores son solo para desarrollo local. Antes de desplegar se deben cambiar `ADMIN_PASSWORD`, `JWT_SECRET` y las contraseñas de las cuatro bases.

## Pruebas automatizadas

```powershell
cd backend\servicio-usuarios
.\mvnw.cmd test

cd ..\servicio-cursos
.\mvnw.cmd test

cd ..\servicio-matriculas
.\mvnw.cmd test

cd ..\servicio-evaluaciones
.\mvnw.cmd test
```

Cada prueba levanta el servicio correspondiente con una base H2 aislada. Los flujos verifican autenticación, permisos, duplicados, catálogo, prerrequisitos, aforo y generación de OpenAPI.

El frontend se verifica con:

```powershell
cd frontend\app-web
npm run lint
npm run build
```

## Documentación

- [Requisitos funcionales](documentacion/requisitos-funcionales.md)
- [Requisitos no funcionales](documentacion/requisitos-no-funcionales.md)
- [Detalle del servicio de usuarios](backend/servicio-usuarios/README.md)
- [Modelo entidad-relación de usuarios](base-de-datos/diagramas-er/usuarios.md)
- [Detalle del servicio de cursos](backend/servicio-cursos/README.md)
- [Modelo entidad-relación de cursos](base-de-datos/diagramas-er/cursos.md)
- [Detalle del servicio de matrículas](backend/servicio-matriculas/README.md)
- [Modelo entidad-relación de matrículas](base-de-datos/diagramas-er/matriculas.md)
- [Detalle del servicio de evaluaciones](backend/servicio-evaluaciones/README.md)
- [Modelo entidad-relación de evaluaciones](base-de-datos/diagramas-er/evaluaciones.md)
- [Guía del frontend React](frontend/app-web/README.md)
- [Guía de pruebas integrales desde el frontend](documentacion/guia-pruebas-frontend.md)
