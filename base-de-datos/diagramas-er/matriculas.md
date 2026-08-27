# Modelo entidad-relación del Servicio de Matrículas

```mermaid
erDiagram
    PERIODOS ||--o{ SECCIONES : contiene
    SECCIONES ||--o{ HORARIOS_SECCION : programa
    PERIODOS ||--o{ MATRICULAS : registra
    SECCIONES ||--o{ MATRICULAS : recibe

    PERIODOS {
        bigint id PK
        varchar codigo UK
        varchar nombre
        date fecha_inicio
        date fecha_fin
        date fecha_inicio_matricula
        date fecha_fin_matricula
        varchar estado
    }

    SECCIONES {
        bigint id PK
        bigint periodo_id FK
        bigint curso_id "externo"
        bigint aula_id "externo"
        bigint docente_id "externo"
        varchar codigo
        integer capacidad
        varchar estado
        bigint version
    }

    HORARIOS_SECCION {
        bigint id PK
        bigint seccion_id FK
        varchar dia_semana
        time hora_inicio
        time hora_fin
    }

    MATRICULAS {
        bigint id PK
        bigint estudiante_id "externo"
        bigint seccion_id FK
        bigint periodo_id FK
        bigint curso_id "externo"
        timestamptz fecha_matricula
        varchar estado
        timestamptz fecha_retiro
        varchar motivo_retiro
    }
```

`curso_id`, `aula_id`, `docente_id` y `estudiante_id` son identificadores externos. Su validez se comprueba mediante APIs REST y no mediante claves foráneas hacia bases de otros microservicios.
