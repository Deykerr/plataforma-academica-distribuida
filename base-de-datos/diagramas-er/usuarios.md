# Modelo entidad-relación del Servicio de Usuarios

```mermaid
erDiagram
    USUARIOS ||--|{ USUARIO_ROLES : posee
    USUARIOS ||--o| ESTUDIANTES : identifica
    USUARIOS ||--o| DOCENTES : identifica

    USUARIOS {
        bigint id PK
        varchar correo UK
        varchar clave_hash
        varchar estado
        timestamptz creado_en
        timestamptz actualizado_en
    }

    USUARIO_ROLES {
        bigint usuario_id PK,FK
        varchar rol PK
    }

    ESTUDIANTES {
        bigint id PK
        bigint usuario_id UK,FK
        varchar codigo UK
        varchar documento_identidad UK
        varchar nombres
        varchar apellidos
        date fecha_nacimiento
        varchar telefono
        varchar direccion
        bigint carrera_id "Referencia externa"
    }

    DOCENTES {
        bigint id PK
        bigint usuario_id UK,FK
        varchar codigo UK
        varchar documento_identidad UK
        varchar nombres
        varchar apellidos
        varchar especialidad
        varchar telefono
    }
```

`carrera_id` no tiene clave foránea local porque la entidad Carrera pertenecerá al microservicio de Cursos. Su validez se comprobará mediante API REST cuando ese servicio sea implementado.
