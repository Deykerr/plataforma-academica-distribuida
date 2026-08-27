# Scripts SQL

El esquema ejecutable del servicio de usuarios está versionado con Flyway en:

`backend/servicio-usuarios/src/main/resources/db/migration/V1__crear_esquema_usuarios.sql`

El esquema ejecutable del servicio de cursos está en:

`backend/servicio-cursos/src/main/resources/db/migration/V1__crear_esquema_cursos.sql`

El esquema ejecutable del servicio de matriculas está en:

`backend/servicio-matriculas/src/main/resources/db/migration/V1__crear_esquema_matriculas.sql`

El esquema ejecutable del servicio de evaluaciones está en:

`backend/servicio-evaluaciones/src/main/resources/db/migration/V1__crear_esquema_evaluaciones.sql`

Flyway aplica las migraciones automáticamente y evita ejecutar scripts manuales fuera de orden. Cada microservicio mantiene sus migraciones dentro de su propio proyecto para conservar el aislamiento de datos.
