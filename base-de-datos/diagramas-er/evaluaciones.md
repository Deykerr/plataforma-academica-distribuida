# Modelo entidad-relación: Servicio de Evaluaciones

```mermaid
erDiagram
    EVALUACIONES ||--o{ CALIFICACIONES : contiene

    EVALUACIONES {
        bigint id PK
        bigint seccion_id "Referencia externa"
        bigint periodo_id "Referencia externa"
        bigint curso_id "Referencia externa"
        bigint docente_id "Referencia externa"
        varchar codigo
        varchar nombre
        varchar tipo
        numeric ponderacion
        numeric nota_maxima
        date fecha
        varchar estado
        bigint version
        timestamptz creado_en
        timestamptz actualizado_en
    }

    CALIFICACIONES {
        bigint id PK
        bigint evaluacion_id FK
        bigint matricula_id "Referencia externa"
        bigint estudiante_id "Referencia externa"
        numeric valor
        varchar observacion
        bigint registrado_por
        bigint version
        timestamptz creado_en
        timestamptz actualizado_en
    }
```

`seccion_id`, `periodo_id`, `curso_id`, `docente_id`, `matricula_id` y `estudiante_id` no son llaves foráneas locales. Se validan mediante APIs REST para conservar el aislamiento de bases de datos entre microservicios.
