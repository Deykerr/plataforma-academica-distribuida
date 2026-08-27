# Modelo entidad-relación del Servicio de Cursos

```mermaid
erDiagram
    CARRERAS ||--o{ CICLOS : contiene
    CARRERAS ||--o{ CURSOS : ofrece
    CICLOS ||--o{ CURSOS : agrupa
    CURSOS ||--o{ CURSO_PREREQUISITOS : requiere
    CURSOS ||--o{ CURSO_PREREQUISITOS : es_requerido

    CARRERAS {
        bigint id PK
        varchar codigo UK
        varchar nombre UK
        integer duracion_ciclos
        varchar estado
    }

    CICLOS {
        bigint id PK
        bigint carrera_id FK
        integer numero
        varchar nombre
        varchar estado
    }

    CURSOS {
        bigint id PK
        bigint carrera_id FK
        bigint ciclo_id FK
        varchar codigo UK
        varchar nombre
        integer creditos
        integer horas_teoria
        integer horas_practica
        varchar estado
    }

    CURSO_PREREQUISITOS {
        bigint curso_id PK,FK
        bigint prerequisito_id PK,FK
    }

    AULAS {
        bigint id PK
        varchar codigo UK
        varchar nombre
        varchar tipo
        integer capacidad
        varchar ubicacion
        varchar estado
    }
```

La base no contiene usuarios, docentes, secciones ni matrículas. Esos datos pertenecen a otros microservicios y se relacionarán mediante identificadores y APIs REST.
