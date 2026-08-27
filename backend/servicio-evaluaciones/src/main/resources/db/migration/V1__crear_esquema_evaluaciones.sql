CREATE TABLE evaluaciones (
    id BIGSERIAL PRIMARY KEY,
    seccion_id BIGINT NOT NULL,
    periodo_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    docente_id BIGINT NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    ponderacion NUMERIC(5,2) NOT NULL,
    nota_maxima NUMERIC(5,2) NOT NULL DEFAULT 20.00,
    fecha DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    version BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMPTZ NOT NULL,
    actualizado_en TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_evaluacion_seccion_codigo UNIQUE (seccion_id, codigo),
    CONSTRAINT ck_evaluacion_ponderacion CHECK (ponderacion > 0 AND ponderacion <= 100),
    CONSTRAINT ck_evaluacion_nota_maxima CHECK (nota_maxima > 0),
    CONSTRAINT ck_evaluacion_estado CHECK (estado IN ('BORRADOR', 'PUBLICADA', 'CERRADA', 'ANULADA'))
);

CREATE TABLE calificaciones (
    id BIGSERIAL PRIMARY KEY,
    evaluacion_id BIGINT NOT NULL REFERENCES evaluaciones(id),
    matricula_id BIGINT NOT NULL,
    estudiante_id BIGINT NOT NULL,
    valor NUMERIC(5,2) NOT NULL,
    observacion VARCHAR(500),
    registrado_por BIGINT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMPTZ NOT NULL,
    actualizado_en TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_calificacion_evaluacion_matricula UNIQUE (evaluacion_id, matricula_id),
    CONSTRAINT ck_calificacion_valor CHECK (valor >= 0)
);

CREATE INDEX idx_evaluaciones_seccion ON evaluaciones(seccion_id);
CREATE INDEX idx_evaluaciones_periodo ON evaluaciones(periodo_id);
CREATE INDEX idx_evaluaciones_curso ON evaluaciones(curso_id);
CREATE INDEX idx_calificaciones_matricula ON calificaciones(matricula_id);
CREATE INDEX idx_calificaciones_estudiante ON calificaciones(estudiante_id);
